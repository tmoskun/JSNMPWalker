package com.ezcode.jsnmpwalker.data;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

public class SNMPDeviceData {
	public static final DataFlavor SNMP_DEVICE_DATA_FLAVOR = new DataFlavor(SNMPDeviceData.class,
			SNMPDeviceData.class.getSimpleName());
	
	
	private String _ip = "";
	private SNMPOptionModel _optionModel = new SNMPOptionModel();
	
	public SNMPDeviceData(String ip) {
		_ip = ip;
	}
	
	public SNMPDeviceData(String ip, SNMPOptionModel optionModel) {
		_ip = ip;
		_optionModel = optionModel;
	}
	
	public SNMPDeviceData(SNMPDeviceData data) {
		_ip = data.getIp();
		_optionModel = new SNMPOptionModel(data.getOptions());
	}
	
	public String getIp() {
		return _ip;
	}
	
	public void setIp(String ip) {
		_ip = ip;
	}
	
	public Map<String, String> getOptions() {
		return _optionModel;
	}
	
	public String getOption(String key) {
		return _optionModel.get(key);
	}
	
	public void setOptions(Map<String, String> options) {
		_optionModel = new SNMPOptionModel(options);
	}
	
	public void setOption(String key, String value) {
		_optionModel.put(key, value);
	}
	
	public String toString() {
		return _ip;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof SNMPDeviceData) {
			SNMPDeviceData dat = (SNMPDeviceData) obj;
			return _ip.equalsIgnoreCase(dat.getIp()) && _optionModel.equals(dat.getOptions());
		} else {
			return false;
		}
	}

}
