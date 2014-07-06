package com.ezcode.jsnmpwalker.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TransferableSNMPDeviceData implements Transferable {
	
	private List<SNMPDeviceData> _dataList;
	private DataFlavor _dataFlavor;
	
	public TransferableSNMPDeviceData(SNMPDeviceData data , DataFlavor dataFlavor) {
		init(dataFlavor);
		_dataList.add(data);
	}
	
	public TransferableSNMPDeviceData(List<SNMPDeviceData> dataList , DataFlavor dataFlavor) {
		init(dataFlavor);
		_dataList.addAll(dataList);
	}
	
	private void init(DataFlavor dataFlavor) {
		_dataFlavor = dataFlavor;
		_dataList = new ArrayList<SNMPDeviceData>();
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { _dataFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(_dataFlavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(_dataFlavor))
			return _dataList;
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
