package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class InsertCommand extends AddCommand {

	public InsertCommand(SNMPTreePanel panel, TreePath path, String str) {
		this(panel, new TreePath[] {path}, new String[] {str});
	}

	public InsertCommand(SNMPTreePanel panel, TreePath[] paths, String[] str) {
		super(panel, paths, str);
	}

}
