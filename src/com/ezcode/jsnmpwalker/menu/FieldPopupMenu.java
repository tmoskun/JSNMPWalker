package com.ezcode.jsnmpwalker.menu;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */
import javax.swing.JTextField;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import com.ezcode.jsnmpwalker.listener.FieldPopupListener;


public class FieldPopupMenu extends JPopupMenu {
	public static final String PASTE_ITEM = "Paste";
	public static final String CLEAR_ITEM = "Clear";
	
	private JTextComponent _field;
	private FieldPopupListener _fieldPopupListener;
	
	public FieldPopupMenu(JTextComponent field, FieldPopupListener fieldPopupListener) {
		_field = field;
		_fieldPopupListener = fieldPopupListener;
	}
	
	public void buildMenu() {
		JMenuItem paste = new JMenuItem(PASTE_ITEM);
		paste.addMouseListener(_fieldPopupListener);
		this.add(paste);
		JMenuItem clear = new JMenuItem(CLEAR_ITEM);
		clear.addMouseListener(_fieldPopupListener);
		this.add(clear);
	}
	
	public JTextComponent getField() {
		return _field;
	}
	
}

