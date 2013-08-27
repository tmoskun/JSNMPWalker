package com.ezcode.jsnmpwalker;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.ezcode.jsnmpwalker.data.SNMPSessionOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.formatter.SNMPFormatter;
import com.ezcode.jsnmpwalker.publish.SNMPPublisher;
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
	private static final int NUM_OF_THREADS = 10;
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
	private ArrayList<SwingWorker> _snmpWorkers;
	private Thread _snmpPublisher;
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
		_snmpWorkers = new ArrayList<SwingWorker>();
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
	public void scanNetwork(String ip, Integer mask, String netType, boolean usePing, int timeout) {
		try {
			if(ip.equalsIgnoreCase(NetworkScanner.LOCALHOST)) {
				if(isUnix()) {
					InetAddress addr = null;
		        	NetworkInterface eth0Interface = NetworkInterface.getByName("eth0");
					addr = getLocalHostAddressLinux(eth0Interface, netType);
			        if (addr == null) {
						NetworkInterface wlan0Interface = NetworkInterface.getByName("wlan0");
			        	addr = getLocalHostAddressLinux(wlan0Interface, netType);
			        }
			        if (addr != null) {
			        	ip = addr.getHostAddress();
			        } else {
			        	JOptionPane.showMessageDialog(null, "Can't get your Unix/Linux localhost network interface. Please, check your network connection or try to enter your ip directly.");
			        	return;
			        }
				} else {
					InetAddress localhost = InetAddress.getLocalHost();
					ip = localhost.getHostAddress();
				} 
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
			boolean isWindows = isWindows();
			toggleNetScan(true);
			//String networkAddress = utils.getNetworkAddress();
			//String broadcastAddress = utils.getBroadcastAddress();
			List<InetAddress> broadcastAddresses = getBroadcastAddresses();
			for(InetAddress address: addresses) {
				String hostAddress = address.getHostAddress();
				//if(address.isLoopbackAddress() || hostAddress.equals(networkAddress) || hostAddress.equals(broadcastAddress)) {
				if(address.isLoopbackAddress() || broadcastAddresses.contains(address)) {
					_netScanLatch.countDown();
				} else {
					SwingWorker worker = new NetworkScanner(address, usePing, timeout, isWindows, this);
					_netScanService.submit(worker);
				}
			}
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "Can't scan the network", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private InetAddress getLocalHostAddressLinux(NetworkInterface netInterface, String netType) {
        InetAddress addr = null;
		try {
			if(netInterface != null && netInterface.isUp() && !netInterface.isLoopback()) {
			     Enumeration<InetAddress> iplist = netInterface.getInetAddresses();
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
			}
		} catch (SocketException e) {
			//ignore
		}
		return addr;
	}
	
	private List<InetAddress> getBroadcastAddresses() {
		List<InetAddress> broadcastAddresses = new ArrayList<InetAddress>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (!networkInterface.isLoopback()) {
					for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
						InetAddress broadcast = interfaceAddress.getBroadcast();
						if(broadcast != null) {
							broadcastAddresses.add(broadcast);
						}
					}
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return broadcastAddresses;
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

	public void runSNMP(ArrayList<SNMPTreeData> treeData, SNMPSessionOptionModel model, String filename) {
		closeWriter();
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
		_snmpWorkers.clear();
		BlockingQueue snmpQueue = new LinkedBlockingQueue();
		_snmpPublisher = new SNMPPublisher(this, snmpQueue, _writer);
		_snmpPublisher.start();
		resetOutputSearch();
		toggleSNMPRun(true);
		for (SNMPTreeData d : data) {
			SwingWorker worker = new SNMPSessionWorker(this, _formatter, d,
					getOptionModel(), snmpQueue);
			_snmpWorkers.add(worker);
			_snmpService.submit(worker);
		}
	}

	public void stopSNMP() {
		if(_snmpService != null) {
			try {
//				while(!_snmpWorkers.isEmpty()) {
//					SwingWorker worker = _snmpWorkers.remove(0);
//					worker.cancel(true);
//				}
				for(SwingWorker worker: _snmpWorkers) {
					worker.cancel(true);
				}
				_snmpPublisher.interrupt();
				List<Runnable> tasks = _snmpService.shutdownNow();
/*
				if (_writer != null) {
					try {
						_writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
*/
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Can't cancel the process", "Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
		
	public void doneSNMP(SwingWorker worker) {
		done(_snmpLatch, _snmpService, worker, _snmpTerminationThread);
	}
	
	private void done(final CountDownLatch latch, final ExecutorService service, final SwingWorker worker, Thread terminationThread) {
		if(latch != null && service != null) {
			if(!worker.isCancelled())
				latch.countDown();
			if(latch.getCount() == 0)
				service.shutdown();
			if(!terminationThread.isAlive() && (service.isShutdown() || worker.isCancelled())) {
				terminationThread.start();
			}
		}
	}
	
	@Override
	public void closeWriter() {
		if (_writer != null) {
			try {
				_writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	private static boolean isUnix() {		 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
	}
	
	private static boolean isWindows() {		 
		return (OS.indexOf("win") >= 0); 
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

	/*
	private class SNMPThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(final Runnable r) {
			return new Thread(r) {
				public void interrupt() {
					System.out.println(r.getClass().getName());
					super.interrupt();
				}
			};
		}
		
	}
	*/
	
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
