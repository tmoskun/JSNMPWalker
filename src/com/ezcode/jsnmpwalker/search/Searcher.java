package com.ezcode.jsnmpwalker.search;

import java.util.TreeMap;

import javax.swing.SwingUtilities;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public class Searcher extends Thread {
	
	protected Searchable _searchable;
	protected String _searchKey;
		
	public Searcher(Searchable searchable, String searchKey) {
		_searchable = searchable;
		_searchKey = searchKey;
	}
	
	
    @Override
    public void interrupt() {
    	super.interrupt();
    	while(!isInterrupted()) {
    		try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
	    	_searchable.resetSearch(true);
	    	_searchable.disableSearchControls(false);
			}
		});
    }
	

}
