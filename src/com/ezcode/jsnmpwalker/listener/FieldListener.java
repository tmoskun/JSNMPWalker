package com.ezcode.jsnmpwalker.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.text.JTextComponent;

import com.ezcode.jsnmpwalker.menu.FieldPopupMenu;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class FieldListener extends MouseAdapter {
	private JTextComponent _field;
	private FieldPopupListener _fieldPopupListener;
	private boolean _active = true;
	
	public FieldListener(JTextComponent field, FieldPopupListener fieldPopupListener) {
		this(field, fieldPopupListener, true);
	}
	
	public FieldListener(JTextComponent field, FieldPopupListener fieldPopupListener, boolean active) {
		_field = field;
		_fieldPopupListener = fieldPopupListener;
		_active = active;
	}
	
	public void setActive(boolean active) {
		_active = active;
	}
	
	private void FieldPopupEvent(MouseEvent event) {
		if(_active) {
			int x = event.getX();
			int y = event.getY();
			FieldPopupMenu popup = new FieldPopupMenu(_field, _fieldPopupListener);
			popup.buildMenu();
			popup.show(_field, x, y);
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			FieldPopupEvent(e);
		} 
	}
	
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			FieldPopupEvent(e);
		} 
	}
	

}
