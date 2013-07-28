package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class RemoveCommand extends TreeCommand {
	protected Map<TreePath, Map<Integer, TreeNode>> _pathMap;

	public RemoveCommand(SNMPTreePanel panel, TreePath[] paths) {
		super(panel);
		_pathMap = new Hashtable<TreePath, Map<Integer, TreeNode>>();
		if(paths != null) {
			for(TreePath path: paths) {
				TreePath parentPath = path.getParentPath();
				MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
				MutableTreeNode parent = (MutableTreeNode) parentPath.getLastPathComponent();
				int index = _treeModel.getIndexOfChild(parent, node);
				Map<Integer, TreeNode> nodes = _pathMap.get(parentPath);
				if(nodes == null) {
					nodes = new TreeMap<Integer, TreeNode>();
					_pathMap.put(parentPath, nodes);
				}
				nodes.put(index, node);
			}
		} 
	}

	@Override
	public void execute() {
		List<TreeNode> nodes = getAllNodes();
		for(TreeNode node: nodes) {
			if(!((DefaultMutableTreeNode) node).isRoot())
				_treeModel.removeNodeFromParent((MutableTreeNode) node);
		}
	}

	@Override
	public void undo() {
		Set<TreePath> paths = getPaths();
		for(TreePath path: paths) {
			MutableTreeNode parent = (MutableTreeNode) path.getLastPathComponent();
			Map<Integer, TreeNode> children = getChildren(path);
			//add in the ascending order
			for(int index: children.keySet()) {
				MutableTreeNode node = (MutableTreeNode) children.get(index);
				_treeModel.insertNodeInto(node, parent, index);
			}
			//expand all
			Enumeration<TreeNode> nodes = ((DefaultMutableTreeNode) parent).depthFirstEnumeration();
			while(nodes.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
				_tree.expandPath(new TreePath(node.getPath()));
			}
		}
	}
	
	protected Set<TreePath> getPaths() {
		return _pathMap.keySet();
	}
	
	protected Map<Integer, TreeNode> getChildren(TreePath path) {
		return _pathMap.get(path);
	}
		
	protected Collection<TreeNode> getChildNodes(TreePath path) {
		return _pathMap.get(path).values();
	}
	
	protected List<TreeNode> getAllNodes() {
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		for(TreePath path: _pathMap.keySet()) {
			Collection<TreeNode> childNodes = getChildNodes(path);
			nodes.addAll(childNodes);
		}
		return nodes;
	}

}
