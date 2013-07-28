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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class PasteCommand extends TreeCommand {
	private Map<TreePath, Object> _pathMap;
	private List<DefaultMutableTreeNode> _addedNodes;
	private String[] _copyData;
	
	public PasteCommand(SNMPTreePanel panel, TreePath path) {
		this(panel, path, null);
	}
	
	public PasteCommand(SNMPTreePanel panel, TreePath[] paths) {
		this(panel, paths, null);
	}
	
	public PasteCommand(SNMPTreePanel panel, TreePath path, String copyData) {
		this(panel, new TreePath[] {path}, new String[] {copyData});
	}
	
	public PasteCommand(SNMPTreePanel panel, TreePath[] paths, String[] copyData) {
		super(panel);
		if(copyData == null) {
			 String str = _panel.getClipboardContents();
			 if(str != null)
				 _copyData = str.split("\\r?\\n");
		} else {
			_copyData = copyData;
		}

		_pathMap = new HashMap<TreePath, Object>();
		_addedNodes = new ArrayList<DefaultMutableTreeNode>();
		if(paths != null) {
			for(TreePath path: paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				_pathMap.put(path, node.getUserObject());
			}
			int addNum = paths.length;
			while(addNum < _copyData.length) {
				_addedNodes.add(new DefaultMutableTreeNode(_copyData[addNum]));
				addNum++;
			}
		}	
	}

	@Override
	public void execute() {
		if(_copyData != null && _copyData.length > 0) {	
			Set<TreePath> paths = getPaths();
			int index = 0;
			int lastline = _copyData.length - 1;
			DefaultMutableTreeNode parent = null;
			
			for(TreePath path: paths) {
				if(index > lastline)
					break;
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				parent = (DefaultMutableTreeNode) node.getParent();
				node.setUserObject(_copyData[index]);
				_treeModel.nodeChanged(node);
				index++;
			}
			if(parent != null) {
				for(DefaultMutableTreeNode node: _addedNodes) {
					if(index > lastline)
						break;
					_panel.addNode(parent, _copyData[index], node);
				}
			}
		}
	}

	@Override
	public void undo() {
		Set<TreePath> paths = getPaths();
		for(TreePath path: _pathMap.keySet()) {
			Object obj = _pathMap.get(path);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			node.setUserObject(obj);
			_treeModel.nodeChanged(node);
		}
		for(DefaultMutableTreeNode node: _addedNodes) {
			_treeModel.removeNodeFromParent(node);
		}

	}
	
	private Set<TreePath> getPaths() {
		return _pathMap.keySet();
	}
	
	private Object getPathData(TreePath path) {
		return _pathMap.get(path);
	}

}
