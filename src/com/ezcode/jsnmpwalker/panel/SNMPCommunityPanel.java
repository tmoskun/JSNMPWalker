package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.listener.FieldListener;
import com.ezcode.jsnmpwalker.listener.FieldPopupListener;
import com.ezcode.jsnmpwalker.listener.OptionFieldListener;
import com.ezcode.jsnmpwalker.target.FieldDropTarget;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPCommunityPanel extends JPanel {
	private JComboBox _methodCombo = null;
	private JTextField _ipField = null;
	//private JTextField _oidField = null;
	private JTextArea _oidArea = null;
	private JTextField _community;
	private SNMPOptionModel _optionModel;
	private FieldPopupListener _fieldPopupListener;
	
	public SNMPCommunityPanel(FieldPopupListener fieldPopupListener) {
		this(new SNMPOptionModel(), fieldPopupListener);
	}
	
	public SNMPCommunityPanel(SNMPOptionModel optionModel, FieldPopupListener fieldPopupListener) {
		_optionModel = optionModel;
		_fieldPopupListener = fieldPopupListener;
		init(true);
	}
	
	public SNMPCommunityPanel(SNMPOptionModel optionModel, String command, String ip, List<String> oids, FieldPopupListener fieldPopupListener, boolean editMethod) {
		_optionModel = optionModel;
		_fieldPopupListener = fieldPopupListener;
		_methodCombo = new JComboBox(SNMPTreeData.METHODS);
		_methodCombo.setSelectedItem(command);
		_ipField = new JTextField(ip);
		_ipField.setPreferredSize(PanelUtils.FIELD_DIM);
		
		_oidArea = new JTextArea();
		StringBuffer buff = new StringBuffer();
		PanelUtils.appendWithLineBreak(buff, oids);
		_oidArea.append(buff.toString().trim());
		_oidArea.setBorder(PanelUtils.UI_DEFAULTS.getBorder("TextField.border"));
		_oidArea.setToolTipText("Values separated by comma, semicolumn or new line");

		init(editMethod);
	}
	
	public SNMPCommunityPanel(SNMPOptionModel optionModel, String command, String ip, List<String> oids, FieldPopupListener fieldPopupListener) {
		this(optionModel, command, ip, oids, fieldPopupListener, true);
	}
	
	public SNMPCommunityPanel(SNMPTreeData data, FieldPopupListener fieldPopupListener) {
		this((SNMPOptionModel) data.getOptionModel(), data.getCommand(), data.getIp(), data.getOids(), fieldPopupListener);
	}
	
	
	public void init(boolean editMethod) {
		int rownum = 1;
		setLayout(new SpringLayout());
		setBorder(PanelUtils.DIALOG_BORDER);
		
		if(_methodCombo != null) {
			rownum++;
			final JLabel methLabel = new JLabel("SNMP method: ", JLabel.TRAILING);
			add(methLabel);
			add(_methodCombo);
			methLabel.setLabelFor(_methodCombo);
			_methodCombo.setEnabled(editMethod);
		}

		if(_ipField != null) {
			rownum++;
			final JLabel ipLabel = new JLabel("IP: ", JLabel.TRAILING);
			add(ipLabel);
			add(_ipField);
			ipLabel.setLabelFor(_ipField);
			_ipField.addMouseListener(new FieldListener(_ipField, _fieldPopupListener));
			_ipField.setDropTarget(new FieldDropTarget(_ipField));
		}
		
		if(_oidArea != null) {
			rownum++;
			final JLabel oidLabel = new JLabel("OID(s): ", JLabel.TRAILING);
			add(oidLabel);
			_oidArea.setLineWrap(true);
			JScrollPane sp = new JScrollPane(_oidArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setPreferredSize(PanelUtils.AREA_DIM);
			add(sp);
			oidLabel.setLabelFor(_oidArea);
			_oidArea.addMouseListener(new FieldListener(_oidArea, _fieldPopupListener));
			_oidArea.setDropTarget(new FieldDropTarget(_oidArea));
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
		String oids = _oidArea.getText().trim();
		if(oids == null || oids.length() == 0)
			return new String[0];
		return oids.split("[,;\\s]+");
	}
	
	public void showCommunity(String version) {
		_community.setEnabled(!version.equalsIgnoreCase(SNMPOptionModel.SNMP_VERSION_3));
	}
	
}
