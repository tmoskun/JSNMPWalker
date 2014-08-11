package com.ezcode.jsnmpwalker.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.listener.TextSearchListener;
import com.ezcode.jsnmpwalker.utils.PanelUtils;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class ButtonDialog extends JDialog {
	
	private final JButton _searchButton;
	
	public ButtonDialog(Frame frame, int width) {
		super(frame, true);
		this.setLayout(new BorderLayout());
		//_searchable = searchable;
	    setSize(width, 150);
		setLocationRelativeTo(frame);
		
		final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		_searchButton = new JButton(PanelUtils.TEXT_SEARCH);
		_searchButton.setMnemonic(KeyEvent.VK_S);
		buttonPane.add(_searchButton);
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ButtonDialog.this.dispose();
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
	
	protected void setListener(ActionListener searchListener) {
		_searchButton.addActionListener(searchListener);
	}
	

}
