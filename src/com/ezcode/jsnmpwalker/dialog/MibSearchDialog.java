package com.ezcode.jsnmpwalker.dialog;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import com.ezcode.jsnmpwalker.listener.TreeSearchListener;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.search.SearchableTree;

public class MibSearchDialog extends SearchDialog {
	private JCheckBox _searchTextCheckBox;
	private ActionListener _searchListener;
	
	public MibSearchDialog(Frame frame, SearchableTree searchable, String searchKey, boolean isIncludeText) {
		super(frame, 400, searchKey);
		setTitle("Find MIB");
		_searchTextCheckBox = new JCheckBox("If not found, search in MIB descriptions", isIncludeText);
		super.addOption(_searchTextCheckBox);
		_searchListener = new TreeSearchListener(searchable, super.getSearchField(), _searchTextCheckBox, this);
		super.setListener(_searchListener);
	}

}
