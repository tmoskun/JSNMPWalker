/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under MIT license
 */

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
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


public class SNMPSessionWorker extends SwingWorker<Object, Object> {
	
	private SNMPFormatter _formatter;
	private Writer _writer;
	private SNMPSessionFrame _panel;
	private SNMPTreeData _treeData;
	private Map<String, String> _options;
	
	private CommunityTarget  _target;
	private ResponseListener _listener;
	private Map<Integer32,String> _walkList;
	// Needed because bulk responses come one at a time
	private Integer32 _lastResponseId;
	private long _lastResponseTime;
	
	private int _requests;
	private int _responses;
	private Snmp _snmp;
	
	public SNMPSessionWorker(SNMPSessionFrame panel, SNMPFormatter formatter, SNMPTreeData treeData, Map<String, String> options) {
		this(panel, formatter, treeData, options, null);
	}
	
	public SNMPSessionWorker(SNMPSessionFrame panel, SNMPFormatter formatter, SNMPTreeData treeData, Map<String, String> options, Writer w) {
		_panel = panel;
		_treeData = treeData;
		_options = options;
		_formatter = formatter;
		
		_snmp = SnmpSingleton.getSnmp();
		_writer = w;
		_requests = 0;
		_responses = 0;
		_walkList = new HashMap<Integer32,String>();

		_lastResponseTime = 0;

		Address targetAddress = GenericAddress.parse("udp:"+_treeData.getIp()+"/"+options.get(SNMPSessionOptionModel.PORT_KEY));
	    // setting up target
		_target = new CommunityTarget();
		_target.setCommunity(new OctetString(options.get(SNMPSessionOptionModel.COMMUNITY_KEY)));
		_target.setAddress(targetAddress);
		_target.setRetries(Integer.parseInt(options.get(SNMPSessionOptionModel.RETRIES_KEY)));
		_target.setTimeout(Integer.parseInt(options.get(SNMPSessionOptionModel.TIMEOUT_KEY)));
		int version = SNMPSessionOptionModel.getVersion(options.get(SNMPSessionOptionModel.SNMP_VERSION_KEY));
		_target.setVersion(version);
		
		if(version == SnmpConstants.version3) {
			_target.setSecurityLevel(SNMPSessionOptionModel.getSecurityLevel(options.get(SNMPSessionOptionModel.SECURITY_LEVEL_KEY)));
			UsmUser user = new UsmUser(new OctetString(options.get(SNMPSessionOptionModel.SECURITY_NAME_KEY)), SNMPSessionOptionModel.getAuthenticationType(options.get(SNMPSessionOptionModel.AUTH_TYPE_KEY)), new OctetString(options.get(SNMPSessionOptionModel.AUTH_PASSPHRASE_KEY)),
					                   SNMPSessionOptionModel.getPrivacyType(options.get(SNMPSessionOptionModel.PRIV_TYPE_KEY)), new OctetString(options.get(SNMPSessionOptionModel.PRIV_PASSPHRASE_KEY)));
			// add user to the USM
			_snmp.getUSM().addUser(user.getSecurityName(), user);
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
			    publish(new PDUData(event.getPeerAddress(), pdu, getResponseTime(pdu)));
			    // check if it's a walk
			    Integer32 oldReqId = pdu.getRequestID();
			    String oid = _walkList.get(oldReqId);
			    if (oid != null)
			    {
			    	// it's a walk
			    	_walkList.remove(oldReqId);
			    	// get the last one, in case it's bulk
			    	String retOid = pdu.getVariableBindings().get(pdu.getVariableBindings().size()-1).getOid().toString();
			    			
			    	if (!Null.isExceptionSyntax(pdu.get(0).getVariable().getSyntax()) && retOid.startsWith(oid) && !retOid.equals(oid))
			    	{
			    		Integer32 reqId = walkCore(retOid);
			    		//--do some bookkeeping
			    		_walkList.put(reqId, oid); // keep the original OID
			    	}
			    }
			 }
		};	
		
	}
	
	@Override
	protected Object doInBackground() throws Exception {
		String commandName = _treeData.getCommand();
		String command = Character.toLowerCase(commandName.charAt(0)) + commandName.substring(1);
		String oid = _treeData.getOid();
		try {
			Method meth = this.getClass().getMethod(command, String.class);
			meth.invoke(this, oid);
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
		return _panel;
	}
	
	@Override
	protected void process(List<Object> chunks) {
		for(Object o: chunks) {
			if(o instanceof PDUData) {
				PDUData d = (PDUData) o;
				String result = d.toString();
				_panel.appendResult(d.toString());
				if(_writer != null) {
					try {
						_writer.write(result);
						_writer.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}	
		}
	}
	
	
	@Override
	protected void done() {
		_panel.done(this);
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
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETBULK);
		pdu.setMaxRepetitions(16);
		
		int reqId = _snmp.getNextRequestID();
		pdu.setRequestID(new Integer32(reqId));
		send(pdu);
		//--register since this is a walk
	}
	
	public void get(List<String> oids) {
		PDU pdu = new PDU();
		for (String oid: oids) {
			pdu.add(new VariableBinding(new OID(oid)));
		}
		pdu.setType(PDU.GET);
		
		int reqId = _snmp.getNextRequestID();
		pdu.setRequestID(new Integer32(reqId));
		send(pdu);
	}
	
	public void getNext(List<String> oids) {
		PDU pdu = new PDU();
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
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID(oid)));
		pdu.setType(PDU.GETNEXT);
		Integer32 reqId = new Integer32(_snmp.getNextRequestID());
		pdu.setRequestID(reqId);
		send(pdu);
		return reqId;
	}
		
	
	private void send(PDU pdu) {
		try {
			publish(new PDUData(_target.getAddress(), pdu));
			_snmp.send(pdu, _target, null, _listener);
			++_requests;
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public boolean isComplete() {
		return ((_responses >= _requests) && _walkList.isEmpty());
	}
	
	public void printStats()
	{
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
