package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.JTextComponent;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.listener.OptionFieldListener;
import com.ezcode.jsnmpwalker.listener.SNMPRadioButtonListener;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPCommunityPanel extends JPanel {
	private JComboBox<String> _methodCombo = null;
	private JTextField _ipField = null;
	//private JTextField _oidField = null;
	private JTextArea _oidArea = null;
	private JTextField _community;
	private SNMPOptionModel _optionModel;
	
	public SNMPCommunityPanel() {
		_optionModel = new SNMPOptionModel();
		init();
	}
	
	public SNMPCommunityPanel(SNMPOptionModel optionModel) {
		_optionModel = optionModel;
		init();
	}
	
	public SNMPCommunityPanel(SNMPOptionModel optionModel, String command, String ip, List<String> oids) {
		_optionModel = optionModel;
		_methodCombo = new JComboBox<String>(SNMPTreeData.METHODS);
		_methodCombo.setSelectedItem(command);
		_ipField = new JTextField(ip);
		_ipField.setPreferredSize(PanelUtils.FIELD_DIM);
		
		_oidArea = new JTextArea();
		for(String oid: oids) {
			_oidArea.append(oid);
			_oidArea.append("\n");
		}
		_oidArea.setBorder(PanelUtils.UI_DEFAULTS.getBorder("TextField.border"));
		_oidArea.setPreferredSize(PanelUtils.AREA_DIM);
		_oidArea.setToolTipText("Values separated by comma, semicolumn or new line");

		init();
	}
	
	public SNMPCommunityPanel(SNMPTreeData data) {
		this((SNMPOptionModel) data.getOptionModel(), data.getCommand(), data.getIp(), data.getOids());
	}
	
	
	public void init() {
		int rownum = 1;
		setLayout(new SpringLayout());
		setBorder(PanelUtils.DIALOG_BORDER);
		
		if(_methodCombo != null) {
			rownum++;
			final JLabel methLabel = new JLabel("SNMP method: ", JLabel.TRAILING);
			add(methLabel);
			add(_methodCombo);
			methLabel.setLabelFor(_methodCombo);
		}

		if(_ipField != null) {
			rownum++;
			final JLabel ipLabel = new JLabel("IP: ", JLabel.TRAILING);
			add(ipLabel);
			add(_ipField);
			ipLabel.setLabelFor(_ipField);
		}
		
		if(_oidArea != null) {
			rownum++;
			final JLabel oidLabel = new JLabel("OID(s): ", JLabel.TRAILING);
			add(oidLabel);
			add(_oidArea);
			oidLabel.setLabelFor(_oidArea);
		}
		
		final JLabel commLabel = new JLabel("Community: ", JLabel.TRAILING);
		add(commLabel);
		_community = new JTextField(_optionModel.get(SNMPOptionModel.COMMUNITY_KEY));
		_community.setPreferredSize(PanelUtils.FIELD_DIM);
		add(_community);
		commLabel.setLabelFor(_community);
		_community.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, _community, SNMPOptionModel.COMMUNITY_KEY));
		  
		SpringUtilities.makeCompactGrid(this, //parent
                rownum, 2,
                5, 5,  //initX, initY
                10, 10); //xPad, yPad
		
		showCommunity(_optionModel.get(SNMPOptionModel.SNMP_VERSION_KEY));
	
	}
	
	public String getCommand() {
		return (String) _methodCombo.getSelectedItem();
	}
	
	public String getIp() {
		return _ipField.getText();
	}
	
	public String[] getOids() {
		return _oidArea.getText().split("[,;\n]");
	}
	
	public void showCommunity(String version) {
		_community.setEnabled(!version.equalsIgnoreCase(SNMPOptionModel.SNMP_VERSION_3));
	}
	

}
