package com.ezcode.jsnmpwalker.target;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.percederberg.mibble.value.ObjectIdentifierValue;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.SNMPTreeCellEditor;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class TreeDropTarget extends DropTarget {
	
	public static final DataFlavor MIB_DATA_FLAVOR = new DataFlavor(ObjectIdentifierValue.class,
			ObjectIdentifierValue.class.getSimpleName());
	
	public static final DataFlavor DEVICE_DATA_FLAVOR = new DataFlavor(InetAddress.class,
			InetAddress.class.getSimpleName());
	
	private SNMPTreePanel _panel;
	private JTree _tree;
		
	public TreeDropTarget(SNMPTreePanel panel) {
		_panel = panel;
		_tree = panel.getTree();
	}
	
	
	private void collectIpNodes(List<TreePath> paths, TreePath path, int nodeType) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		if(node.isLeaf() && path.getPathCount() < nodeType)
			return;
		else if(path.getPathCount() == nodeType)
			paths.add(path);
		else {
			Enumeration children = node.children();
			while(children.hasMoreElements()) {
				collectIpNodes(paths, path.pathByAddingChild(children.nextElement()), nodeType);
			}
		}
	}
	
	private String formatData(Object item) {
		if(item instanceof ObjectIdentifierValue) {
			return item.toString();
		} else if(item instanceof InetAddress) {
			return ((InetAddress) item).getHostAddress();
		}
		return "Undefined";
	}
	
	private void insertTransferData(DropTargetDropEvent evt, Transferable transfer, DataFlavor flavor, int nodeType) {
        try {
			evt.acceptDrop(DnDConstants.ACTION_COPY);
			Object data = transfer.getTransferData(flavor);
			if(data != null && data.toString().length() > 0 && data instanceof List) {          		
            	List<Object> list = (List<Object>) data;
                String[] item = new String[list.size()];
                for(int i = 0; i < list.size(); i++) {
                	item[i] = formatData(list.get(i));
                }
	            Point location = evt.getLocation();
	    		TreePath path = _tree.getPathForLocation(location.x, location.y);
	    		if(path != null) {
		    		int level = path.getPathCount();
		    		if(level == nodeType) {
		    			String[] options = {"Overwrite", "Add", "Cancel"};
		    			int result = JOptionPane.showOptionDialog(null, "Would you like to overwrite the component or add a new one?", "Drag and Drop", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		    			if(result == JOptionPane.YES_OPTION) {
		    				_panel.pasteData(path, item);
		    			} else if(result == JOptionPane.NO_OPTION) {
		    				_panel.insertData(path.getParentPath(), item);
		    			}
		    		} else {
		    			List<TreePath> paths = new ArrayList<TreePath>();
		    			collectIpNodes(paths, path, nodeType - 1);
		    			TreePath[] patharr = new TreePath[paths.size()];
		    			patharr = paths.toArray(patharr);
		    			_panel.insertData(patharr, item);
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
	
	@Override
    public synchronized void drop(DropTargetDropEvent evt) {
        Transferable transfer = evt.getTransferable();
        if(transfer.isDataFlavorSupported(MIB_DATA_FLAVOR)) {
        	insertTransferData(evt, transfer, MIB_DATA_FLAVOR, SNMPTreeCellEditor.OID_NODE);
        } else if(transfer.isDataFlavorSupported(DEVICE_DATA_FLAVOR)) {
        	insertTransferData(evt, transfer, DEVICE_DATA_FLAVOR, SNMPTreeCellEditor.IP_NODE);
        }
	}

}
