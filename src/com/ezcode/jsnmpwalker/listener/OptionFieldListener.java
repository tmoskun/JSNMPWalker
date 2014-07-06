package com.ezcode.jsnmpwalker.listener;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;

public class OptionFieldListener implements DocumentListener {
	
	private JTextField _field;
	private String _key;
	private SNMPOptionModel _optionModel;
	
	public OptionFieldListener(SNMPOptionModel optionModel, JTextField field, String key) {
		_optionModel = optionModel;
		_field = field;
		_key = key;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		update();	
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		update();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		update();
	}
	
	private void update() {
		_optionModel.put(_key, _field.getText());
	}

}
