package com.ezcode.jsnmpwalker.target;

/**
 * Copyright(c) 2014 

 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.net.InetAddress;

import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.TransferableTreeData;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.utils.PanelUtils;


public abstract class AbstractSNMPDropTarget extends DropTarget {
	
	protected abstract void insertTransferData(DropTargetDropEvent evt, Object data, int nodeType);
	
	protected void insertTransferData(DropTargetDropEvent evt, Transferable transfer, DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
    	Object data = transfer.getTransferData(dataFlavor);
		if(data instanceof TransferableTreeData) {
	        TransferableTreeData treeData = (TransferableTreeData) data;
	        insertTransferData(evt, treeData.getData(), treeData.getDataType());
		}
	}
	
	@Override
    public synchronized void drop(DropTargetDropEvent evt) {
        Transferable transfer = evt.getTransferable();
        try {
	        if(transfer.isDataFlavorSupported(PanelUtils.MIB_DATA_FLAVOR)) {
	        	//from the MIB tree
	        	insertTransferData(evt, transfer.getTransferData(PanelUtils.MIB_DATA_FLAVOR), SNMPTreePanel.OID_NODE);
	        } else if(transfer.isDataFlavorSupported(PanelUtils.DEVICE_DATA_FLAVOR)) {
	        	//from the device tree
	        	insertTransferData(evt, transfer.getTransferData(PanelUtils.DEVICE_DATA_FLAVOR), SNMPTreePanel.IP_NODE);
	        } else if(transfer.isDataFlavorSupported(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR)) {
	        	//from the SNMP Command tree, ips
	        	insertTransferData(evt, transfer, SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
	        } else if(transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
	        	//from the SNMP Command tree, oids
	        	insertTransferData(evt, transfer, DataFlavor.stringFlavor);
	        }
        } catch (UnsupportedFlavorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	final static protected Object getObject(Object obj, int nodeType) {
		if(obj != null) {
			if(obj instanceof InetAddress) {
				return new SNMPDeviceData(PanelUtils.formatData(obj));
			} else if(obj instanceof SNMPDeviceData) {
				return new SNMPDeviceData((SNMPDeviceData)obj);
			}
		}
		return obj;
	}

}
