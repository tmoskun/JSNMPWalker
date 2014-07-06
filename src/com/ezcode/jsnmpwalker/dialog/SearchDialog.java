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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.layout.WrapLayout;
import com.ezcode.jsnmpwalker.listener.SearchListener;
import com.ezcode.jsnmpwalker.panel.Searchable;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SearchDialog extends JDialog {
	//private Searchable _searchable;
	private JTextField _searchField;
	private SearchListener _searchListener;
	private JCheckBox _regexCheckBox;
	private JButton _searchButton;
	
	public SearchDialog(Frame frame) {
		super(frame, true);
		this.setLayout(new BorderLayout());
		//_searchable = searchable;
		setTitle("Search");
	    setSize(500, 150);
		setLocationRelativeTo(frame);
		init();
	}
	
	private void init() {
		final JPanel searchPane = new JPanel(new WrapLayout(FlowLayout.LEFT));
		_searchField = new JTextField();
		_searchField.setPreferredSize(new Dimension(PanelUtils.FIELD_WIDTH, 20));
		_regexCheckBox = new JCheckBox("Regex");
		
		_searchListener = new SearchListener(_searchField, _regexCheckBox, this);
		_searchField.addActionListener(_searchListener);
		
		searchPane.add(_searchField);
		searchPane.add(_regexCheckBox);
		
		this.add(searchPane, BorderLayout.CENTER);
		
		final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		_searchButton = new JButton(PanelUtils.TEXT_SEARCH);
		_searchButton.setMnemonic(KeyEvent.VK_S);
		_searchButton.addActionListener(_searchListener);
		buttonPane.add(_searchButton);
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SearchDialog.this.dispose();
			}
		});
		buttonPane.add(cancelButton);
		add(buttonPane, BorderLayout.SOUTH);
		
		//set keystrokes for action buttons
		_searchButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "Save");
		_searchButton.getActionMap().put("Save", new ButtonAction(this, _searchButton));
		
		cancelButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "Cancel");
		cancelButton.getActionMap().put("Cancel", new ButtonAction(this, cancelButton));
		
	}
	
	
	public void setSearchable(Searchable searchable) {
		_searchListener.setSearchable(searchable);
	}
	
	
}
