/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under MIT license
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Snmp4jApplication extends SNMPSessionFrame {
	private List<SwingWorker> _workers;
	private Writer _writer = null;
	private boolean _debug = false;
	private SNMPFormatter _formatter;

	public static String GET_BULK = "1";
	public static String GET_NEXT = "2";
	public static String GET = "3";
	public static String WALK = "4";
	public static String[] OPERATIONS = { GET_BULK, GET_NEXT, GET, WALK };
	public static String YES = "Y";
	public static String NO = "N";
	public static String[] ANSWERS = { YES, NO };

	public Snmp4jApplication() {
		this(false);
	}

	public Snmp4jApplication(boolean debug) {
		super("SNMP run");
		_debug = debug;
		_workers = new ArrayList<SwingWorker>();
		_formatter = new SNMPFormatter();
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
		for (SNMPTreeData d : data) {
			SwingWorker worker = new SNMPSessionWorker(this, _formatter, d,
					getModel(), _writer);
			_workers.add(worker);
		}
		toggleRun(true);
		for (SwingWorker worker : _workers) {
			worker.execute();
		}
	}

	public boolean stopSNMP() {
		boolean cancel = true;
		try {
			for (SwingWorker w : _workers) {
				boolean can = w.cancel(true);
				cancel = cancel && can;
				if (!cancel)
					return false;
			}
		} catch (Exception e) {
		}
		if (_writer != null) {
			try {
				_writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return cancel;
	}

	public void done(SwingWorker worker) {
		if (worker != null) {
			_workers.remove(worker);
			if (_workers.isEmpty()) {
				toggleRun(false);
				if (worker.isCancelled())
					JOptionPane.showMessageDialog(null,
							"The SNMP run has been canceled");
				else
					JOptionPane.showMessageDialog(null,
							"The process has finished");
			}
		}

	}

	public static void main(String args[]) {
		final Options opts = new Options();
		opts.addOption(new Option("debug", false, "Debug output"));

		final CommandLineParser pars = new BasicParser();
		CommandLine comm = null;
		try {
			comm = pars.parse(opts, args);
		} catch (ParseException ex) {
			System.out.println("Problem parsing command line arguments");
			System.exit(1);
		}

		Snmp4jApplication app = new Snmp4jApplication(comm.hasOption("debug"));
		app.init();
		app.setVisible(true);
	}
}
