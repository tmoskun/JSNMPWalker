package com.ezcode.jsnmpwalker.dialog;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.layout.WrapLayout;
import com.ezcode.jsnmpwalker.listener.OptionFieldListener;
import com.ezcode.jsnmpwalker.listener.SNMPRadioButtonListener;
import com.ezcode.jsnmpwalker.panel.SNMPCommunityPanel;
import com.ezcode.jsnmpwalker.panel.SNMPOptionPanel;
import com.ezcode.jsnmpwalker.panel.SNMPSecurityPanel;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.panel.SNMPVersionPanel;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class CommandDialog extends JDialog {
	private static final String GENERAL_TAB = "General";
	private static final String OPTIONS_TAB = "Options";
	private static final String SECURITY_TAB = "Security";
	private static final String[] TABS = {GENERAL_TAB, OPTIONS_TAB, SECURITY_TAB};
	
	private SNMPTreePanel _treePanel;
	private SNMPOptionModel _optionModel;
	private JPanel _dataPane;
	private JTabbedPane _tp;
	private boolean _editing = false;
	
	public CommandDialog(Frame frame, String title,  SNMPTreePanel treePanel) {
		super(frame, true);
		_treePanel = treePanel;
		_editing = false;
		_optionModel = new SNMPOptionModel();
		init(title);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String ip) {
		super(frame, true);
		_treePanel = treePanel;
		_editing = false;
		_optionModel = new SNMPOptionModel();
		init(title, ip);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String[] oids) {
		super(frame, true);
		_treePanel = treePanel;
		_editing = false;
		_optionModel = new SNMPOptionModel();
		init(title, oids);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, SNMPTreeData data) {
		super(frame, true);
		_treePanel = treePanel;
		_optionModel = (SNMPOptionModel) data.getOptionModel();
		_editing = true;
		init(title, data.getCommand(), data.getIp(), data.getOids());
	}
	
	private void init(String title) {
		init(title, "", "", new ArrayList<String>());
	}
	
	private void init(String title, String ip) {
		init(title, "", ip, new ArrayList<String>());
	}
	
	private void init(String title, String[] oids) {
		init(title, "", "", Arrays.asList(oids));
	}
	
	
	private void init(String title, String command, String ip, List<String> oids) {
		setLayout(new BorderLayout());
		setTitle(title);
		_tp = new JTabbedPane(JTabbedPane.TOP);
		_tp.addTab(GENERAL_TAB, getGeneralPanel(command, ip, oids));
		_tp.addTab(OPTIONS_TAB, getOptionsPanel());
		_tp.addTab(SECURITY_TAB, getSecurityPanel());
		
		showSecurityTab(_optionModel.get(SNMPOptionModel.SNMP_VERSION_KEY));
		add(_tp, BorderLayout.CENTER);
		final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JButton saveButt = new JButton("Save");
		saveButt.setMnemonic(KeyEvent.VK_S);
		saveButt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(save())
					CommandDialog.this.dispose();	
			}
			
		});
		buttonPane.add(saveButt);
		final JButton cancelButt = new JButton("Cancel");
		cancelButt.setMnemonic(KeyEvent.VK_C);
		cancelButt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//CommandDialog.this.setVisible(false);
				CommandDialog.this.dispose();
			}
			
		});
		buttonPane.add(cancelButt);
		add(buttonPane, BorderLayout.SOUTH);
		
		//set keystrokes for action buttons
		saveButt.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "Save");
		saveButt.getActionMap().put("Save", new ButtonAction(this, saveButt));
		
		cancelButt.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "Cancel");
		cancelButt.getActionMap().put("Cancel", new ButtonAction(this, cancelButt));
		
		setSize(500, 350);
		setLocationRelativeTo(null);
	}
	
	private JPanel getGeneralPanel(String command, String ip, List<String> oids) {
		final JPanel panel = new JPanel(new BorderLayout());
		_dataPane = new SNMPCommunityPanel(_optionModel, command, ip, oids);
		panel.add(_dataPane, BorderLayout.NORTH);
		
		ActionListener verListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JRadioButton butt = (JRadioButton) e.getSource();
				String version = butt.getText();
				showSecurityTab(version);
				((SNMPCommunityPanel)_dataPane).showCommunity(version);
			}
		};
		
		final JPanel versionPane = new SNMPVersionPanel(_optionModel, verListener);
		panel.add(versionPane, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel getOptionsPanel() {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel optPane = new SNMPOptionPanel(_optionModel);
		panel.add(optPane, BorderLayout.NORTH);
		return panel;
	}
	
	private JPanel getSecurityPanel() {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel secPane = new SNMPSecurityPanel(_optionModel);
		panel.add(secPane, BorderLayout.NORTH);
		return panel;
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
		SNMPCommunityPanel panel = (SNMPCommunityPanel)_dataPane;
		String method = panel.getCommand();
		String ip = panel.getIp();
		SNMPDeviceData deviceData = new SNMPDeviceData(ip, _optionModel);
		String[] oids = panel.getOids();
		TreeModel treeModel = _editing ? null : _treePanel.getTree().getModel();
		if(PanelUtils.validate(this, method, deviceData, oids, _optionModel, treeModel)) {
			if(_editing) {
				
			} else {
				_treePanel.createSNMP(new Object[] {method, deviceData, oids});
			}
			return true;
	    }
	    return false;
	}

}
