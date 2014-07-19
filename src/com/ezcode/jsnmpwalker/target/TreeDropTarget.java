package com.ezcode.jsnmpwalker.target;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.TransferableTreeData;
import com.ezcode.jsnmpwalker.dialog.CommandDialog;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class TreeDropTarget extends AbstractSNMPDropTarget {
	
	private SNMPTreePanel _panel;
	private JTree _tree;
		
	public TreeDropTarget(SNMPTreePanel panel) {
		_panel = panel;
		_tree = panel.getTree();
	}
	
	
	private void collectNodes(List<TreePath> paths, TreePath path, int nodeType) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if(node.isLeaf() && path.getPathCount() < nodeType) {
			return;
		} else if(path.getPathCount() == nodeType) {
			paths.add(path);
		} else {
			Enumeration children = node.children();
			while(children.hasMoreElements()) {
				collectNodes(paths, path.pathByAddingChild(children.nextElement()), nodeType);
			}
		}
	}
	
	@Override
	protected void insertTransferData(DropTargetDropEvent evt, Object data, int nodeType) {
        try {
			evt.acceptDrop(DnDConstants.ACTION_COPY);
			if(data != null && data.toString().length() > 0 && data instanceof List) {          		
            	List<Object> list = (List<Object>) data;
                Object[] items = new Object[list.size()];
                for(int i = 0; i < list.size(); i++) {
                	items[i] = getObject(list.get(i), nodeType);
                }
	            Point location = evt.getLocation();
	    		TreePath path = _tree.getPathForLocation(location.x, location.y);
	    		boolean found = false;
	    		if(path != null) {
		    		int level = path.getPathCount();
		    		if(level == nodeType) {
		    			found = true;
		    			String[] options = {"Overwrite", "Add", "Cancel"};
		    			int result = JOptionPane.showOptionDialog(null, "Would you like to overwrite the component or add a new one?", "Drag and Drop", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		    			if(result == JOptionPane.YES_OPTION) {
		    				_panel.pasteData(path, items);
		    			} else if(result == JOptionPane.NO_OPTION) {
		    				_panel.insertData(path.getParentPath(), items);
		    			}
		    		} else {
		    			List<TreePath> otherPaths = new ArrayList<TreePath>();
		    			collectNodes(otherPaths, path, nodeType - 1);
		    			if(otherPaths.size() > 0) {
		    				found = true;
			    			TreePath[] patharr = new TreePath[otherPaths.size()];
			    			patharr = otherPaths.toArray(patharr);
			    			_panel.insertData(patharr, items);
		    			}
		    		}
	    		} 
	    		if(path == null || !found) {
    				if(nodeType == SNMPTreePanel.IP_NODE) {
    					_panel.openCommandDialog(items[0], nodeType);
    				} else if(nodeType == SNMPTreePanel.OID_NODE) {
    					_panel.openCommandDialog(items, nodeType);
    				}
	    		}
            }
			return;
        } catch(Exception ex) {
        	ex.printStackTrace();
        	evt.rejectDrop();
            //System.err.println(ex.getMessage());
        } finally {
            evt.dropComplete(true);
        }
	}
	

}
