package com.ezcode.jsnmpwalker.search;

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


public class TextSearcher extends Searcher {

	private boolean _isCaseSensitive;
	private boolean _isRegex;
	
	
	public TextSearcher(SearchableText searchable, String searchKey, boolean isCaseSensitive, boolean isRegex) {
		super(searchable, searchKey);
		_isCaseSensitive = isCaseSensitive;
		_isRegex = isRegex;
	}
	
	
	@Override
	public void run() {	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		    	_searchable.resetSearch(true);
				_searchable.disableSearchControls(true);
			}
		});
		final SearchableText searchableText = (SearchableText) _searchable;
		try {
			searchableText.setCurrentSearchKey(_searchKey);
			searchableText.setCaseSensitivity(_isCaseSensitive);
			searchableText.setRegex(_isRegex);
			searchableText.findSearchResult();
			if(searchableText.hasSearchResults()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						searchableText.showResult();
					}
				});
			}
		} catch (SearchResultNotFoundException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage());
		} 
	}
	
}

