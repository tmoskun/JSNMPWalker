package com.ezcode.jsnmpwalker.listener;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */




import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.search.TextSearcher;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class TextSearchListener extends AbstractSearchListener {
	private JCheckBox _caseSensitiveCheckBox;
	private JCheckBox _regexCheckBox;
	
	
	public TextSearchListener(JTextField searchField, JCheckBox caseSensitiveCheckBox, JCheckBox regexCheckBox) {
		this(searchField, caseSensitiveCheckBox, regexCheckBox, null);
	}
	
	public TextSearchListener(JTextField searchField, JCheckBox caseSensitiveCheckBox, JCheckBox regexCheckBox, Dialog dialog) {
		super(searchField, dialog);
		_caseSensitiveCheckBox = caseSensitiveCheckBox;
		_regexCheckBox = regexCheckBox;
	}
	
	
	public void startSearch() {
		String searchKey = _searchField.getText();
		boolean isCaseSensitive = _caseSensitiveCheckBox.isSelected();
		boolean isRegex = _regexCheckBox.isSelected();
		_searcherThread = new TextSearcher((SearchableText) _searchable, searchKey, isCaseSensitive, isRegex);
		_searcherThread.start();
	}
}
