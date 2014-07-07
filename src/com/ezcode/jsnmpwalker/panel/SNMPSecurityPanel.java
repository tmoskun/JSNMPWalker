package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.listener.OptionComboListener;
import com.ezcode.jsnmpwalker.listener.OptionFieldListener;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPSecurityPanel extends JPanel {
    final private SNMPOptionModel _optionModel;
	final private JTextField _name;
	final private JComboBox _level;
	final private JTextField _authPass;
	final private JComboBox _authType;
	final private JTextField _privPass;
	final private JComboBox _privType;
	
	public SNMPSecurityPanel(SNMPOptionModel optionModel) {
		_optionModel = optionModel;
	
		setLayout(new SpringLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel nameLabel = new JLabel("Security Name: ", JLabel.TRAILING);
		add(nameLabel);
		_name = new JTextField(_optionModel.get(SNMPOptionModel.SECURITY_NAME_KEY));
		_name.setPreferredSize(PanelUtils.FIELD_DIM);
		add(_name);
		nameLabel.setLabelFor(_name);
		_name.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _name, SNMPOptionModel.SECURITY_NAME_KEY));
		
	    JLabel levelLabel = new JLabel("Security Level: ", JLabel.TRAILING);
	    add(levelLabel);
	    _level = new JComboBox(SNMPOptionModel.SECURITY_LEVELS);
	    _level.setSelectedItem(_optionModel.get(SNMPOptionModel.SECURITY_LEVEL_KEY));
	    add(_level);
	    levelLabel.setLabelFor(_level);
	    _level.addActionListener(new OptionComboListener(_optionModel, SNMPOptionModel.SECURITY_LEVEL_KEY));
	    _level.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSecurityControls();
			}
	    });
	    
	    JLabel authPassLabel = new JLabel("Authentication Passphrase: ", JLabel.TRAILING);
	    add(authPassLabel);
	    _authPass = new JTextField(_optionModel.get(SNMPOptionModel.AUTH_PASSPHRASE_KEY));
	    _authPass.setPreferredSize(PanelUtils.FIELD_DIM);
	    add(_authPass);
	    authPassLabel.setLabelFor(_authPass);
	    _authPass.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _authPass, SNMPOptionModel.AUTH_PASSPHRASE_KEY));
	
	    JLabel authTypeLabel = new JLabel("Authentication Type: ", JLabel.TRAILING);
	    add(authTypeLabel);
	    _authType = new JComboBox(SNMPOptionModel.AUTH_TYPES);
	    _authType.setSelectedItem(_optionModel.get(SNMPOptionModel.AUTH_TYPE_KEY));
	    add(_authType);
	    authTypeLabel.setLabelFor(_authType);
	    _authType.addActionListener(new OptionComboListener(_optionModel, SNMPOptionModel.AUTH_TYPE_KEY));
	
	    JLabel privPassLabel = new JLabel("Privacy Passphrase: ", JLabel.TRAILING);
	    add(privPassLabel);
	    _privPass = new JTextField(_optionModel.get(SNMPOptionModel.PRIV_PASSPHRASE_KEY));
	    _privPass.setPreferredSize(PanelUtils.FIELD_DIM);
	    add(_privPass);
	    privPassLabel.setLabelFor(_privPass);
	    _privPass.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _privPass, SNMPOptionModel.PRIV_PASSPHRASE_KEY));
	    
	    JLabel privTypeLabel = new JLabel("Privacy Type: ", JLabel.TRAILING);
	    add(privTypeLabel);
	    _privType = new JComboBox(SNMPOptionModel.PRIV_TYPES);
	    _privType.setSelectedItem(_optionModel.get(SNMPOptionModel.PRIV_TYPE_KEY));
	    add(_privType);
	    privTypeLabel.setLabelFor(_privType);
	    _privType.addActionListener(new OptionComboListener(_optionModel, SNMPOptionModel.PRIV_TYPE_KEY));
	    
	    SpringUtilities.makeCompactGrid(this, //parent
	            6, 2,
	            5, 5,  //initX, initY
	            10, 10); //xPad, yPad
	    
	    showSecurityControls();
	}
	
	
	private void showSecurityControls() {
		String level = (String) _level.getSelectedItem();
		if(level.equalsIgnoreCase(SNMPOptionModel.SECURITY_LEVEL_NOAUTH_NOPRIV)) {
			_authPass.setEnabled(false);
			_authType.setEnabled(false);
			_privPass.setEnabled(false);
			_privType.setEnabled(false);
		} else if(level.equalsIgnoreCase(SNMPOptionModel.SECURITY_LEVEL_AUTH_NOPRIV)) {
			_authPass.setEnabled(true);
			_authType.setEnabled(true);
			_privPass.setEnabled(false);
			_privType.setEnabled(false);
		} else if(level.equalsIgnoreCase(SNMPOptionModel.SECURITY_LEVEL_AUTH_PRIV)) {
			_authPass.setEnabled(true);
			_authType.setEnabled(true);
			_privPass.setEnabled(true);
			_privType.setEnabled(true);
		}
	}
	
}
