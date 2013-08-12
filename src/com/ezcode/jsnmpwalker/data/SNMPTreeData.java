package com.ezcode.jsnmpwalker.data;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.net.InetAddress;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;


public class SNMPTreeData {
	
	//public static final String[] COMMANDS = {"Get", "GetNext", "GetBulk", "Walk", "Table"};
	public static final String[] COMMANDS = {"Get", "GetNext", "GetBulk", "Walk"};
//	private static final String IP_PATTERN = 
//	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
//	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private String _command = "";
	private String _ip = "";
	private String _oid = "";
	
	public SNMPTreeData(String command, String ip, String oid) {
		_command = command;
		_ip = ip;
		_oid = oid;
	}
	
	public SNMPTreeData(TreeNode[] nodes, int commandnum, int ipnum, int oidnum) {
		if(nodes.length >= Math.max(commandnum, Math.max(ipnum, oidnum))) {
			if(nodes[commandnum] instanceof DefaultMutableTreeNode) {
				_command = (String) ((DefaultMutableTreeNode) nodes[commandnum]).getUserObject();
			}
			if(nodes[ipnum] instanceof DefaultMutableTreeNode) {
				_ip = (String) ((DefaultMutableTreeNode) nodes[ipnum]).getUserObject();
			}
			if(nodes[oidnum] instanceof DefaultMutableTreeNode) {
				_oid = (String) ((DefaultMutableTreeNode) nodes[oidnum]).getUserObject();
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

	public String getOid() {
		return _oid;
	}

	public void setOid(String _oid) {
		this._oid = _oid;
	}

	public boolean isValid() {
		return _command != null && _ip != null && _oid != null && _command.length() > 0 && _ip.length() > 0 && _oid.length() > 0
				&& validateCommand(_command) && validateIP(_ip) && validateOID(_oid);
	}
	
	private static boolean validateCommand(String command) {
		return Arrays.asList(COMMANDS).contains(command);
	}
	private static boolean validateIP(String ip) {
		try {
			InetAddress.getByName(ip);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	//TODO
	private static boolean validateOID(String oid) {
		return true;
	}
	
	public String toString() {
		return _command + ": " + _ip + " " + _oid;
	}
	

	
}
