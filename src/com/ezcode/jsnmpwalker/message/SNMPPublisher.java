package com.ezcode.jsnmpwalker.message;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;

public class SNMPPublisher extends Thread {
	
	private BlockingQueue<String> _queue;
	private SNMPSessionFrame _frame;
	private Writer _writer;
	private ArrayList<String> _buffer;
	
	public SNMPPublisher(SNMPSessionFrame frame, BlockingQueue queue) {
		this(frame, queue, null);
	}
	
	
	public SNMPPublisher(SNMPSessionFrame frame, BlockingQueue<String> queue, Writer w) {
		_frame = frame;
		_writer = w;
		_queue = queue;
		_buffer = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		while(!isInterrupted()) {
			try {
				_buffer.clear();
				this.sleep(250);
				_queue.drainTo(_buffer);
				for(String result: _buffer) {
					if(!isInterrupted()) {
						_frame.appendResult(result);
						if(_writer != null) {
							try {
								_writer.write(result);
								_writer.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			} catch (InterruptedException e) {
				//ignore
				//e.printStackTrace();
			}	
		}
	}
		
}
