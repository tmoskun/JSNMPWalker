package com.ezcode.jsnmpwalker.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.ezcode.jsnmpwalker.menu.FieldPopupMenu;
import com.ezcode.jsnmpwalker.utils.ClipboardUtils;
import com.ezcode.jsnmpwalker.utils.PanelUtils;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class FieldPopupListener extends MouseAdapter {
	
	public void mousePressed(MouseEvent event) {
		JMenuItem item = (JMenuItem)event.getSource();
		String command = item.getText();
		FieldPopupMenu menu = (FieldPopupMenu) item.getParent();
		JTextComponent field = menu.getField();
		if(command.equalsIgnoreCase(FieldPopupMenu.PASTE_ITEM)) {
			paste(field);
		} else if(command.equalsIgnoreCase(FieldPopupMenu.CLEAR_ITEM)) {
			field.setText("");
		}
	}
	
	private void paste(JTextComponent field) {
		Object obj = ClipboardUtils.getClipboardContents();
		List<Object> list = new ArrayList();
		if(obj instanceof List) {
			list = (List) obj; 
		} else if (obj instanceof String){
			//Object[] str = obj.toString().split("\\r?\\n");
			Object[] str = obj.toString().split("[,;\\s]+");
			for(Object s: str) {
				if(s != null && s.toString().length() > 0) {
					list.add(s);
				}
			}
		}
		if(field instanceof JTextArea) {
			StringBuffer buff = new StringBuffer();
			PanelUtils.appendWithLineBreak(buff, list);
			field.setText(buff.toString().trim());
		} else if(field instanceof JTextField){
			if(!list.isEmpty())
				field.setText(list.get(0).toString());
		}
	}

}
