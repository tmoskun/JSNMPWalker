package com.ezcode.jsnmpwalker.command;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class CreateSNMPCommand extends AddCommand {
	
	public CreateSNMPCommand(SNMPTreePanel panel, Object[] userData) {
		super(panel);
		List data = new ArrayList();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _treeModel.getRoot();
		TreePath path = new TreePath(root);
		for(Object obj: userData) {
			if(obj instanceof Collection) {
				Collection<String> obj1 = (Collection) obj;
				List nodes = new ArrayList();
				for(Object obj2: obj1) {
					addData(path, data, obj2, nodes);
				}
			} else {
				path = addData(path, data, obj);
			}
		}
		setUserData(data.toArray());
	}
	
	
	private void addData(TreePath path, List data, Object obj, List nodes) {
		if(obj != null && obj.toString().length() > 0) {
			addData(path, data, obj,false, nodes);
			saveNodes(path, nodes);
		}
	}
	
	private TreePath addData(TreePath path, List data, Object obj) {
		return addData(path, data, obj, true, null);
	}
	
	private TreePath addData(TreePath path, List data, Object obj, boolean extend, List nodes) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(obj);
		TreePath foundPath = contains(_treeModel, path, obj);
		if(foundPath == null) {
			data.add(obj);
			if(extend) {
				saveNode(path, node);
				path = path.pathByAddingChild(node);
			} else if(nodes != null && !nodes.contains(node)){
				nodes.add(node);
			}
		} else {
			path = foundPath;
		}
		return path;
	}

	private static TreePath contains(DefaultTreeModel model, TreePath path, Object obj) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
		if(parent != null) {
			Enumeration children = parent.children();
			while(children.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
				Object nodeObj = node.getUserObject();
				if(nodeObj.equals(obj)) {
					return new TreePath(model.getPathToRoot(node));
				}
			}
		}
		return null;
	}

}
