package com.ezcode.jsnmpwalker.listener;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.ezcode.jsnmpwalker.search.Searchable;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.search.SearchableTree;
import com.ezcode.jsnmpwalker.utils.PanelUtils;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public abstract class AbstractSearchListener implements ActionListener {
	protected Searchable _searchable;
	protected JTextField _searchField;
	protected Thread _searcherThread;
	protected Dialog _dialog;
	
	public AbstractSearchListener(JTextField searchField, Dialog dialog) {
		_searchField = searchField;
		_dialog = dialog;
	}
	
	public void setSearchable(Searchable searchable) {
		_searchable = searchable;
	}
	
	@Override
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
			if(searchKey.length() > 0 && _searchable != null) {
				startSearch();
				if(_dialog != null) {
					_dialog.setVisible(false);
				}
			} else {
				JOptionPane.showMessageDialog(null, "No search key provided");
			}
		}

	}
	
	public abstract void startSearch();
	

}
