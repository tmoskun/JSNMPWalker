package com.ezcode.jsnmpwalker.command;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.utils.ClipboardUtils;

public class AddCommand extends TreeCommand {
	private Object[] _userData = null;
	private Map<TreePath, List<TreeNode>> _pathMap;

	public AddCommand(SNMPTreePanel panel) {
		super(panel);
		_pathMap = new HashMap<TreePath, List<TreeNode>>();
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath path) {
		this(panel, path, "");
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath path, Object userData) {
		this(panel, new TreePath[] {path}, new Object[] {userData});
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath[] paths) {
		this(panel, paths, new String[] {""});
	}
	
	public AddCommand(SNMPTreePanel panel, Object[] userData) {
		this(panel, new TreePath[] {}, userData);
	}
	
	public AddCommand(SNMPTreePanel panel, TreePath[] paths, Object userData) {
		this(panel, paths, new Object[] {userData});
	}
	

	public AddCommand(SNMPTreePanel panel, TreePath[] paths, Object[] userData) {
		super(panel);
		//System.out.println("add " + (paths != null) + " " + (paths.length > 0) + " " + paths[0].getPathCount() + " " + SNMPTreePanel.IP_NODE);
		setUserData(userData, (paths != null && paths.length > 0 && (paths[0].getPathCount() + 1) == SNMPTreePanel.IP_NODE));
		_pathMap = new HashMap<TreePath, List<TreeNode>>();
		for(TreePath path: paths) {
			List<TreeNode> nodes = new ArrayList<TreeNode>();
			for(Object s: _userData) {
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(s);
				nodes.add(node);
			}
			saveNodes(path, nodes);
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
					Object obj = ((DefaultMutableTreeNode) node).getUserObject();
					boolean isEditing = (obj == null || obj.toString().length() == 0);
					_panel.addNode(parent, (DefaultMutableTreeNode) node, isEditing);
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
	
	protected void setUserData(Object[] userData) {
		setUserData(userData, false);
	}
	
	protected void setUserData(Object[] userData, boolean createObjects) {
		if(userData == null) {
			 Object obj = ClipboardUtils.getClipboardContents();
			 if(obj != null) {
				 if(obj instanceof List) {
					 _userData = ((List) obj).toArray();
				 } else if(obj instanceof String) {
					 if(createObjects) {
						 String[] userDataStr = ((String) obj).split("\\r?\\n");
						 _userData = new Object[userDataStr.length];
						  for(int i = 0; i < userDataStr.length; i++) {
							  _userData[i] = new SNMPDeviceData((String) userDataStr[i]);
						  }
					 } else {
						 _userData = ((String) obj).split("\\r?\\n");
					 }
				 }
			 }
		} else {
			_userData = userData;
		}
	}
	
	protected Object[] getUserData() {
		return _userData;
	}
	
	protected void saveNodes(TreePath path, List<TreeNode> nodes) {
		_pathMap.put(path, nodes);
	}
	
	protected void saveNode(TreePath path, TreeNode node) {
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		nodes.add(node);
		saveNodes(path, nodes);
	}
	
	private TreeSet<TreePath> getPaths() {
		TreeSet sorted = new TreeSet(new PathComparator());
		sorted.addAll(_pathMap.keySet());
		return sorted;
	}
	
	private List<TreeNode> getChildren(TreePath path) {
		return _pathMap.get(path);
	}
	
	private class PathComparator implements Comparator<TreePath> {

		@Override
		public int compare(TreePath o1, TreePath o2) {
			return o1.getPathCount() - o2.getPathCount();
		}
		
	}

}
