package com.ezcode.jsnmpwalker.data;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import net.percederberg.mibble.browser.MibNode;
import net.percederberg.mibble.value.ObjectIdentifierValue;


public class TransferableOid implements Transferable {
	
	private List<ObjectIdentifierValue> _oids;
	private DataFlavor _dataFlavor;
	
	public TransferableOid(ObjectIdentifierValue oid, DataFlavor dataFlavor) {
		init(dataFlavor);
		_oids.add(oid);
	}
	
	public TransferableOid(List<ObjectIdentifierValue> oids, DataFlavor dataFlavor) {
		init(dataFlavor);
		_oids.addAll(oids);
	}
	
	private void init(DataFlavor dataFlavor) {
		_dataFlavor = dataFlavor;
		_oids = new ArrayList<ObjectIdentifierValue>();
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
			return _oids;
		else
			throw new UnsupportedFlavorException(flavor);
	}
	
}
