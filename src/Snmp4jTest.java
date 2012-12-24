import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class Snmp4jTest {
	public static Snmp snmp;
	public Writer writer;
	
	public Snmp4jTest(String outfile) {
	
		try {
			writer = new Writer(outfile);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		TransportMapping transport;
		try {
			transport = new DefaultUdpTransportMapping();
			snmp = new Snmp(transport);
			USM usm = new USM(SecurityProtocols.getInstance(),
			                  new OctetString(MPv3.createLocalEngineID()), 0);
			SecurityModels.getInstance().addSecurityModel(usm);
			transport.listen();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void get() 
	{
		// TODO Pass in IP addresses
		// Replace IP1 to IP4 with real IP addresses.
		Session session1 = new Session("udp:"+IP1+"/161", writer);
		Session session2 = new Session("udp:"+IP2+"/161",writer);
		Session session3 = new Session("udp:"+IP3+"/161",writer);
		Session session4 = new Session("udp:"+IP4+"/161",writer);
		
		// creating PDU
		session1.getBulk("1.3.6.1.2.1.1.1");
		session1.getBulk("1.3.6.1.2.1.1.2");
		
		List<String> oList = new ArrayList<String>();
		oList.add("1.3.6.1.2.1.25.3.2.1.3.768");
		oList.add("1.3.6.1.2.1.25.3.2.1.3.770");
		session2.get(oList);
		
		oList.clear();
		oList.add("1.3.6.1.2.1.25.3.2.1.3.7170");
		session2.get(oList);
		
		oList.clear();
		oList.add("1.3.6.1.2.1.25.22.2.1.1.770");
		session2.getNext(oList);
		
		session1.walk("1.3.6.1.2.1.25.3.2.1.3");
		session2.walk("1.3.6.1.2.1.1");
		session3.walk("1.3.6.1.2.1.1");
		session4.walk("1.3.6.1.2.1.25.3.2.1.3");
	
		// wait for all sessions to complete
		while (!session1.isComplete() || !session2.isComplete() || !session3.isComplete() || !session4.isComplete())
		{
			session1.printStats();
			session2.printStats();
			session3.printStats();
			session4.printStats();
			try {
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//sleep for 100 ms

		}
		session1.printStats();
		session2.printStats();
		session3.printStats();
		session4.printStats();		
		writer.close();
	}
	
	public static void main(String args[]){
		if (args.length >= 1) {
			System.out.println("Start");
			Snmp4jTest getter = new Snmp4jTest(args[0]);
			getter.get();
			System.out.println("End");
		} else {
			System.out.println("Arguments missing.\nUsage: GetSNMP <outfile>");
		}
	}
}

