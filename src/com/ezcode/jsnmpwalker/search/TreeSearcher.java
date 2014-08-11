package com.ezcode.jsnmpwalker.search;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.iterator.TreeSearchIterator;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class TreeSearcher extends Searcher {
	
	private boolean _isIncludeText;
	private boolean _forward = true;
	private boolean _isNext = false;
	
	public TreeSearcher(Searchable searchable, boolean forward, boolean isNext) {
		this(searchable, searchable.getCurrentSearchKey(), ((SearchableTree) searchable).isCurrentInludeText(), forward, isNext);
	}
	
	public TreeSearcher(Searchable searchable, String searchKey, boolean isIncludeText, boolean forward, boolean isNext) {
		super(searchable, searchKey);
		_isIncludeText = isIncludeText;
		_forward = forward;
		_isNext = isNext;
	}
	

	@Override
	public void run() {	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
		    	_searchable.disableSearchControls(true);
			}
		});
		final SearchableTree searchableTree = (SearchableTree) _searchable;
		String currentSearchKey = searchableTree.getCurrentSearchKey();
		boolean isCurrentIncludeText = searchableTree.isCurrentInludeText();
		try {
			//if(_searchKey.equals(currentSearchKey) && _isIncludeText == isCurrentIncludeText) {
			if(_isNext) {
				if(_forward) {
					searchableTree.moveToNext();
				} else {
					searchableTree.moveToPrevious();
				}
			} else {
				searchableTree.resetSearch(true);
				searchableTree.setCurrentSearchKey(_searchKey);
				searchableTree.setCurrentIncludeText(_isIncludeText);
				searchableTree.findSearchResult();
			}
		} catch(SearchResultNotFoundException ex) {
			JOptionPane.showMessageDialog(null, ex.getMessage());
		} finally {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						searchableTree.showResult();
					} finally {
						_searchable.disableSearchControls(false);
					}
				}
			});
		}
	}
}
