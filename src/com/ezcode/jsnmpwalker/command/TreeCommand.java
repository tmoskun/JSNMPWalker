package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.command.TreeNodeCommandStack.Command;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public abstract class TreeCommand implements Command {
	
	protected SNMPTreePanel _panel;
	protected JTree _tree;
	protected DefaultTreeModel _treeModel;
	
	
	public TreeCommand(SNMPTreePanel panel) {
		_panel = panel;
		_tree = panel.getTree();
		_treeModel = (DefaultTreeModel) _tree.getModel();
	}

	@Override
	public abstract void execute();

	@Override
	public abstract void undo();

}
