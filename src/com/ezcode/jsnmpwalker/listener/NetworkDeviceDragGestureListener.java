package com.ezcode.jsnmpwalker.listener;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import com.ezcode.jsnmpwalker.data.TransferableIp;
import com.ezcode.jsnmpwalker.target.TreeDropTarget;

public class NetworkDeviceDragGestureListener implements DragGestureListener {
	
	private JTable _deviceList;
	
	public NetworkDeviceDragGestureListener(JTable table) {
		_deviceList = table;
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent event) {
		Cursor cursor = null;
		//JTree mibTree = (JTree) event.getComponent();

		if (event.getDragAction() == DnDConstants.ACTION_COPY) {
			cursor = DragSource.DefaultCopyDrop;
		}
		Point location = event.getDragOrigin();
		int[] rows = _deviceList.getSelectedRows();
		if(rows == null) {
			rows = new int[] {_deviceList.rowAtPoint(location)};
		}
		int col = _deviceList.columnAtPoint(location);
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		for(int r: rows) {
			InetAddress address = (InetAddress) _deviceList.getValueAt(r, col);
			addresses.add(address);
		}
		event.startDrag(cursor, new TransferableIp(addresses, TreeDropTarget.DEVICE_DATA_FLAVOR));
	}

}
