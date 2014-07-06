package com.ezcode.jsnmpwalker.listener;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;

public class SNMPRadioButtonListener implements ActionListener {
	private String _key;
	private SNMPOptionModel _optionModel;
	
	public SNMPRadioButtonListener(SNMPOptionModel optionModel, String k) {
		_optionModel = optionModel;
		_key = k;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		String str = "";
		if(obj instanceof JRadioButton) {
			str = ((JRadioButton) obj).getText();
		} else if(obj instanceof JRadioButtonMenuItem) {
			str = ((JRadioButtonMenuItem) obj).getText();
		}
		_optionModel.put(_key, str);

	}

}
