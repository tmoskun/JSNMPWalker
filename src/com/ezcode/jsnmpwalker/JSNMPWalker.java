package com.ezcode.jsnmpwalker;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

//import org.apache.commons.net.util.SubnetUtils;

import com.ezcode.jsnmpwalker.data.SNMPSessionOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.formatter.SNMPFormatter;
import com.ezcode.jsnmpwalker.worker.NetworkScanner;
import com.ezcode.jsnmpwalker.worker.SNMPSessionWorker;

import edazdarevic.commons.net.CIDRUtils;

//import org.apache.commons.cli.BasicParser;
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.CommandLineParser;
//import org.apache.commons.cli.Option;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;

public class JSNMPWalker extends SNMPSessionFrame {
	private static final int NUM_OF_THREADS = 20;
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static String GET_BULK = "1";
	public static String GET_NEXT = "2";
	public static String GET = "3";
	public static String WALK = "4";
	public static String[] OPERATIONS = { GET_BULK, GET_NEXT, GET, WALK };
	public static String YES = "Y";
	public static String NO = "N";
	public static String[] ANSWERS = { YES, NO };
	
	private ExecutorService _snmpService;
	private CountDownLatch _snmpLatch;
	private ExecutorService _netScanService;
	private CountDownLatch _netScanLatch;
	private Thread _snmpTerminationThread;
	private Thread _netScanTerminationThread;
	
	private Writer _writer = null;
	private boolean _debug = false;
	private SNMPFormatter _formatter;

	public JSNMPWalker() {
		this(false);
	}

	public JSNMPWalker(boolean debug) {
		super("SNMP run");
		_debug = debug;
		//_snmpWorkers = new ArrayList<SwingWorker>();
		//_snmpService = Executors.newFixedThreadPool(NUM_OF_THREADS);
		//_netScanWorkers = new ArrayList<SwingWorker>();
		//_netScanService = Executors.newFixedThreadPool(NUM_OF_THREADS);
		_formatter = new SNMPFormatter();	
	}
	
	public void loadDefaultMibs() {
        loadDefaultMib("RFC1155-SMI");
        loadDefaultMib("RFC1213-MIB");
        loadDefaultMib("SNMPv2-SMI");
        loadDefaultMib("SNMPv2-TC");
        loadDefaultMib("HOST-RESOURCES-MIB");
        refreshMibTree();
	}
	
	@Override
	public void scanNetwork(String ip, Integer mask, String netType) {
		try {
			if(ip.equalsIgnoreCase(NetworkScanner.LOCALHOST)) {
				if(isUnix()) {
					NetworkInterface eth0Interface = NetworkInterface.getByName("eth0");
					if(eth0Interface != null) {
					     Enumeration<InetAddress> iplist = eth0Interface.getInetAddresses();
					        InetAddress addr = null;
					        while (iplist.hasMoreElements()) {
					            InetAddress ad = iplist.nextElement();
					            if(!ad.isLoopbackAddress()) {
						            //byte bs[] = ad.getAddress();
						            if (netType.equalsIgnoreCase(NetworkScanner.IPv4) && (ad instanceof Inet4Address)) {
						                addr = ad;
						                break;
						            } else if(netType.equalsIgnoreCase(NetworkScanner.IPv6) && (ad instanceof Inet6Address) ) {
						            	addr = ad;
						            	break;
						            }
					            }
					        }

					        if (addr != null) {
					        	ip = addr.getHostAddress();
					        }
					}
				} else {
					InetAddress localhost = InetAddress.getLocalHost();
					ip = localhost.getHostAddress();
				} 
				/*
				Enumeration nets = NetworkInterface.getNetworkInterfaces();
				while(nets.hasMoreElements()) {
					NetworkInterface netint = (NetworkInterface) nets.nextElement();
			        System.out.println("Display name: " 
			                + netint.getDisplayName());
			        System.out.println("Hardware address: " 
			                + Arrays.toString(netint.getHardwareAddress()));
			        Enumeration addresses = netint.getInetAddresses();
			        while(addresses.hasMoreElements()) {
			        	Object address = addresses.nextElement();
			        	System.out.println("address " + address.getClass().getName() + " " + address);
			        }
				}
				*/
			} else {
				//try to resolve the address as a computer name
				try {
					InetAddress address = InetAddress.getByName(ip);
					ip = address.getHostAddress();
				} catch (Exception e) {
					//ignore
				}
			}
			String subnet = ip+"/"+mask.toString();
			CIDRUtils utils = new CIDRUtils(subnet);
			List<InetAddress> addresses = utils.getAllAddresses(NetworkScanner.TARGET_SIZES.get(netType));
			_netScanLatch = new CountDownLatch(addresses.size());
			_netScanService = Executors.newFixedThreadPool(NUM_OF_THREADS);
			_netScanTerminationThread = new TerminationThread(_netScanService, _netScanLatch, new Callable() {
				public Object call() {
					toggleNetScan(false);
					return null;
				}
			});
			toggleNetScan(true);
			for(InetAddress address: addresses) {
				SwingWorker worker = new NetworkScanner(address, this);
				_netScanService.submit(worker);
			}
			//SubnetUtils utils = new SubnetUtils(subnet);
			//String[] addresses = utils.getInfo().getAllAddresses();
			/*
			_netScanLatch = new CountDownLatch(addresses.length);
			_netScanService = Executors.newFixedThreadPool(NUM_OF_THREADS);
			toggleNetScan(true);
			for(int i = 0; i < addresses.length; i++) {
				//byte[] addr = addresses[i].getBytes();
				//SwingWorker worker = new NetworkScanner(addr, this);
				SwingWorker worker = new NetworkScanner(addresses[i], this);
				_netScanService.submit(worker);
			}
			*/
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Can't scan the network", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		//_netScanService.shutdown();
	}
	
	@Override
	public void stopScanning() {
		if(_netScanService != null) {
			try {
				_netScanService.shutdownNow();
			} catch(Exception e) {
				JOptionPane.showMessageDialog(null, "Can't cancel the process", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	@Override
	public void doneScan(SwingWorker worker) {
		done(_netScanLatch, _netScanService, worker, _netScanTerminationThread);	
	}

	public void runSNMP(ArrayList<SNMPTreeData> treeData,
			SNMPSessionOptionModel model, String filename) {
		if (_writer != null) {
			try {
				_writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		SNMPFormatter.restart();
		String header = _formatter.writeHeader();
		appendResult(header);
		if (filename != null && filename.length() > 0) {
			try {
				// reset();
				OutputStreamWriter fstream = new FileWriter(filename);
				_writer = new BufferedWriter(fstream);
				_writer.write(header);
			} catch (IOException e) {
				System.out.println("Can't create output stream");
				e.printStackTrace();
			}
		} else {
			_writer = null;
		}

		List<SNMPTreeData> data = this.getTreeData();
		_snmpLatch = new CountDownLatch(data.size());
		_snmpService = Executors.newFixedThreadPool(NUM_OF_THREADS);
		_snmpTerminationThread = new TerminationThread(_snmpService, _snmpLatch, new Callable() {
			public Object call() {
				toggleSNMPRun(false);
				return null;
			}
		});
		toggleSNMPRun(true);
		for (SNMPTreeData d : data) {
			SwingWorker worker = new SNMPSessionWorker(this, _formatter, d,
					getOptionModel(), _writer);
			_snmpService.submit(worker);
		}
	}

	public void stopSNMP() {
		if(_snmpService != null) {
			try {
				List<Runnable> tasks = _snmpService.shutdownNow();
				if (_writer != null) {
					try {
						_writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Can't cancel the process", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
//		if (_writer != null) {
//			try {
//				_writer.close();
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		}
	}
		
	public void doneSNMP(SwingWorker worker) {
		done(_snmpLatch, _snmpService, worker, _snmpTerminationThread);
	}
	
	private void done(final CountDownLatch latch, final ExecutorService service, final SwingWorker worker, Thread terminationThread) {
		if(latch != null && service != null) {
			latch.countDown();
			if(latch.getCount() == 0)
				service.shutdown();
			if(!terminationThread.isAlive() && (service.isShutdown() || worker.isCancelled())) {
				terminationThread.start();
			}
		}
	}
	
	private static boolean isUnix() {
		 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
	
	private class TerminationThread extends Thread {
		private ExecutorService _service;
		private CountDownLatch _latch;
		private Callable _toggleUI;
		
		protected TerminationThread(ExecutorService service, CountDownLatch latch, Callable toggleUI) {
			super();
			_service = service;
			_latch = latch;
			_toggleUI = toggleUI;
		}
		
		@Override
		public void run() {
			while(!_service.isTerminated()) {
				try {
					_service.awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {}
			}
			if (_latch.getCount() > 0)
				JOptionPane.showMessageDialog(null,
						"The SNMP run has been canceled");
			else
				JOptionPane.showMessageDialog(null,
						"The process has finished");
			//toggleRun(false);
			try {
				_toggleUI.call();
			} catch (Exception e) {}
		}
		
	}
	
	public static void main(String args[]) {
		//final Options opts = new Options();
		//opts.addOption(new Option("debug", false, "Debug output"));

		/*
		final CommandLineParser pars = new BasicParser();
		CommandLine comm = null;
		try {
			comm = pars.parse(opts, args);
		} catch (ParseException ex) {
			System.out.println("Problem parsing command line arguments");
			System.exit(1);
		}

		Snmp4jApplication app = new Snmp4jApplication(comm.hasOption("debug"));
		*/
		JSNMPWalker app = new JSNMPWalker(false);
		app.init();
		app.setVisible(true);
	}


}
