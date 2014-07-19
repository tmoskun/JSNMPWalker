package com.ezcode.jsnmpwalker.dialog;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.listener.FieldPopupListener;
import com.ezcode.jsnmpwalker.panel.SNMPCommunityPanel;
import com.ezcode.jsnmpwalker.panel.SNMPOptionPanel;
import com.ezcode.jsnmpwalker.panel.SNMPSecurityPanel;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.panel.SNMPVersionPanel;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class CommandDialog extends JDialog  {
	private static final String GENERAL_TAB = "General";
	private static final String OPTIONS_TAB = "Options";
	private static final String SECURITY_TAB = "Security";
	private static final String[] TABS = {GENERAL_TAB, OPTIONS_TAB, SECURITY_TAB};
	
	private SNMPTreePanel _treePanel;
	private TreePath _path = null;
	private SNMPOptionModel _optionModel;
	private JPanel _dataPane;
	private JTabbedPane _tp;
	//private boolean _editing = false;
	//private boolean _fieldPopupListener;
	
	public CommandDialog(Frame frame, String title,  SNMPTreePanel treePanel) {
		this(frame, title, treePanel, "", "", new ArrayList<String>(), new SNMPOptionModel(), null);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String ip) {
		this(frame, title, treePanel, "", ip, new ArrayList<String>(), new SNMPOptionModel(), null);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String[] oids) {
		this(frame, title, treePanel, "", "", oids, new SNMPOptionModel(), null);
	}
	
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, SNMPDeviceData data, TreePath path) {
		this(frame, title, treePanel, "", data.getIp(), new ArrayList<String>(), (SNMPOptionModel) data.getOptions(), path);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String command, SNMPDeviceData data, String[] oids, TreePath path) {
		this(frame, title, treePanel, command, data.getIp(), oids, (SNMPOptionModel) data.getOptions(), path);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String command, String ip, String[] oids, SNMPOptionModel optionModel, TreePath path) {
		this(frame, title, treePanel, command, ip, Arrays.asList(oids), optionModel, path);
	}
	
	public CommandDialog(Frame frame, String title, SNMPTreePanel treePanel, String command, String ip, List<String> oids, SNMPOptionModel optionModel, TreePath path) {
		super(frame, false);
		_treePanel = treePanel;
		_path = path;
		_optionModel = optionModel;
		init(title, command, ip, oids);
	}
	
	
	private void init(String title, String command, String ip, List<String> oids) {
		setLayout(new BorderLayout());
		setTitle(title);
		_tp = new JTabbedPane(JTabbedPane.TOP);
		_tp.addTab(GENERAL_TAB, getGeneralPanel(command, ip, oids, _treePanel.getFieldPopupListener()));
		_tp.addTab(OPTIONS_TAB, getOptionsPanel());
		_tp.addTab(SECURITY_TAB, getSecurityPanel(_treePanel.getFieldPopupListener()));
		
		showSecurityTab(_optionModel.get(SNMPOptionModel.SNMP_VERSION_KEY));
		add(_tp, BorderLayout.CENTER);
		final JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JButton saveButt = new JButton("Save");
		//saveButt.setMnemonic(KeyEvent.VK_S);
		saveButt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(save())
					CommandDialog.this.dispose();	
			}
			
		});
		buttonPane.add(saveButt);
		final JButton cancelButt = new JButton("Cancel");
		//cancelButt.setMnemonic(KeyEvent.VK_C);
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
		//saveButt.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "Save");
		//saveButt.getActionMap().put("Save", new ButtonAction(this, saveButt));
		
		//cancelButt.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "Cancel");
		//cancelButt.getActionMap().put("Cancel", new ButtonAction(this, cancelButt));
		
		setSize(500, 350);
		setLocationRelativeTo(null);
	}
	
	private JPanel getGeneralPanel(String command, String ip, List<String> oids, FieldPopupListener fieldPopupListener) {
		final JPanel panel = new JPanel(new BorderLayout());
		_dataPane = new SNMPCommunityPanel(_optionModel, command, ip, oids, fieldPopupListener, (_path == null));
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
	
	private JPanel getSecurityPanel(FieldPopupListener fieldPopupListener) {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel secPane = new SNMPSecurityPanel(_optionModel, fieldPopupListener);
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
		List<String> oidList = Arrays.asList(panel.getOids());
		Set<String> oids = new HashSet<String>();
		oids.addAll(oidList);
		boolean editing = (_path != null);
		TreeModel treeModel = editing ? null : _treePanel.getTree().getModel();
		if(PanelUtils.validate(this, method, deviceData, oids, _optionModel, treeModel)) {
			if(editing) {
				_treePanel.editSNMP(_path, deviceData, oids);
			} else {
				_treePanel.createSNMP(new Object[] {method, deviceData, oids});
			}
			if(oidList.size() > oids.size()) {
				JOptionPane.showMessageDialog(this, "Duplicate OIDs will be removed");
			}
			return true;
	    }
	    return false;
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		_treePanel.enableCommandButtons(!visible);
	}
	@Override
	public void dispose() {
		super.dispose();
		_treePanel.enableCommandButtons(true);
	}

}
