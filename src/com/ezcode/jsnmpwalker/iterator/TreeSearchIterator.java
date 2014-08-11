package com.ezcode.jsnmpwalker.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.panel.MibTreePanel;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class TreeSearchIterator implements Iterator<TreeNode> {
	
	protected TreeModel _treeModel;
	
	private boolean _forward;
	private List<TreeNode> _nodes;
	private Enumeration<TreeNode> _traversalOrder;
	
	public TreeSearchIterator(JTree tree) {
		this(tree, true);
	}
	
	public TreeSearchIterator(JTree tree, boolean forward) {
		_treeModel = tree.getModel();
		_forward = forward;
		_nodes = new ArrayList<TreeNode>();
	}
	
	
	public void init(TreePath prevResult) {
		init(prevResult, false);
	}
	
	public void init(TreePath prevResult, boolean localMibOnly) {
		_nodes.clear();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _treeModel.getRoot();
    	TreePath startPath = null;
    	if(prevResult != null) { 
    		startPath = prevResult;
    	} else {
    		DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) root.getFirstChild();
    		DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) root.getLastChild();
			startPath = _forward ? new TreePath(firstNode.getPath()) : new TreePath(lastNode.getPath());
   	    }

		TreePath path = getPathAfterRoot(root, startPath);   		
    	setTraversal(path, startPath);
    	if(!localMibOnly) {
    		int index = _treeModel.getIndexOfChild(root, path.getLastPathComponent());
        	if(_forward) {
	        	int last = root.getChildCount();
				for(int i = index + 1; i < last; i++) {
					_nodes.add(root.getChildAt(i));
				}
    		} else {
    			for(int i = index - 1; i >= 0; i--) {
					_nodes.add(root.getChildAt(i));
    			}
    		}
    	}
    	if(prevResult != null) {
    		next(); 
    	}
	}
	
	private TreePath getPathAfterRoot(TreeNode root, TreePath path) {
		TreeNode node = (TreeNode) path.getLastPathComponent();
		if(node.getParent().equals(root)) {
			return path;
		} else {
			return getPathAfterRoot(root, path.getParentPath());
		}
	}
	
	private void setTraversal(DefaultMutableTreeNode node) {
		if(_forward) {
			_traversalOrder = node.preorderEnumeration();
		} else {
			_traversalOrder = node.postorderEnumeration();
		}
	}
	
	private void setTraversal(TreePath path, TreePath startPath) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		setTraversal(node);
		if(startPath != null) {
			while(_traversalOrder.hasMoreElements() && !path.equals(startPath)) {
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) _traversalOrder.nextElement();
				path = new TreePath(n.getPath());
			}
		}
	}
	
	
	private Iterator<TreeNode> getIterator(DefaultMutableTreeNode node) {
		Enumeration depthFirst = node.depthFirstEnumeration();
		List<TreeNode> depthFirstList = Collections.list(depthFirst);
		if(_forward) {
			Collections.reverse(depthFirstList);
		}
		return depthFirstList.iterator();
	}
		

	@Override
	public boolean hasNext() {
		return _traversalOrder.hasMoreElements() || !_nodes.isEmpty();
	}

	@Override
	public TreeNode next() {
		if(!_traversalOrder.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) _nodes.get(0);
			setTraversal(node);
			_nodes.remove(0);
		}
		return (TreeNode) _traversalOrder.nextElement();
	}

	@Override
	public void remove() {
		//ignore			
	}
				
}

