package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.command.TreeNodeCommandStack.Command;

public class EditCommand implements Command {
	private String _savedData;
	private String _newData = null;
	private TreeNode _node = null;
	private JTree _tree;
	
	
	public EditCommand(JTree tree) {
		this(tree, null);
	}
	
	public EditCommand(JTree tree, TreePath path) {
		_tree = tree;
		setNode(path);
	}

	@Override
	public void execute() {
		setData(_newData);
	}

	@Override
	public void undo() {
		_newData = _node.toString();
		setData(_savedData);
	}
	
	private void setNode(TreePath path) {
		if(path == null) {
			_savedData = "";
		} else {
			setNode((TreeNode) path.getLastPathComponent());
		}
	}
	
	public void setNode(TreeNode node) {
		_node = node;
		_savedData = _node.toString();
	}
	
	private void setData(String str) {
		if(str != null) {
			((DefaultMutableTreeNode) _node).setUserObject(str);
			((DefaultTreeModel)_tree.getModel()).nodeChanged(_node);
		}
	}

}
