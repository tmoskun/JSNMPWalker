package com.ezcode.jsnmpwalker.publish;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.SwingUtilities;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;

public class SNMPPublisher extends Thread {
	
	private BlockingQueue<String> _queue;
	private SNMPSessionFrame _frame;
	private ArrayList<String> _buffer;
	private List<String> _savedContent = null;
	private boolean _done;
	
	public SNMPPublisher(SNMPSessionFrame frame, BlockingQueue queue, List<String> savedContent) {
		_frame = frame;
		_queue = queue;
		_savedContent = savedContent;
		_buffer = new ArrayList<String>();
		_done = false;
	}
	
	public SNMPPublisher(SNMPSessionFrame frame, BlockingQueue queue) {
		this(frame, queue, frame.getSavedContent());
	}
	
	@Override
	public void interrupt() {
		try {
			super.interrupt();
		} catch(Exception e) {
			//ignore
		}
		_done = true;
	}
	
	@Override
	public boolean isInterrupted() {
		return _done || super.isInterrupted();
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
				if(_savedContent != null)
					_savedContent.addAll(_buffer);
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
