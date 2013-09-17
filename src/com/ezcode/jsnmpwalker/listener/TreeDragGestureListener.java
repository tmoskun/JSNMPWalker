package com.ezcode.jsnmpwalker.listener;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;

import net.percederberg.mibble.value.ObjectIdentifierValue;

import com.ezcode.jsnmpwalker.SNMPTreeCellEditor;
import com.ezcode.jsnmpwalker.data.TransferableIp;
import com.ezcode.jsnmpwalker.data.TransferableTreeData;
import com.ezcode.jsnmpwalker.target.TreeDropTarget;

public class TreeDragGestureListener implements DragGestureListener {
	private JTree _tree;
	
	public TreeDragGestureListener(JTree tree) {
		_tree = tree;
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		Cursor cursor = null;
		//JTree mibTree = (JTree) event.getComponent();

		if (event.getDragAction() == DnDConstants.ACTION_COPY) {
			cursor = DragSource.DefaultCopyDrop;
		}
		List<String> oids = new ArrayList<String>();
		List<String> ips = new ArrayList<String>();
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null) {
			Point location = event.getDragOrigin();
			TreePath path = _tree.getPathForLocation(location.x, location.y);
			if(path != null) {
				String obj = (String) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
				if(path.getPathCount() == SNMPTreeCellEditor.IP_NODE) {
					ips.add(obj);
				} else if(path.getPathCount() == SNMPTreeCellEditor.OID_NODE) {
					oids.add(obj);
				}
			}
		}
		if(ips.size() > 0)
			event.startDrag(cursor, new TransferableTreeData(ips, SNMPTreeCellEditor.IP_NODE));
		if(oids.size() > 0)
			event.startDrag(cursor, new TransferableTreeData(oids, SNMPTreeCellEditor.OID_NODE));
	}

}
