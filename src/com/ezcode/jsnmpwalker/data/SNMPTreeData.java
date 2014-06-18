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
	public static final String[] SINGLE_OID_COMMANDS = {"GetBulk", "Walk"};
	public static final String[] MULTI_OID_COMMANDS = {"Get", "GetNext"};
	public static final String[] COMMANDS = Arrays.copyOf(MULTI_OID_COMMANDS, MULTI_OID_COMMANDS.length + SINGLE_OID_COMMANDS.length);
	static {
		System.arraycopy(SINGLE_OID_COMMANDS, 0, COMMANDS, MULTI_OID_COMMANDS.length, SINGLE_OID_COMMANDS.length);
	}
	public static final Pattern MIB_PATTERN = Pattern.compile("^(.+)::(\\w+)((\\.\\d)*)?$");

	private String _command = "";
	private String _ip = "";
	private List<String> _oids = new ArrayList<String>();
	
	public SNMPTreeData(String command, String ip, String oid) {
		_command = command;
		_ip = ip;
		_oids.add(oid);
	}
	
	public SNMPTreeData(String command, String ip, List<String> oids) {
		_command = command;
		_ip = ip;
		_oids.addAll(oids);
	}
	
	public SNMPTreeData(TreeNode[] nodes, int commandnum, int ipnum, int oidnum) {
		if(nodes.length >= Math.max(commandnum, Math.max(ipnum, oidnum))) {
			if(nodes[commandnum] instanceof DefaultMutableTreeNode) {
				_command = (String) ((DefaultMutableTreeNode) nodes[commandnum]).getUserObject();
			}
			if(nodes[ipnum] instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode ip = (DefaultMutableTreeNode) nodes[ipnum];
				_ip = (String) ip.getUserObject();
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

	public void setCommand(String _command) {
		this._command = _command;
	}

	public String getIp() {
		return _ip;
	}

	public void setIp(String _ip) {
		this._ip = _ip;
	}

	public List<String> getOids() {
		return _oids;
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

	public boolean isValidNode() {
		return _command != null && _ip != null && _command.length() > 0 && _ip.length() > 0 
				&& validateCommand(_command) && validateIP(_ip);
	}
	
	public static boolean isValidOID(String oid) {
		return validateOID(oid);
	}
	
	private static boolean validateCommand(String command) {
		return Arrays.asList(COMMANDS).contains(command);
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
	
	public static String getMIB(String textOID) {
		Pattern patt = Pattern.compile("^(.+)::(\\w+)(\\.\\d*)?$");
		Matcher matt = patt.matcher(textOID);
		if(matt.find())
			return matt.group(1);
		return null;
	}
	
	public static boolean isMultiOIDCommand(String command) {
		return Arrays.asList(MULTI_OID_COMMANDS).contains(command);
	}
	
	public String toString() {
		return _command + ": " + _ip + " " + _oids.toString();
	}
	

	
}
