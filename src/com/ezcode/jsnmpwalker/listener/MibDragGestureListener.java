package com.ezcode.jsnmpwalker.listener;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.Cursor;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.data.TransferableOid;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

import net.percederberg.mibble.browser.MibNode;
import net.percederberg.mibble.value.ObjectIdentifierValue;

public class MibDragGestureListener implements DragGestureListener {
	private JTree _mibTree;
	
	public MibDragGestureListener(JTree tree) {
		_mibTree = tree;
	}
	
	private void collectOids(List<ObjectIdentifierValue> oids, MibNode node) {
		ObjectIdentifierValue oid = (ObjectIdentifierValue) node.getValue();
		if(oid == null) {
			Enumeration children = node.children();
			while(children.hasMoreElements()) {
				MibNode child = (MibNode)children.nextElement();
				collectOids(oids, child);
			}
		} else {
			oids.add(oid);
		}
	}
	
	private void collectOids(List<ObjectIdentifierValue> oids, TreePath[] paths) {
		for(TreePath path: paths) {
			MibNode node = (MibNode) path.getLastPathComponent();
			collectOids(oids, node);
			
		}
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		Cursor cursor = null;
		//JTree mibTree = (JTree) event.getComponent();

		if (event.getDragAction() == DnDConstants.ACTION_COPY) {
			cursor = DragSource.DefaultCopyDrop;
		}
		List<ObjectIdentifierValue> oids = new ArrayList<ObjectIdentifierValue>();
		TreePath[] paths = _mibTree.getSelectionPaths();
		if(paths == null) {
			Point location = event.getDragOrigin();
			TreePath path = _mibTree.getPathForLocation(location.x, location.y);
			if(path != null) {
				MibNode node = (MibNode) path.getLastPathComponent();
				collectOids(oids, node);
			}
		} else {
			collectOids(oids, paths);
		}
		event.startDrag(cursor, new TransferableOid(oids, PanelUtils.MIB_DATA_FLAVOR));	
	}

}
