package com.ezcode.jsnmpwalker.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.TransferableSNMPDeviceData;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public class ClipboardUtils {
	
	public static void setClipboardContents( ClipboardOwner owner, Object obj ){
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(obj instanceof String) {
			StringSelection stringSelection = new StringSelection( obj.toString() );
			clipboard.setContents( stringSelection, owner );
		} else if (obj instanceof SNMPDeviceData){
			TransferableSNMPDeviceData dataSelection = new TransferableSNMPDeviceData((SNMPDeviceData) obj, SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
			clipboard.setContents(dataSelection, owner);
		} else if (obj instanceof List) {
			List list = (List) obj;
			if(!list.isEmpty()) {
				Object first = list.get(0);
				if(first instanceof String) {
					StringBuilder str = new StringBuilder();
					for(Object s: list) {
						str.append(s);
						str.append("\n");
					}
					StringSelection stringSelection = new StringSelection( str.toString() );
					clipboard.setContents( stringSelection, owner );
				} else if(first instanceof SNMPDeviceData) {
					TransferableSNMPDeviceData dataSelection = new TransferableSNMPDeviceData((List<SNMPDeviceData>) obj, SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
					clipboard.setContents(dataSelection, owner);
				}
			}
		}
	}
	
	public static Object getClipboardContents() {
		Object result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		if(contents != null) {
			try {
				if (contents.isDataFlavorSupported(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR)) {
					result = contents.getTransferData(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
				} else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			    }
			} catch (UnsupportedFlavorException ex){
			    System.out.println(ex);
			    ex.printStackTrace();
			} catch (IOException ex) {
			    System.out.println(ex);
			    ex.printStackTrace();
			 }
		 }
		 return result;
	}

}
