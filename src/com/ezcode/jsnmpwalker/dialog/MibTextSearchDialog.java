package com.ezcode.jsnmpwalker.dialog;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.layout.WrapLayout;
import com.ezcode.jsnmpwalker.listener.TextSearchListener;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class MibTextSearchDialog extends SearchDialog {
	private TextSearchListener _searchListener;
	private JCheckBox _caseSensitiveCheckBox;
	private JCheckBox _regexCheckBox;
	
	public MibTextSearchDialog(Frame frame, String searchKey, boolean isCaseSensitive, boolean isRegex) {
		super(frame, 500, searchKey);
		setTitle("Find in the MIB description");
		_caseSensitiveCheckBox = new JCheckBox("Case Sensitive", isCaseSensitive);
		_regexCheckBox = new JCheckBox("Regex", isRegex);
		super.addOption(_caseSensitiveCheckBox);
		super.addOption(_regexCheckBox);
		_searchListener = new TextSearchListener(super.getSearchField(), _caseSensitiveCheckBox, _regexCheckBox, this);
		super.setListener(_searchListener);
	}
	
	public void setSearchable(SearchableText searchable) {
		_searchListener.setSearchable(searchable);
	}
	
}
