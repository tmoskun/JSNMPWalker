package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class CutCommand extends RemoveCommand {
	StringBuilder _savedClipboardData;

	public CutCommand(SNMPTreePanel panel, TreePath[] paths) {
		super(panel, paths);
		_savedClipboardData = new StringBuilder();
		_savedClipboardData.append(_panel.getClipboardContents());
	}
	
	@Override
	public void execute() {
		super.execute();
		_panel.copyData(getAllNodes());
	}	
	
	@Override
	public void undo() {
		super.undo();
		if(_savedClipboardData.length() > 0)
			_panel.setClipboardContents(_savedClipboardData.toString());
	}
	
	

}
