package com.ezcode.jsnmpwalker.worker;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.SwingWorker;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;

public class NetworkScanner extends SwingWorker<Object, Object> {
	public static final String LOCALHOST = "localhost";
	//public static final String LOCALHOST_IP = "127.0.0.1";
	
	public static final String IPv6 = "IPv6";
	public static final String IPv4 = "IPv4";
	public static final Hashtable<String, Integer> TARGET_SIZES;
	public static final Hashtable<String, Integer> PREFIX_DEFAULTS;
	public static final Hashtable<String, Integer> PREFIX_LENGTHS;
	static {
		TARGET_SIZES = new Hashtable<String, Integer>();
		TARGET_SIZES.put(NetworkScanner.IPv4, 4);
		TARGET_SIZES.put(NetworkScanner.IPv6, 16);
		PREFIX_LENGTHS = new Hashtable<String, Integer>();
		PREFIX_LENGTHS.put(NetworkScanner.IPv4, 32);
		PREFIX_LENGTHS.put(NetworkScanner.IPv6, 128);
		PREFIX_DEFAULTS = new Hashtable<String, Integer>();
		PREFIX_DEFAULTS.put(NetworkScanner.IPv4, 24);
		PREFIX_DEFAULTS.put(NetworkScanner.IPv6, 120);
	}
	
	//private String _ip;
	private InetAddress _address;
	private int _timeout;
	private SNMPSessionFrame _panel;

	public static final long NUM_OF_DEVICES_LIMIT = 1024;

	
//	public NetworkScanner(String ip, SNMPSessionFrame panel) {
//		_ip = ip;
//		_panel = panel;
//	}
	
	public NetworkScanner(InetAddress address, int timeout, SNMPSessionFrame panel) {
		_address = address;
		_timeout = timeout;
		_panel = panel;
	}

	@Override
	protected Object doInBackground() throws Exception {
		/*
		InetAddress[] addresses = InetAddress.getAllByName(_ip);
		for(int i = 0; i < addresses.length; i++) {
			InetAddress address = addresses[i];
			//System.out.println(address);
			if(address.isReachable(1000)) {
				publish(address);
			} 
		}
		*/
		if(_address.isReachable(_timeout)) {
			publish(_address);
		} 
		return _panel;
	}
	
	@Override
	protected void process(List<Object> chunks) {
		for(Object o: chunks) {
			if(o instanceof InetAddress) {
				_panel.addAddress((InetAddress) o);
			}
		}
	}
	
	@Override
	protected void done() {
		_panel.doneScan(this);
	}
	
	public static Long getNumberOfHosts(int prefixLength, int numOfBits) {
	    Double x = Math.pow(2, (numOfBits - prefixLength));

	    if (x < 0)
	        x = 0D;

	    return x.longValue();
	}	

}
