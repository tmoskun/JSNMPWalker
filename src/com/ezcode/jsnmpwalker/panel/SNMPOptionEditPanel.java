package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.tree.TreeModel;

import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.dialog.CommandDialog;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPOptionEditPanel extends JPanel {
	private static final String GENERAL_TAB = "General";
	private static final String SECURITY_TAB = "Security";
	
	private SNMPTreePanel _treePanel;
	private SNMPDeviceData _deviceData;
	private SNMPOptionModel _optionModel;
	private JTabbedPane _tp;
	
	
	public SNMPOptionEditPanel(SNMPTreePanel treePanel, SNMPDeviceData data) {
		_treePanel = treePanel;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		_deviceData = data;
		_optionModel = new SNMPOptionModel(data.getOptions());
		_tp = new JTabbedPane(JTabbedPane.TOP);
		_tp.addTab(GENERAL_TAB, getGeneralPanel());
		_tp.addTab(SECURITY_TAB, getSecurityPanel());
		showSecurityTab(_optionModel.get(SNMPOptionModel.SNMP_VERSION_KEY));
		add(_tp, BorderLayout.CENTER);
		final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JButton saveButt = new JButton("Save");
		saveButt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(save()) {
					JOptionPane.showMessageDialog(SNMPOptionEditPanel.this, "SNMP options saved");
				}
			}
			
		});
		final JButton restoreButt = new JButton("Restore defaults");
		restoreButt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_optionModel = new SNMPOptionModel();
				_tp.setComponentAt(0, getGeneralPanel());
				_tp.setComponentAt(1, getSecurityPanel());
				showSecurityTab(_optionModel.get(SNMPOptionModel.SNMP_VERSION_KEY));
				JOptionPane.showMessageDialog(SNMPOptionEditPanel.this, "Defaults restored");
			}
			
		});
		
		final JButton closeButt = new JButton("Close");
		closeButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_treePanel.clearSNMPOptionPanel();	
			}
		});
		
		buttonPane.add(saveButt);
		buttonPane.add(restoreButt);
		buttonPane.add(closeButt);
		add(buttonPane, BorderLayout.SOUTH);
	}
	
	private JPanel getGeneralPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel topPanel = new JPanel(new SpringLayout());
	
		final SNMPCommunityPanel commPanel = new SNMPCommunityPanel(_optionModel, _treePanel.getFieldPopupListener());
		
		topPanel.add(commPanel);

		//JPanel bottomPanel = new JPanel(new BorderLayout());
		topPanel.add(new SNMPVersionPanel(_optionModel, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton butt = (JRadioButton) e.getSource();
				String version = butt.getText();
				showSecurityTab(version);
				commPanel.showCommunity(version);
			}
		}));
		topPanel.add(new SNMPOptionPanel(_optionModel));
	    SpringUtilities.makeCompactGrid(topPanel, //parent
                3, 1,
                5, 5,  //initX, initY
                10, 10); //xPad, yPad

		panel.add(topPanel, BorderLayout.NORTH);
	
		return panel; 
	}
	
	private JPanel getSecurityPanel() {
		return new SNMPSecurityPanel(_optionModel, _treePanel.getFieldPopupListener());
	}
	
	private void showSecurityTab(String version) {
		int securityIndex = _tp.indexOfTab(SECURITY_TAB);
		if(version.equalsIgnoreCase(SNMPOptionModel.SNMP_VERSION_3)) {
			_tp.setEnabledAt(securityIndex, true);
		} else {
			_tp.setEnabledAt(securityIndex, false);
		}
	}
	
	private boolean save() {
		if(PanelUtils.validate(this, _optionModel)) {
			_deviceData.setOptions(_optionModel);
			return true;
	    }
	    return false;
	}
	

}
