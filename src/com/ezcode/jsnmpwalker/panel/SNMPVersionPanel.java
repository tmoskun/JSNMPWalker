package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.listener.SNMPRadioButtonListener;

public class SNMPVersionPanel extends JPanel {
	private SNMPOptionModel _optionModel;
	
	public SNMPVersionPanel(SNMPOptionModel optionModel, ActionListener versionListener) {
		_optionModel = optionModel;
		init(versionListener);
	}
	
	private void init(ActionListener verListener) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		add(new JLabel("SNMP version: "));
		ButtonGroup vergroup = new ButtonGroup();
		
		String snmpverdefault = _optionModel.get(SNMPOptionModel.SNMP_VERSION_KEY);
		JRadioButton v1 = new JRadioButton(SNMPOptionModel.SNMP_VERSION_1, snmpverdefault.equals(SNMPOptionModel.SNMP_VERSION_1));
		JRadioButton v2c = new JRadioButton(SNMPOptionModel.SNMP_VERSION_2c, snmpverdefault.equals(SNMPOptionModel.SNMP_VERSION_2c));
		JRadioButton v3 = new JRadioButton(SNMPOptionModel.SNMP_VERSION_3, snmpverdefault.equals(SNMPOptionModel.SNMP_VERSION_3));
		SNMPRadioButtonListener radioListener = new SNMPRadioButtonListener(_optionModel, SNMPOptionModel.SNMP_VERSION_KEY);
		v1.addActionListener(radioListener);
		v2c.addActionListener(radioListener);
		v3.addActionListener(radioListener);
		
		v1.addActionListener(verListener);
		v2c.addActionListener(verListener);
		v3.addActionListener(verListener);
		
		vergroup.add(v1);
		vergroup.add(v2c);
		vergroup.add(v3);	
		add(v1);
		add(v2c);
		add(v3);
	}

}
