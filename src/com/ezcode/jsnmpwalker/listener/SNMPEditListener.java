package com.ezcode.jsnmpwalker.listener;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class SNMPEditListener implements ActionListener {
	private SNMPTreePanel _panel;
	
	public SNMPEditListener(SNMPTreePanel panel) {
		_panel = panel;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Object obj = event.getSource();
		if(obj instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) obj;
			String title = item.getText();
			if(title.startsWith("Undo")) {
				_panel.undo();
			} else if(title.startsWith("Redo")) {
				_panel.redo();
			} else if(title.startsWith("Delete")) {
				_panel.removeNodes();
			} else if(title.startsWith("Cut")) {
				_panel.cutNodes();
			} else if(title.startsWith("Copy")) {
				_panel.copyData();
			} else if(title.startsWith("Paste")) {
				_panel.pasteData();
			} else if(title.startsWith("Insert")) {
				_panel.insertData();
			}
		}
	}

}
