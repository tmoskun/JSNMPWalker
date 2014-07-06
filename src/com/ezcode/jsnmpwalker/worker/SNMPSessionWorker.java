package com.ezcode.jsnmpwalker.worker;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */


import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.swing.SwingWorker;

import org.snmp4j.AbstractTarget;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.formatter.SNMPFormatter;


public class SNMPSessionWorker extends SwingWorker<Object, Object> {
	
	private SNMPFormatter _formatter;
//	private Writer _writer;
	private SNMPSessionFrame _panel;
	private BlockingQueue<String> _queue;
	private SNMPTreeData _treeData;
	private Map<String, String> _options;
	
	private AbstractTarget _target;
	private ResponseListener _listener;
	private Map<Integer32,String> _walkList;
	// Needed because bulk responses come one at a time
	private Integer32 _lastResponseId;
	private long _lastResponseTime;
	
	private int _requests;
	private int _responses;
	private Snmp _snmp;
	private int _version;
	
	
	public SNMPSessionWorker(SNMPSessionFrame panel, SNMPFormatter formatter, SNMPTreeData treeData, BlockingQueue<String> queue) {
		
		_panel = panel;
		_queue = queue;
		_treeData = treeData;
		Map<String, String> options = _treeData.getOptionModel();
		_formatter = formatter;
		
		_snmp = SnmpSingleton.getSnmp();
		//_writer = w;
		_requests = 0;
		_responses = 0;
		_walkList = new HashMap<Integer32,String>();

		_lastResponseTime = 0;

		Address targetAddress = GenericAddress.parse("udp:"+_treeData.getIp()+"/"+options.get(SNMPOptionModel.PORT_KEY));
		int retries = Integer.parseInt(options.get(SNMPOptionModel.RETRIES_KEY));
		int timeout = Integer.parseInt(options.get(SNMPOptionModel.TIMEOUT_KEY));
		// setting up community target

		_version = SNMPOptionModel.getVersion(options.get(SNMPOptionModel.SNMP_VERSION_KEY));
		
		if(_version == SnmpConstants.version3) {
			try {
				OctetString securityName = new OctetString(options.get(SNMPOptionModel.SECURITY_NAME_KEY));
				int securityLevel = SNMPOptionModel.getSecurityLevel(options.get(SNMPOptionModel.SECURITY_LEVEL_KEY));
				_target = new UserTarget();
				_target.setAddress(targetAddress);
				_target.setRetries(retries);
				_target.setTimeout(timeout);
				_target.setVersion(SnmpConstants.version3);
				_target.setSecurityLevel(securityLevel);
				_target.setSecurityName(securityName);
				

				OctetString authPass = null; 
				OctetString privPass = null;
				OID authType = null;
				OID privType = null;
				
				if(securityLevel == SecurityLevel.AUTH_PRIV || securityLevel == SecurityLevel.AUTH_NOPRIV) {
					authType = SNMPOptionModel.getAuthenticationType(options.get(SNMPOptionModel.AUTH_TYPE_KEY));
					String authPassStr = options.get(SNMPOptionModel.AUTH_PASSPHRASE_KEY);
					authPass = new OctetString(authPassStr); 
					if(securityLevel == SecurityLevel.AUTH_PRIV) {
						privType = SNMPOptionModel.getPrivacyType(options.get(SNMPOptionModel.PRIV_TYPE_KEY));
						String privPassStr = options.get(SNMPOptionModel.PRIV_PASSPHRASE_KEY);
						privPass = new OctetString(privPassStr);
					}
				}
				   		
				UsmUser user = new UsmUser(securityName, authType, authPass, privType, privPass);
				// add user to the USM
				_snmp.getUSM().addUser(user.getSecurityName(), user);
			} catch (IllegalArgumentException ex) {
				System.out.println("Illegal arguments");
				ex.printStackTrace();
			} 
		} else {
			_target = new CommunityTarget();
			((CommunityTarget) _target).setCommunity(new OctetString(options.get(SNMPOptionModel.COMMUNITY_KEY)));
			_target.setAddress(targetAddress);
			_target.setRetries(retries);
			_target.setTimeout(timeout);
			_target.setVersion(_version);
		}
		
		// set up a listener to use. 
		_listener = new ResponseListener() {
			  public void onResponse(ResponseEvent event) {
			    // Always cancel async request when response has been received
			    // otherwise a memory leak is created! Not canceling a request
			    // immediately can be useful when sending a request to a broadcast
			    // address.
			    ((Snmp)event.getSource()).cancel(event.getRequest(), this);
			    ++_responses;

			    PDU pdu = event.getResponse();
			    //not reachable
			    if(pdu == null) {
			    	_walkList.remove(event.getRequest().getRequestID());
			    	return;
			    }
			    if(!SNMPSessionWorker.this.isCancelled()) {
			    	enqueue(new PDUData(event.getPeerAddress(), pdu, getResponseTime(pdu)));
				    // check if it's a walk
				    Integer32 oldReqId = pdu.getRequestID();
				    String oid = _walkList.get(oldReqId);
			    	// it's a walk
				    if (oid != null) {
				    	// get the last one, in case it's bulk
				    	String retOid = pdu.getVariableBindings().get(pdu.getVariableBindings().size()-1).getOid().toString();
				    	if (!SNMPSessionWorker.this.isCancelled() && !Null.isExceptionSyntax(pdu.get(0).getVariable().getSyntax()) && retOid.startsWith(oid) && !retOid.equals(oid)) {
				    		Integer32 reqId = walkCore(retOid);
					    	//--do some bookkeeping
					    	_walkList.put(reqId, oid); // keep the original OID
				    	}
				    }
			    	_walkList.remove(oldReqId);
			    }
			 }
		};	
		
	}
	
	@Override
	protected Object doInBackground() throws Exception {
		String commandName = _treeData.getCommand();
		String command = Character.toLowerCase(commandName.charAt(0)) + commandName.substring(1);
		List<String> oids = _treeData.getOids();
		if(oids.size() > 0) {
			try {
				if(SNMPTreeData.isMultiOIDMethod(commandName)) {
					Method meth = this.getClass().getDeclaredMethod(command, List.class);
					meth.invoke(this, oids);
				} else {
					Method meth = this.getClass().getDeclaredMethod(command, String.class);
					meth.invoke(this, oids.get(0));
				}
			} catch(Exception e) {
				System.out.println("Method " + command + " doesn't exist or can't invoke the method");
				e.printStackTrace();
			}
			while(!this.isComplete()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}//sleep for 100 ms
			}
		}
		return _panel;
	}
	
	private void enqueue(PDUData data) {
		_queue.add(data.toString());
	}
	
		
	@Override
	protected void done() {
		_panel.doneSNMP(this);
	}

	private long getResponseTime(PDU pdu) {
		if (_lastResponseId != null) {
			if (_lastResponseId.equals(pdu.getRequestID()))
			{
				return _lastResponseTime;
			}
		}
		_lastResponseTime = SNMPFormatter.getElapsedTime();
		_lastResponseId = pdu.getRequestID();
		return _lastResponseTime;
	}
	
	public void getBulk(String oid) {
		PDU pdu = PDUforVersion.getPDU(_version);
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETBULK);
		pdu.setMaxRepetitions(16);
		
		int reqId = _snmp.getNextRequestID();
		pdu.setRequestID(new Integer32(reqId));
		send(pdu);
		//--register since this is a walk
	}
	
	public void get(List<String> oids) {
		PDU pdu = PDUforVersion.getPDU(_version);
		for (String oid: oids) {
			pdu.add(new VariableBinding(new OID(oid)));
		}
		pdu.setType(PDU.GET);
		
		int reqId = _snmp.getNextRequestID();
		pdu.setRequestID(new Integer32(reqId));
		send(pdu);
	}
	
	public void getNext(List<String> oids) {
		PDU pdu = PDUforVersion.getPDU(_version);
		for (String oid: oids) {
			pdu.add(new VariableBinding(new OID(oid)));
		}
		int reqId = _snmp.getNextRequestID();
		pdu.setRequestID(new Integer32(reqId));
		pdu.setType(PDU.GETNEXT);
		send(pdu);
	}
	
	public void walk(String oid) {
		Integer32 reqId = walkCore(oid);
		_walkList.put(reqId, oid);
	}
	
	private Integer32 walkCore(String oid) {
		PDU pdu = PDUforVersion.getPDU(_version);
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETNEXT);
		Integer32 reqId = new Integer32(_snmp.getNextRequestID());
		pdu.setRequestID(reqId);
		send(pdu);
		return reqId;
	}
	
		
	private void send(PDU pdu) {
		try {
			enqueue(new PDUData(_target.getAddress(), pdu));
			_snmp.send(pdu, _target, null, _listener);
			++_requests;
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public boolean isComplete() {
		return this.isCancelled() || ((_responses >= _requests) && _walkList.isEmpty());
	}
	
	public void printStats() {
		if (isComplete())
			System.out.println(_target.getAddress()+": Completed. "+_requests+" request, "+_responses+" responses");
		else
			System.out.println(_target.getAddress()+": In Progress. "+_requests+" request, "+_responses+" responses");

	}
	
	private static class SnmpSingleton {
		private static Snmp _snmp = null;
		
		private SnmpSingleton() {}

		public static Snmp getSnmp() {
			if(_snmp == null) {
				TransportMapping transport;
				try {
					transport = new DefaultUdpTransportMapping();
					_snmp = new Snmp(transport);
					USM usm = new USM(SecurityProtocols.getInstance(),
					                  new OctetString(MPv3.createLocalEngineID()), 0);
					SecurityModels.getInstance().addSecurityModel(usm);
					transport.listen();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
			return _snmp;
		}	
		
	}
	
	private static class PDUforVersion {
		
		public static PDU getPDU(int version) {
			if(version == SnmpConstants.version1) {
				return new PDU();
			} else if(version == SnmpConstants.version2c) {
				return new PDU();
			} else if(version == SnmpConstants.version3) {
				return new ScopedPDU();
			} else {
				return new PDU();
			}
		}
	}
	
	private class PDUData {
		private Address _session;
		private PDU _pdu;
		private long _elapsedTime;
		
		public PDUData(Address sess, PDU pdu) {
			this(sess, pdu, SNMPFormatter.getElapsedTime());
		}
		
		public PDUData(Address sess, PDU pdu, long elapsedTime) {
			_session = sess;
			_pdu = pdu;
			_elapsedTime = elapsedTime;
		}
		
		public String toString() {
			return _formatter.writePDU(_session, _pdu, _elapsedTime);
		}
		
		
	}
	

}
