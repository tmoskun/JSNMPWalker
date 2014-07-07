package com.ezcode.jsnmpwalker.data;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.percederberg.mibble.MibLoaderException;

import org.snmp4j.util.SimpleOIDTextFormat;

import com.ezcode.jsnmpwalker.panel.MibTreePanel;


public class SNMPTreeData {
	
	//public static final String[] COMMANDS = {"Get", "GetNext", "GetBulk", "Walk", "Table"};
	public static final String WALK = "Walk";
	public static final String GET_BULK = "GetBulk";
	public static final String GET = "Get";
	public static final String GET_NEXT = "GetNext";
	public static final String[] SINGLE_OID_METHODS = {GET_BULK, WALK};
	public static final String[] MULTI_OID_METHODS = {GET, GET_NEXT};
	public static final String[] METHODS = Arrays.copyOf(MULTI_OID_METHODS, MULTI_OID_METHODS.length + SINGLE_OID_METHODS.length);
	static {
		System.arraycopy(SINGLE_OID_METHODS, 0, METHODS, MULTI_OID_METHODS.length, SINGLE_OID_METHODS.length);
	}
	public static final Pattern MIB_PATTERN = Pattern.compile("^(.+)::(\\w+)((\\.\\d)*)?$");

	private String _command = "";
	private SNMPDeviceData _deviceData;
	private List<String> _oids = new ArrayList<String>();
	
	public SNMPTreeData(String command, SNMPDeviceData deviceData, String oid) {
		_command = command;
		_deviceData = deviceData;
	}
	
	public SNMPTreeData(String command, String ip, SNMPOptionModel optionModel, String oid) {
		_command = command;
		_deviceData = new SNMPDeviceData(ip, optionModel);
		_oids.add(oid);
	}
	
	public SNMPTreeData(String command, String ip, SNMPOptionModel optionModel, List<String> oids) {
		_command = command;
		_deviceData = new SNMPDeviceData(ip, optionModel);
		_oids.addAll(oids);
	}
	
	public SNMPTreeData(TreeNode[] nodes, int commandnum, int ipnum, int oidnum) {
		if(nodes.length >= Math.max(commandnum, Math.max(ipnum, oidnum))) {
			if(nodes[commandnum] instanceof DefaultMutableTreeNode) {
				_command = (String) ((DefaultMutableTreeNode) nodes[commandnum]).getUserObject();
			}
			if(nodes[ipnum] instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode ip = (DefaultMutableTreeNode) nodes[ipnum];
				//_ip = (String) ip.getUserObject();
				_deviceData = (SNMPDeviceData) ip.getUserObject();
			}			
		}
	}
	
		
	//0 - root, 1 - command, 2 - ip, 3 - oid
	public SNMPTreeData(TreeNode[] nodes) {
		this(nodes, 1, 2, 3);
	}
	
	public String getCommand() {
		return _command;
	}

	public void setCommand(String command) {
		_command = command;
	}

	public String getIp() {
		return _deviceData.getIp();
	}

	public void setIp(String ip) {
		_deviceData.setIp(ip);
	}
	
	public Map<String, String> getOptionModel() {
		return _deviceData.getOptions();
	}
	
	public void setOptionModel(Map<String, String> mod) {
		_deviceData.setOptions(mod);
	}
	 
	
	public static boolean isOIDRequired(String method) {
		return !method.equalsIgnoreCase(WALK);
	}
	
/*
	
	public Map<String, String> getOptions() {
		return _optionModel;
	}
	
	public void setOption(String key, String value) {
		_optionModel.put(key, value);
	}
*/

	public List<String> getOids() {
		return _oids;
	}
	
	public String getOid() {
		return _oids.get(0);
	}
	
	public void addOID(String oid) {
		this._oids.add(oid);
	}
	
	private void addAllOIDs(Enumeration<DefaultMutableTreeNode> oidNodes) {
		while(oidNodes.hasMoreElements()) {
			_oids.add((String) oidNodes.nextElement().getUserObject());
		}
	}
	
	public void addAllOIDs(DefaultMutableTreeNode ip) {
		Enumeration<DefaultMutableTreeNode> oidNodes = ip.children();
		addAllOIDs(oidNodes);
	}
	
	
/*
	public boolean isValidCommand() {
		return _command != null && _ip != null && _command.length() > 0 && _ip.length() > 0 
				&& validateCommand(_command) && validateIP(_ip);
	}
*/
	
	public boolean isValidCommand() {
		return validateCommand(_command) && validateDeviceData(_deviceData);
	}

	
	public static boolean isValidOID(String oid) {
		return validateOID(oid);
	}
	
	private static boolean validateCommand(String command) {
		return command != null && command.length() > 0 && Arrays.asList(METHODS).contains(command);
	}
	
	private static boolean validateDeviceData(SNMPDeviceData data) {
		SNMPOptionModel mod = (SNMPOptionModel) data.getOptions();
		return data != null && validateIP(data.getIp()) && mod != null && !mod.isEmpty();
	}
	
	private static boolean validateIP(String ip) {
		try {
			InetAddress.getByName(ip);
			return true;
		} catch(Exception e) {
		}
		return false;
	}

	private static boolean validateOID(String oid) {
		try {
			SimpleOIDTextFormat.parseOID(oid);
			return true;
		} catch (ParseException e) {
	
		} catch (NumberFormatException e) {
			
		}
		return false;
	}
	
	private static boolean validateOIDs(List<String> oids) {
		for(String oid: oids) {
			if(!validateOID(oid))
				return false;
		}
		return true;
	}
	
	public static String getMIB(String textOID) {
		Pattern patt = Pattern.compile("^(.+)::(\\w+)(\\.\\d*)?$");
		Matcher matt = patt.matcher(textOID);
		if(matt.find())
			return matt.group(1);
		return null;
	}
	
	public static boolean isMultiOIDMethod(String method) {
		return Arrays.asList(MULTI_OID_METHODS).contains(method);
	}
	
	public String toString() {
		return _command + ": " + _deviceData.getIp() + " " + _oids.toString();
	}
	

	
}
