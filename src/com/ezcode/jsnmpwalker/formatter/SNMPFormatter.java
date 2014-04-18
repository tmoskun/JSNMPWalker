package com.ezcode.jsnmpwalker.formatter;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import org.snmp4j.PDU;
import org.snmp4j.smi.Address;


public class SNMPFormatter {
	
	private static long _startTime;
	
	public SNMPFormatter() {
		restart();
	}
	
	public static void restart() {
		_startTime = System.currentTimeMillis();
	}
	
	public static long getElapsedTime() {
		return System.currentTimeMillis()-_startTime;
	}
	
	public String writeLine(String text) {
		return text+'\n';
	}
	
	public String writePDU(Address session, PDU pdu)
	{
		return writeLine(pduToString(session,pdu, getElapsedTime()));
	}
	
	public String writePDU(Address session, PDU pdu, long elapsedTime)
	{
		return writeLine(pduToString(session,pdu, elapsedTime));
	}
	
	public String writeHeader() {
		return writeLine(getHeader());
	}
	
	public String getHeader() {
		return "\nTime\tSession\tType\tRequestId\tStatus\tSyntax\tOID\tValue";
	}
	
	public static String pduToString(Address session, PDU pdu, long elapsedTime) {
	    StringBuffer buf = new StringBuffer();
	    boolean isRequest = ((pdu.getType() != PDU.GET) && (pdu.getType() != PDU.GETBULK) && (pdu.getType() != PDU.GETNEXT));
	    for (int i=0; i<pdu.getVariableBindings().size(); i++) {
	    	if (i != 0)
	    		buf.append("\n");
	    	buf.append(elapsedTime/1000F);  //time
	    	// remove the /161 port from the session
	    	int idx = session.toString().indexOf("/");
	    	if (idx == -1) {
	    		buf.append("\t"+session.toString()); // session
	    	} else {
	    		buf.append("\t"+session.toString().substring(0, idx)); // session		
	    	}
	    	buf.append("\t"+PDU.getTypeString(pdu.getType())); // command
	    	buf.append("\t"+pdu.getRequestID()); // regId
	    	buf.append("\t"+pdu.getErrorStatus()+"("+pdu.getErrorStatusText()+")"); // status
	    	buf.append("\t"); // syntax
	    	if (isRequest) {
	    		buf.append(pdu.getVariableBindings().get(i).getVariable().getSyntaxString()); 
	    	}
	    	buf.append("\t"+pdu.getVariableBindings().get(i).getOid()); // OID
	    	buf.append("\t"); // value
	    	if (isRequest) {
	    		buf.append(pdu.getVariableBindings().get(i).toValueString());
	    	}
	    }
	    return buf.toString();
	}

}
