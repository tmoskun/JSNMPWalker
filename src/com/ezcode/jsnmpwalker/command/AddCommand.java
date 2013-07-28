package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class AddCommand extends TreeCommand {
	private String[] _userData = null;
	private Map<TreePath, List<TreeNode>> _pathMap;
	
	public AddCommand(SNMPTreePanel panel, TreePath path) {
		this(panel, path, "");
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath path, String userData) {
		this(panel, new TreePath[] {path}, new String[] {userData});
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath[] paths) {
		this(panel, paths, new String[] {""});
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath[] paths, String userData) {
		this(panel, paths, new String[] {userData});
	}

	public AddCommand(SNMPTreePanel panel, TreePath[] paths, String[] userData) {
		super(panel);
		if(userData == null) {
			 String str = _panel.getClipboardContents();
			 if(str != null)
				 _userData = str.split("\\r?\\n");
		} else {
			_userData = userData;
		}
		//_userData = userData;
		_pathMap = new HashMap<TreePath, List<TreeNode>>();
		for(TreePath path: paths) {
			List<TreeNode> nodes = new ArrayList<TreeNode>();
			for(String s: _userData) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(s);
				nodes.add(node);
			}
			_pathMap.put(path, nodes);
		}
	}
	
	

	@Override
	public void execute() {
		Set<TreePath> paths = getPaths();
		for(TreePath path: paths) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
			if(_userData != null) {
				List<TreeNode> nodes = getChildren(path);
				int index = 0;
				int lastIndex = _userData.length-1;
				for(TreeNode node: nodes) {
					if(index > lastIndex)
						break;
					_panel.addNode(parent, _userData[index], (DefaultMutableTreeNode) node);
					index++;
				}
			}
		}
	}

	@Override
	public void undo() {
		Set<TreePath> paths = getPaths();
		for(TreePath path: paths) {
			List<TreeNode> nodes = getChildren(path);
			for(TreeNode node: nodes) {
				_treeModel.removeNodeFromParent((MutableTreeNode) node);
			}
		}	
	}
	
	private Set<TreePath> getPaths() {
		return _pathMap.keySet();
	}
	
	private List<TreeNode> getChildren(TreePath path) {
		return _pathMap.get(path);
	}

}
