package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class TextSearcher extends Thread {

	private Searchable _searchable;
	private String _searchKey;
	private boolean _isCaseSensitive;
	private boolean _isRegex;
	private TreeMap<Integer, Integer> _searchPositions;
	private int _currentSearchPosition;
	
	
	public TextSearcher(Searchable searchable, String searchKey, boolean isCaseSensitive, boolean isRegex) {
		_searchable = searchable;
		_searchKey = searchKey;
		_isCaseSensitive = isCaseSensitive;
		_isRegex = isRegex;
		_searchPositions = searchable.getSearchPositions();
		_currentSearchPosition = searchable.getCurrentSearchPosition();
	}
	
	public TextSearcher(Searchable searchable, String searchKey,  boolean isCaseSensitive, boolean isRegex, TreeMap<Integer, Integer> searchPositions, int currentSearchPosition) {
		_searchable = searchable;
		_searchKey = searchKey;
		_isCaseSensitive = isCaseSensitive;
		_isRegex = isRegex;
		_searchPositions = searchPositions;
		_currentSearchPosition = currentSearchPosition;
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
	    	_searchable.enableSearchControls(false);
			}
		});
    }
      

	@Override
	public void run() {	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		    	_searchable.resetSearch(true);
				_searchable.enableSearchControls(true);
			}
		});
		String currentSearchKey = _searchable.getCurrentSearchKey();
		boolean isCurrentCaseSensitive = _searchable.isCaseSensitive();
		boolean isCurrentRegex = _searchable.isRegex();
		if(_searchKey.equals(currentSearchKey) && _isCaseSensitive == isCurrentCaseSensitive && _isRegex == isCurrentRegex) {
			//wrap around if it's finished
			try {
				_currentSearchPosition = _searchPositions.higherKey(_currentSearchPosition);
			} catch(Exception e) {
				_currentSearchPosition = _searchPositions.isEmpty() ? -1 : _searchPositions.firstKey();
			}
		} else {
			_searchable.setCurrentSearchKey(_searchKey);
			_searchable.setCaseSensitivity(_isCaseSensitive);
			_searchable.setRegex(_isRegex);
			String searchKeyAdjusted = _isRegex ? _searchKey : Pattern.quote(_searchKey);
			Pattern patt = null;
			if(_isCaseSensitive) {
				patt = Pattern.compile(searchKeyAdjusted);
			} else {
				patt = Pattern.compile(searchKeyAdjusted, Pattern.CASE_INSENSITIVE);
			}
			Matcher matt = patt.matcher(_searchable.getSearchText());
			_searchPositions.clear();
			while(matt.find()) {
				_searchPositions.put(matt.start(), matt.end());
			}
			_currentSearchPosition = _searchPositions.isEmpty() ? -1 : _searchPositions.firstKey();
		}
		if(_currentSearchPosition >= 0 && _currentSearchPosition <= _searchPositions.lastKey()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_searchable.showResults(_searchPositions, _currentSearchPosition);
				}
			});
		} else {
			JOptionPane.showMessageDialog(null, "No search results found for '"+ _searchKey + "'");
		}
	}
	
}

