package com.ezcode.jsnmpwalker.data;
/**
 * Copyright(c) 2012-2014 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import net.percederberg.mibble.value.ObjectIdentifierValue;

public class TransferableIp implements Transferable {
	private List<InetAddress> _ips;
	private DataFlavor _dataFlavor;
	
	public TransferableIp(InetAddress ip, DataFlavor dataFlavor) {
		init(dataFlavor);
		_ips.add(ip);
	}
	
	public TransferableIp(List<InetAddress> ips, DataFlavor dataFlavor) {
		init(dataFlavor);
		_ips.addAll(ips);
	}
	
	private void init(DataFlavor dataFlavor) {
		_dataFlavor = dataFlavor;
		_ips = new ArrayList<InetAddress>();
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
			return _ips;
		else
			throw new UnsupportedFlavorException(flavor);
	}

}
