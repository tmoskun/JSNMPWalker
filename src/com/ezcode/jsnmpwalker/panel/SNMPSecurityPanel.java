package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.listener.FieldListener;
import com.ezcode.jsnmpwalker.listener.FieldPopupListener;
import com.ezcode.jsnmpwalker.listener.OptionComboListener;
import com.ezcode.jsnmpwalker.listener.OptionFieldListener;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPSecurityPanel extends JPanel {
    final private SNMPOptionModel _optionModel;
    final private JTextField _engineid;
    final private JCheckBox _enableEngineDiscovery;
	final private JTextField _name;
	final private JTextField _context;
	final private JComboBox _level;
	final private JTextField _authPass;
	final private JComboBox _authType;
	final private JTextField _privPass;
	final private JComboBox _privType;
	
	public SNMPSecurityPanel(SNMPOptionModel optionModel, FieldPopupListener fieldPopupListener) {
		_optionModel = optionModel;
	
		setLayout(new SpringLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel engineidLabel = new JLabel("Engine ID: ", JLabel.TRAILING);
		add(engineidLabel);
		_engineid = new SecurityTextField(_optionModel.get(SNMPOptionModel.ENGINE_ID_KEY));
		_engineid.setPreferredSize(PanelUtils.FIELD_DIM);
		add(_engineid);
		engineidLabel.setLabelFor(_engineid);
		_engineid.addMouseListener(new FieldListener(_engineid, fieldPopupListener));
		_engineid.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _engineid, SNMPOptionModel.ENGINE_ID_KEY));
		
		//to fill up the cell
		JLabel empty = new JLabel("");
		add(empty);
		_enableEngineDiscovery = new JCheckBox("Enable Engine Discovery", Boolean.valueOf(_optionModel.get(SNMPOptionModel.ENABLE_ENGINE_DISCOVERY_KEY)));
		add(_enableEngineDiscovery);
		_enableEngineDiscovery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean isSelected = _enableEngineDiscovery.isSelected();
				_engineid.setEnabled(!isSelected);
				_optionModel.put(SNMPOptionModel.ENABLE_ENGINE_DISCOVERY_KEY, String.valueOf(isSelected));
			}
		});
		_engineid.setEnabled(!_enableEngineDiscovery.isSelected());
		
		JLabel nameLabel = new JLabel("Security Name: ", JLabel.TRAILING);
		add(nameLabel);
		_name = new SecurityTextField(_optionModel.get(SNMPOptionModel.SECURITY_NAME_KEY));
		_name.setPreferredSize(PanelUtils.FIELD_DIM);
		add(_name);
		nameLabel.setLabelFor(_name);
		_name.addMouseListener(new FieldListener(_name, fieldPopupListener));
		_name.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _name, SNMPOptionModel.SECURITY_NAME_KEY));
		
	    JLabel contextLabel = new JLabel("Context (optional): ", JLabel.TRAILING);
	    add(contextLabel);
	    _context = new SecurityTextField(_optionModel.get(SNMPOptionModel.CONTEXT_NAME_KEY));
	    _context.setPreferredSize(PanelUtils.FIELD_DIM);
	    add(_context);
	    contextLabel.setLabelFor(_context);
	    _context.addMouseListener(new FieldListener(_context, fieldPopupListener));
	    _context.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _context, SNMPOptionModel.CONTEXT_NAME_KEY));
		
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
	    
	    JLabel authPassLabel = new JLabel("Auth Passphrase: ", JLabel.TRAILING);
	    add(authPassLabel);
	    _authPass = new SecurityTextField(_optionModel.get(SNMPOptionModel.AUTH_PASSPHRASE_KEY));
	    _authPass.setPreferredSize(PanelUtils.FIELD_DIM);
	    add(_authPass);
	    authPassLabel.setLabelFor(_authPass);
	    _authPass.addMouseListener(new FieldListener(_authPass, fieldPopupListener));
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
	    _privPass = new SecurityTextField(_optionModel.get(SNMPOptionModel.PRIV_PASSPHRASE_KEY));
	    _privPass.setPreferredSize(PanelUtils.FIELD_DIM);
	    add(_privPass);
	    privPassLabel.setLabelFor(_privPass);
	    _privPass.addMouseListener(new FieldListener(_privPass, fieldPopupListener));
	    _privPass.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _privPass, SNMPOptionModel.PRIV_PASSPHRASE_KEY));
	    
	    JLabel privTypeLabel = new JLabel("Privacy Type: ", JLabel.TRAILING);
	    add(privTypeLabel);
	    _privType = new JComboBox(SNMPOptionModel.PRIV_TYPES);
	    _privType.setSelectedItem(_optionModel.get(SNMPOptionModel.PRIV_TYPE_KEY));
	    add(_privType);
	    privTypeLabel.setLabelFor(_privType);
	    _privType.addActionListener(new OptionComboListener(_optionModel, SNMPOptionModel.PRIV_TYPE_KEY));
	    
	    SpringUtilities.makeCompactGrid(this, //parent
	            9, 2,
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
	
	private class SecurityTextField extends JTextField {
		
		public SecurityTextField(String str) {
			super(str);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			MouseListener[] listeners = this.getListeners(MouseListener.class);
			for(MouseListener lis: listeners) {
				if(lis instanceof FieldListener) {
					((FieldListener) lis).setActive(enabled);
				}
			}
		}
	}
	
}
