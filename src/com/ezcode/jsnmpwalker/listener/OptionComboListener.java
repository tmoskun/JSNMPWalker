package com.ezcode.jsnmpwalker.listener;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;

public class OptionComboListener implements ActionListener {

	private String _key;
	private SNMPOptionModel _optionModel;
	
	public OptionComboListener(SNMPOptionModel optionModel, String key) {
		_optionModel = optionModel;
		_key = key;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			JComboBox comb = (JComboBox) e.getSource();
			String sel = (String) comb.getSelectedItem();
			if(sel != null && sel.length() > 0)
				_optionModel.put(_key, sel);
		} catch (Exception ex) {
			//not a combobox
		}
		
	}

}
