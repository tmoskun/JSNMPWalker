package com.ezcode.jsnmpwalker.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ButtonAction extends AbstractAction {
	private Window _window;
	private AbstractButton _butt;
	public ButtonAction(Window window, AbstractButton butt) {
		_window = window;
		_butt = butt;
	}
	public void actionPerformed(ActionEvent e) {
		Component focused = _window.getFocusOwner();
		if(!(focused instanceof JTextField) && !(focused instanceof JComboBox))
			_butt.doClick();			
	}
	
}