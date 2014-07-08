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

import com.ezcode.jsnmpwalker.panel.Searchable;
import com.ezcode.jsnmpwalker.panel.TextSearcher;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SearchListener implements ActionListener {
	private Searchable _searchable;
	private JTextField _searchField;
	private JCheckBox _caseSensitiveCheckBox;
	private JCheckBox _regexCheckBox;
	
	private Thread _searcherThread;
	private Dialog _dialog;
	
	public SearchListener(JTextField searchField, JCheckBox caseSensitiveCheckBox, JCheckBox regexCheckBox) {
		this(searchField, caseSensitiveCheckBox, regexCheckBox, null);
	}
	
	public SearchListener(JTextField searchField, JCheckBox caseSensitiveCheckBox, JCheckBox regexCheckBox, Dialog dialog) {
		_searchField = searchField;
		_caseSensitiveCheckBox = caseSensitiveCheckBox;
		_regexCheckBox = regexCheckBox;
		_dialog = dialog;
	}
	
	public void setSearchable(Searchable searchable) {
		_searchable = searchable;
	}
	
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		String name = "";
		if(obj instanceof JButton) {
			name = ((JButton) obj).getText();
		}
		if(name.equals(PanelUtils.TEXT_STOP)) {
			_searcherThread.interrupt();
		} else {
			String searchKey = _searchField.getText();
			boolean isCaseSensitive = _caseSensitiveCheckBox.isSelected();
			boolean isRegex = _regexCheckBox.isSelected();
			if(searchKey.length() > 0 && _searchable != null) {
				_searcherThread = new TextSearcher(_searchable, searchKey, isCaseSensitive, isRegex);
				_searcherThread.start();
				if(_dialog != null) {
					_dialog.setVisible(false);;
				}
			} else {
				JOptionPane.showMessageDialog(null, "No search key provided");
			}
		}
	}	
}
