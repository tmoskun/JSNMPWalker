package com.ezcode.jsnmpwalker.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class EditSNMPCommand extends TreeCommand {
	
	private TreePath _ipPath;
	private Object _savedIP;
	private Object _newIP;
	private List<TreeNode> _savedOIDs = new ArrayList<TreeNode>();
	private List<TreeNode> _newOIDs = new ArrayList<TreeNode>();

	public EditSNMPCommand(SNMPTreePanel panel, TreePath ipPath, Object ip, Collection<String> oids) {
		super(panel);
		_ipPath = ipPath;
		TreeNode ipNode = (TreeNode) _ipPath.getLastPathComponent();
		Enumeration oidNodes = ipNode.children();
		while(oidNodes.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) oidNodes.nextElement();
			_savedOIDs.add(node);
		}
		for(String oid: oids) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(oid);
			_newOIDs.add(node);
		}
		_savedIP = ((DefaultMutableTreeNode) ipNode).getUserObject();
		_newIP = ip;
	}

	@Override
	public void execute() {
		setData(_newIP, _newOIDs);
	}

	@Override
	public void undo() {
		setData(_savedIP, _savedOIDs);
	}
	
	private void setData(Object ip, List<TreeNode> oidNodes) {
		DefaultMutableTreeNode ipNode = (DefaultMutableTreeNode) _ipPath.getLastPathComponent();
		if(ip != null) {
			ipNode.setUserObject(ip);
			//_treeModel.nodeChanged(ipNode);
		}
		ipNode.removeAllChildren();
		for(int i = 0; i < oidNodes.size(); i++) {
			_treeModel.insertNodeInto((MutableTreeNode) oidNodes.get(i), ipNode, i);
		}
		_treeModel.reload();
		_tree.expandPath(_ipPath);
	}
	
	
}
