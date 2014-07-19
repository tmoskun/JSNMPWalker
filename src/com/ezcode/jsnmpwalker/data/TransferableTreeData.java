package com.ezcode.jsnmpwalker.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class TransferableTreeData implements Transferable, Serializable {
	private List<Object> _data;
	private int _dataType;
	
	public TransferableTreeData(Object d, int dataType) {
		init(dataType);
		_data.add(d);
	}
	
	public TransferableTreeData(List<Object> d, int dataType) {
		init(dataType);
		_data.addAll(d);
	}
	
	private void init(int dataType) {
		_dataType = dataType;
		_data = new ArrayList<Object>();
	}
	
	public int getDataType() {
		return _dataType;
	}
	
	public List getData() {
		return _data;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {DataFlavor.stringFlavor, SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if(_dataType == SNMPTreePanel.OID_NODE)
			return flavor.equals(DataFlavor.stringFlavor);
		else if(_dataType == SNMPTreePanel.IP_NODE)
			return flavor.equals(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DataFlavor.stringFlavor) || flavor.equals(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR)) {
			return this;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
