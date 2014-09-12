package com.ezcode.jsnmpwalker.publish;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import javax.swing.SwingUtilities;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;

public class SNMPPublisher extends Thread {
	
	private BlockingQueue<String> _queue;
	private SNMPSessionFrame _frame;
	private ArrayList<String> _buffer;
	
	public SNMPPublisher(SNMPSessionFrame frame, BlockingQueue queue) {
		_frame = frame;
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
				final StringBuilder result = new StringBuilder();
				for(String s: _buffer) {
					result.append(s);
				}
				if(!isInterrupted()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							_frame.appendResult(result.toString());
						}
					});
				}
			} catch (InterruptedException e) {
				//ignore
				//e.printStackTrace();
			}	
		}
	}
		
}
