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
	private JCheckBox _regexCheckBox;
	
	private Thread _searcherThread;
	private Dialog _dialog;
	
	public SearchListener(JTextField searchField, JCheckBox regexCheckBox) {
		this(searchField, regexCheckBox, null);
	}
	
	public SearchListener(JTextField searchField, JCheckBox regexCheckBox, Dialog dialog) {
		_searchField = searchField;
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
			boolean isRegex = _regexCheckBox.isSelected();
			if(searchKey.length() > 0 && _searchable != null) {
				_searcherThread = new TextSearcher(_searchable, searchKey, isRegex);
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
