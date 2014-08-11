package com.ezcode.jsnmpwalker.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.ezcode.jsnmpwalker.layout.WrapLayout;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class SearchDialog extends ButtonDialog {
	final private JTextField _searchField;
	final private JPanel _optPanel;

	public SearchDialog(Frame frame, int width, String searchKey) {
		super(frame, width);
		final JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		_searchField = new JTextField(searchKey);
		_searchField.setPreferredSize(new Dimension(PanelUtils.FIELD_WIDTH, 20));
		
		_optPanel = new JPanel(new WrapLayout(FlowLayout.LEFT));
		
		searchPanel.add(_searchField, BorderLayout.NORTH);
		searchPanel.add(_optPanel, BorderLayout.CENTER);
		
		add(searchPanel, BorderLayout.CENTER);
	}
	
	protected void setListener(ActionListener searchListener) {
		super.setListener(searchListener);
		_searchField.addActionListener(searchListener);
	}
	
	protected void addOption(JComponent comp) {
		_optPanel.add(comp);
	}
	
	protected JTextField getSearchField() {
		return _searchField;
	}
	
	
	
}
