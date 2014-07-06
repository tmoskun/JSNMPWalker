package com.ezcode.jsnmpwalker.utils;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.snmp4j.smi.OctetString;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;

import java.awt.Component;

public class PanelUtils {
	
	public static final Dimension FIELD_DIM = new Dimension(120, 25);
	public static final Dimension AREA_DIM = new Dimension(120, 40);
	public static final Border DIALOG_BORDER = BorderFactory.createEmptyBorder(5, 30, 5, 30);
	final public static UIDefaults UI_DEFAULTS = javax.swing.UIManager.getDefaults();
	
	public static boolean validate(Component parent, String method, SNMPDeviceData deviceData, String[] oids) {
		return validate(parent, method, deviceData, oids, null);
	}
	
	public static boolean validate(Component parent, SNMPOptionModel optionModel) {
		return validate(parent, null, null, null, optionModel, null);
	}
	
	public static boolean validate(Component parent, String method, SNMPDeviceData deviceData, String[] oids, TreeModel treeModel) {
		return validate(parent, method, deviceData, oids, deviceData.getOptions(), null);
	}
	
	
	public static boolean validate(Component parent, String method, SNMPDeviceData deviceData, String[] oids, Map<String, String> optionModel, TreeModel treeModel) {
		String community = optionModel.get(SNMPOptionModel.COMMUNITY_KEY);
		String portNum = optionModel.get(SNMPOptionModel.PORT_KEY);
		String timeout = optionModel.get(SNMPOptionModel.TIMEOUT_KEY);
		String retries = optionModel.get(SNMPOptionModel.RETRIES_KEY);
		String ip = (deviceData == null) ? null : deviceData.getIp();
		if(method != null && method.length() == 0) {
			JOptionPane.showMessageDialog(parent, "SNMP method is not provided");
			return false;
		} else if(ip != null && ip.length() == 0) {
			JOptionPane.showMessageDialog(parent, "IP is not provided");
			return false;
		} else if(oids != null && oids.length == 0) {
			JOptionPane.showMessageDialog(parent, "OID(s) are not provided");
			return false;
		} else if(!SNMPOptionModel.isVersion3(optionModel) && (community != null && community.length() == 0)) {
			JOptionPane.showMessageDialog(parent, "Community name is empty");
			return false;
		} else if(portNum != null && portNum.length() == 0) {
			JOptionPane.showMessageDialog(parent, "Port number is empty");
			return false;
		} else if(timeout != null && timeout.length() == 0) {
			JOptionPane.showMessageDialog(parent, "Timeout is empty");
			return false;
		} else if(retries != null && retries.length() == 0) {
			JOptionPane.showMessageDialog(parent, "Number of retries is empty");
			return false;
		}
		if(SNMPOptionModel.isVersion3(optionModel)) {
			String securityName = optionModel.get(SNMPOptionModel.SECURITY_NAME_KEY);
			String securityLevel = optionModel.get(SNMPOptionModel.SECURITY_LEVEL_KEY);
			String authPass = optionModel.get(SNMPOptionModel.AUTH_PASSPHRASE_KEY);
			String authType = optionModel.get(SNMPOptionModel.AUTH_TYPE_KEY);
			String privPass = optionModel.get(SNMPOptionModel.PRIV_PASSPHRASE_KEY);
			String privType = optionModel.get(SNMPOptionModel.PRIV_TYPE_KEY);
			if(securityName == null || securityName.length() == 0) {
				JOptionPane.showMessageDialog(parent, "Security name is empty");
				return false;
			} else if(securityLevel == null && securityLevel.length() == 0) {
				JOptionPane.showMessageDialog(parent, "Security level is empty");
				return false;
			} else if(SNMPOptionModel.isAuthRequired(securityLevel)) {
				if(authPass == null || authPass.length() == 0) {
					JOptionPane.showMessageDialog(parent, "Authentication passphrase is empty");
					return false;
				} else if(authType == null || authType.length() == 0) {
					JOptionPane.showMessageDialog(parent, "Authentication type is empty");
					return false;
				}
			} else if(SNMPOptionModel.isPrivRequired(securityLevel)) {
				if(privPass == null || privPass.length() == 0) {
					JOptionPane.showMessageDialog(parent, "Privacy passphrase is empty");
					return false;
				} else if(privType == null || privType.length() == 0) {
					JOptionPane.showMessageDialog(parent, "Privacy type is empty");
					return false;
				}
			}
		}
		if(treeModel != null) {
			//check if node exists
	    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
	    	DefaultMutableTreeNode methNode = null;
	    	Enumeration rootChildren = root.children();
	    	while(rootChildren.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootChildren.nextElement();
				String methStr = (String) node.getUserObject();
				if(method.equalsIgnoreCase(methStr)) {
					methNode = node;
					break;
				}
			}
	    	if(methNode == null)
	    		return true;
	    	DefaultMutableTreeNode ipNode = null;
	    	Enumeration methChildren = methNode.children();
			while(methChildren.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) methChildren.nextElement();
				Object ipObj = (Object) node.getUserObject();
				if(ipObj.equals(deviceData)) {
					ipNode = node;
					break;
				}
			}
			if(ipNode == null)
				return true;
	    	DefaultMutableTreeNode oidNode = null;
			Enumeration ipChildren = ipNode.children();
			List<String> oidList = Arrays.asList(oids);
			while(ipChildren.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) ipChildren.nextElement();
				String oidStr = (String) node.getUserObject();
				if(oidList.contains(oidStr)) {
					oidNode = node;
					break;
				}
			}
			if(oidNode == null)
				return true;
			else {
				JOptionPane.showMessageDialog(parent, "This node already exists");
				return false;
			}
			
		}
		return true;
	}

	public static final int FIELD_WIDTH = Math.min(SNMPSessionFrame.WIDTH/6, 300);
	public static final String TEXT_SEARCH = "Search";
	public static final String TEXT_STOP = "Stop";
	public static final Color HILIT_COLOR = Color.YELLOW;
	

}
