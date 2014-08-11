package com.ezcode.jsnmpwalker.listener;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTree;

import com.ezcode.jsnmpwalker.search.Searchable;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.search.SearchableTree;
import com.ezcode.jsnmpwalker.search.TextSearcher;
import com.ezcode.jsnmpwalker.search.TreeSearcher;
import com.ezcode.jsnmpwalker.utils.PanelUtils;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public class TreeSearchListener extends AbstractSearchListener {
	
	private JCheckBox _includeTextCheckBox;
	
	public TreeSearchListener(Searchable searchable, JTextField searchField, JCheckBox includeTextCheckBox, Dialog dialog) {
		super(searchField, dialog);
		_searchable = searchable;
		_includeTextCheckBox = includeTextCheckBox;
	}
	
	
	public void startSearch() {
		boolean isIncludeText = _includeTextCheckBox.isSelected();
		_searcherThread = new TreeSearcher((SearchableTree) _searchable, _searchField.getText(), isIncludeText, true, false);
		_searcherThread.start();
	}

}
