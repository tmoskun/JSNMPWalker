package com.ezcode.jsnmpwalker.data;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class TransferableTreeData implements Transferable, Serializable {
	private List<String> _data;
	private int _dataType;
	
	public TransferableTreeData(String d, int dataType) {
		init(dataType);
		_data.add(d);
	}
	
	public TransferableTreeData(List<String> d, int dataType) {
		init(dataType);
		_data.addAll(d);
	}
	
	private void init(int dataType) {
		_dataType = dataType;
		_data = new ArrayList<String>();
	}
	
	public int getDataType() {
		return _dataType;
	}
	
	public List getData() {
		return _data;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {DataFlavor.stringFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.stringFlavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DataFlavor.stringFlavor)) {
			return this;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
