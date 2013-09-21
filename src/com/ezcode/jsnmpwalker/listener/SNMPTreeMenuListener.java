package com.ezcode.jsnmpwalker.listener;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.menu.SNMPPopupMenu;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class SNMPTreeMenuListener extends MouseAdapter {
	private SNMPTreePanel _panel;
	
	public SNMPTreeMenuListener(SNMPTreePanel panel) {
		_panel = panel;
	}
	
	private void executeCommand(String command, SNMPPopupMenu menu, String str) {
		if(command.startsWith("Add")) {
			_panel.addNodes(str);
		} else if(command.startsWith("Edit")) {
			_panel.editNode(str);
		} else if(command.startsWith("Delete")) {
			_panel.removeNodes();
		} else if(command.startsWith("Cut")) {
			_panel.cutNodes();
		} else if(command.startsWith("Copy")) {
			_panel.copyData();
		} else if(command.startsWith("Paste")) {
			_panel.pasteData();
		} else if(command.startsWith("Insert")) {
			_panel.insertData();
		} else if(command.startsWith("Translate")) {
			_panel.translateData();
		}
	}
		
	public void mousePressed(MouseEvent event) {
		JMenuItem item = (JMenuItem)event.getSource();
		JPopupMenu parent = (JPopupMenu) item.getParent();
		if(parent instanceof SNMPPopupMenu) {
			executeCommand(item.getText(), (SNMPPopupMenu) parent, null);
		} else {
			String str = item.getText();
			JMenu invoker = (JMenu)parent.getInvoker();
			JPopupMenu parent2 = (JPopupMenu) invoker.getParent();
			if(parent2 instanceof SNMPPopupMenu) {
				executeCommand(invoker.getText(), (SNMPPopupMenu) parent2, str);
			}
		}
	}
}
