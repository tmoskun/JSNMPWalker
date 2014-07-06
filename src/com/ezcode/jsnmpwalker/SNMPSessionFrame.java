package com.ezcode.jsnmpwalker;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import net.percederberg.mibble.MibLoaderException;

import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.command.TreeNodeCommandStack;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.menu.SNMPMenuBar;
import com.ezcode.jsnmpwalker.panel.DataPanel;
import com.ezcode.jsnmpwalker.panel.DevicePanel;
import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.panel.MibTreePanel;
import com.ezcode.jsnmpwalker.panel.SNMPOutputPanel;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.storage.SNMPConfigurationStorage;

public abstract class SNMPSessionFrame extends JFrame {

	public static int WIDTH = 1800;
	public static int HEIGHT = 1000;

	private static final String[] FILTERS = {"Type", "Access", "Status", "Units", "Hint", "ModuleID", "Enums", "Indexes"};
	private JTree _tree;
	private DefaultTreeModel _treeModel;
	private SNMPTreePanel _treePane;
	private DataPanel _dataPane;
	private MibBrowserPanel _mibBrowserPane;
	private SNMPOutputPanel _outputPane;
	
	private TreeNodeCommandStack _commandStack;
	
	//private Map<String, JTextField> _fields;
	//private SNMPSessionOptionModel _optionModel;

	//private String _logFile = "";
	
	private SNMPConfigurationStorage _confStorage;
	
	
	private JButton _runSNMPButton;
	private JButton _cancelSNMPButton;
	
	public SNMPSessionFrame() {
		this("Editable Tree");
	}
	
	public SNMPSessionFrame(String title) {
		super(title);
		//size of the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//height of the task bar
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		int taskBarSize = scnMax.bottom;
		int maxWidth = screenSize.width;
		int maxHeight = screenSize.height - taskBarSize;
		
		if(WIDTH > maxWidth) {
			WIDTH = maxWidth;
		}
		if(HEIGHT > maxHeight) {
			HEIGHT = maxHeight;
		}
			
		setSize(WIDTH, HEIGHT);
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.put("TextPane.selectionForeground", Color.GREEN.darker());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		//_fields = new Hashtable<String, JTextField>();
		//_optionModel = new SNMPSessionOptionModel();
	
		_confStorage = new SNMPConfigurationStorage();
		
		
		_runSNMPButton = new JButton("Run SNMP");
		_cancelSNMPButton = new JButton("Stop");
		
		_commandStack = new TreeNodeCommandStack();
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	protected void init() {
		
		_mibBrowserPane = new MibBrowserPanel(this);
		_outputPane = new SNMPOutputPanel(this);
			
		//left panel
		JPanel leftPane = new JPanel(new BorderLayout());

		JPanel snmpPane = new JPanel(new BorderLayout());
		
		//Data panel: MIBS and network devices
		_dataPane = new DataPanel(this);
		loadDefaultMibs();
		
		//Tree to set up data: commands, ips and oids
		_treePane = new SNMPTreePanel(this, (MibTreePanel) _dataPane.getMibPanel(), _commandStack);
		_tree = _treePane.getTree();
		_treeModel = (DefaultTreeModel) _tree.getModel();
		
		snmpPane.add(_treePane, BorderLayout.CENTER);
		
		//((MibPanel) _dataPane.getMibPanel()).printPreorder();
		
		JSplitPane leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, snmpPane, _dataPane);
		leftSplitPane.setBorder(null);
		leftSplitPane.setOneTouchExpandable(true);
		leftSplitPane.setDividerLocation(WIDTH/4 - 50);
		Dimension leftMinSize = new Dimension(150, 600);
		snmpPane.setMinimumSize(leftMinSize);
		_dataPane.setMinimumSize(leftMinSize);
		leftPane.add(leftSplitPane, BorderLayout.CENTER);
		
		JPanel southPane = new JPanel(new BorderLayout());
		southPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				
		JPanel centerPane = new JPanel(new BorderLayout());
		
/*		
		//Community and SNMP version fields
		JPanel opts = new JPanel(new WrapLayout(FlowLayout.LEFT));
		opts.add(new JLabel("Community"));
		final JTextField community = new JTextField(_optionModel.get(SNMPSessionOptionModel.COMMUNITY_KEY));
		community.setPreferredSize(new Dimension(120, 20));
		opts.add(community);
		community.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				update();			
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();			
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();		
			}
			private void update() {
				_optionModel.put(SNMPSessionOptionModel.COMMUNITY_KEY, community.getText());
			}
			
		});
		//opts.add(new JLabel("         "));
		opts.add(Box.createHorizontalStrut(10));
		opts.add(new JLabel("SNMP version"));
		ButtonGroup vergroup = new ButtonGroup();
		
		String snmpverdefault = _optionModel.get(SNMPSessionOptionModel.SNMP_VERSION_KEY);
		JRadioButton v1 = new JRadioButton("1", snmpverdefault.equals("1"));
		JRadioButton v2c = new JRadioButton("2c", snmpverdefault.equals("2c"));
		JRadioButton v3 = new JRadioButton("3", snmpverdefault.equals("3"));
		SNMPRadioButtonListener verlist = new SNMPRadioButtonListener(_optionModel, SNMPSessionOptionModel.SNMP_VERSION_KEY);
		v1.addActionListener(verlist);
		v2c.addActionListener(verlist);
		v3.addActionListener(verlist);
		vergroup.add(v1);
		vergroup.add(v2c);
		vergroup.add(v3);	
		opts.add(v1);
		opts.add(v2c);
		opts.add(v3);
		
		centerPane.add(opts, BorderLayout.NORTH);
*/
		
/*
		//TODO: filters	
		JPanel filts = new JPanel(new GridLayout(0, 2, 10, 10));

		filts.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Filters"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		for(String o: FILTERS) {			
			JTextField f = new JTextField(FlowLayout.RIGHT);
			f.setPreferredSize(new Dimension(100, 20));
			_fields.put(o, f);
			filts.add(new FormField(o, f));
		}	
		centerPane.add(filts, BorderLayout.CENTER);
*/		
		
		southPane.add(centerPane, BorderLayout.CENTER);
				
		//action buttons
		JPanel runp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		_cancelSNMPButton.setMnemonic(KeyEvent.VK_S);
		_cancelSNMPButton.setEnabled(false);
		_runSNMPButton.setMnemonic(KeyEvent.VK_R);
		_runSNMPButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String logFile = _outputPane.getLogFile();
				//_treePane.getTree().cancelEditing();
				ArrayList<SNMPTreeData> treeData = getTreeData();
				if(treeData.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Commands, IP or OID data are not provided", "Data not provided", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(logFile != null && logFile.length() > 0) {
					File log = new File(logFile);
					if(log.isDirectory()) {
						int result = JOptionPane.showConfirmDialog(null, "The log file is a directory. Would you like to write log into " + logFile + ".txt?", "Confirm file name", JOptionPane.OK_CANCEL_OPTION);
						if(result != JOptionPane.OK_OPTION) 
							return;
					}
					if(!log.getName().endsWith(".txt")) {
						logFile = log.getAbsolutePath() + ".txt";
						log = new File(logFile);
					}
					if(log.exists()) {
						int result = JOptionPane.showConfirmDialog(null, "The file already exists, do you want to override it?", "Confirm override", JOptionPane.OK_CANCEL_OPTION);
						if(result != JOptionPane.OK_OPTION) {
							return;
						} 
					} else {
						try {
							log.createNewFile();
						} catch (IOException e) {
							System.out.println("Can't create a file");
							e.printStackTrace();
						}
					}
				}
				
				//runSNMP(treeData, getOptionModel(), logFile);
				runSNMP(treeData, logFile);
			}
			
		});
		//set keystrokes for action buttons
		_runSNMPButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "Run");
		_runSNMPButton.getActionMap().put("Run", new ButtonAction(this, _runSNMPButton));
		runp.add(_runSNMPButton);
		
		_cancelSNMPButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopSNMP();
				//if(!stopSNMP()) {
				//	JOptionPane.showMessageDialog(null, "Can't cancel the process", "Warning", JOptionPane.WARNING_MESSAGE);
				//}
			}			
		});
		_cancelSNMPButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "Cancel");
		_cancelSNMPButton.getActionMap().put("Cancel", new ButtonAction(this, _cancelSNMPButton));
		runp.add(_cancelSNMPButton);
		
		southPane.add(runp, BorderLayout.SOUTH);
		leftPane.add(southPane, BorderLayout.SOUTH);
				
		//right panel
		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _mibBrowserPane, _outputPane);
		Dimension rightPanelMinSize = new Dimension(300, 200);
		rightSplitPane.setOneTouchExpandable(true);
		rightSplitPane.setDividerLocation(HEIGHT/2);
		_mibBrowserPane.setMinimumSize(rightPanelMinSize);
		_outputPane.setMinimumSize(rightPanelMinSize);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightSplitPane);
		splitPane.setBorder(null);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(WIDTH/2);
		Dimension panelMinSize = new Dimension(300, 800);
		leftPane.setMinimumSize(panelMinSize);
		rightSplitPane.setMinimumSize(panelMinSize);
		getContentPane().add(splitPane);
		
		//Menu Bar
		this.setJMenuBar(new SNMPMenuBar(this, ((MibTreePanel)_dataPane.getMibPanel()).getMibMenu(), _commandStack));
			
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				if(promptCloseApp()) {				
					super.windowClosing(e);
					closeScanning();
					System.exit(0);
				}
			}

			public void windowClosed(WindowEvent e) {
				stopSNMP();
			}
		});

	}
	
	public void closeScanning() {
		stopScanning();
		stopSNMP();
		closeWriter();
	}
	

	public boolean promptCloseApp() {
		int result = showConfirmDialog("Would you like to save the configuration before closing?", "Application closing");
		if(result == 0 || result == 1) {
			String path = _confStorage.getPath();
			if(path == null || result == 1) {
				path = getSaveConfigPath();
			}
			if(path != null) {
				//_confStorage.saveConfiguration(_treeModel, _optionModel, path);
				_confStorage.saveConfiguration(_treeModel, path);
				return true;
			} else {
				return false;
			}
		} 
		return result > 0 && result != 3;
	}
	
	private static int showConfirmDialog(String msg, String title) {
		String[] text = { "Save", "Save As...", "Discard", "Cancel" };
		Map<String, Character> mnemonics = new HashMap<String, Character>();
		Map<String, Integer> indexes = new HashMap<String, Integer>();
		mnemonics.put(text[0], 'S');
		mnemonics.put(text[1], 'A');
		indexes.put(text[1], 5);
		mnemonics.put(text[2], 'D');
		mnemonics.put(text[3], 'C');
		// Create option pane and dialog.
		JOptionPane pane = new JOptionPane(msg, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, text, text[0]);
		JDialog dialog = pane.createDialog(null, title);
		
		// Add mnemonics to buttons.	
		Vector<Component> comps = new Vector<Component>();
		JRootPane rootPane = dialog.getRootPane();
		getSubcomponents(rootPane, comps);
		for(Component comp: comps) {
			if(comp instanceof AbstractButton) {
				AbstractButton butt = (AbstractButton) comp;
				String name = butt.getText();
				Character mnemonic = mnemonics.get(name);
				if(mnemonic != null) {
					butt.setMnemonic(mnemonic);
					butt.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed " + mnemonic), name);
					butt.getActionMap().put(name, new ButtonAction(dialog, butt));
				}
				Integer index = indexes.get(butt.getText());
				if(index != null) {
					butt.setDisplayedMnemonicIndex(index);
				}
			}
		}	
		dialog.setVisible(true);

		int result = JOptionPane.CLOSED_OPTION;

		Object selectedValue = pane.getValue();
		if(selectedValue != null) {
		    if(selectedValue instanceof String) {
		    	result = Arrays.asList(text).indexOf(selectedValue);
		    }
		}

		return result;
    }
	
	private static void getSubcomponents(Component c, Vector<Component> v) {
        if (c == null) {
            return;
        }
        if (c instanceof Container) {
            Component[] children = ((Container) c).getComponents();
            for (int i = 0; i < children.length; i++) {
                v.addElement(children[i]);
                if (children[i] instanceof Container) {
                    getSubcomponents(children[i], v);
                } 
            }
        }
	}
	
	public void loadDefaultMib(String src) {
		try {
			((MibTreePanel) _dataPane.getMibPanel()).loadDefaultMib(src);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MibLoaderException e) {
            e.getLog().printTo(new PrintStream(System.out));
		}
	}
	
	public String getSaveConfigPath() {
		String path = null;
		FileDialog fd = new FileDialog(SNMPSessionFrame.this, "Save As...", FileDialog.SAVE);
		fd.setVisible(true);
		String filename = fd.getFile();
		if(filename != null) {
			if(!filename.endsWith(".xml")) {
				filename = filename + ".xml";
			}
			path = fd.getDirectory() + filename;
			File f = new File(path);
			if(!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e1) {
					System.out.println("Can't create file");
					e1.printStackTrace();
					path = null;
				}
			}
		}
		return path;
	}

/*
	public SNMPSessionOptionModel getOptionModel() {
		return _treePane.getOptionModel();
	}
*/
	
	public SNMPConfigurationStorage getConfStorage() {
		return _confStorage;
	}
	
	public SNMPTreePanel getTreePane() {
		return _treePane;
	}
	
	protected  ArrayList<SNMPTreeData> getTreeData() {
		ArrayList<SNMPTreeData> treeData = new ArrayList<SNMPTreeData>();
		Object root = _treeModel.getRoot();
		//walk(treeData, getChildren(root));
		if(!walk(treeData, getChildren(root))) {
			if(JOptionPane.showConfirmDialog(null, "Some of the data was invalid. Continue with the rest?", "Invalid data", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				treeData.clear();
			}
		}
		return treeData;
	}
	

	private boolean walk(ArrayList<SNMPTreeData> data, Enumeration<Object> children) {
		boolean valid = true;
		while(children.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			Object obj = node.getUserObject();
			if(obj != null && obj.toString().length() >  0) {
				TreeNode[] path = node.getPath();
				//0 - root, 1 - command, 2 - ip, 3 - oid
				String command = (String) ((DefaultMutableTreeNode) path[1]).getUserObject();
				if((path.length == 3 && SNMPTreeData.isMultiOIDMethod(command)) || (path.length == 4 && _treeModel.isLeaf(node))) {
					SNMPTreeData row = new SNMPTreeData(path);
					if(valid = row.isValidCommand()) {
						if(path.length == 4) {
							String oid = (String) node.getUserObject();
							if(valid &= verifyOID(oid)) {
								row.addOID(oid);
							}
						} else if(path.length == 3) {
							Enumeration<DefaultMutableTreeNode> oidNodes = getChildren(node);
							while(oidNodes.hasMoreElements()) {
								valid &= verifyOIDNode(oidNodes.nextElement());
							}
							if(valid) {
								row.addAllOIDs(node);
							}	
						}
						valid &= row.getOids().size() > 0;
					}
					if(valid) {
						data.add(row);
					} 
				} else {		
					Enumeration ch = getChildren(node);
					valid &= walk(data, ch);
				}
			}
		}
		return valid;
	}
	
	private boolean verifyOIDNode(DefaultMutableTreeNode node) {
		return verifyOID((String) node.getUserObject());
	}
	
	private boolean verifyOID(String oid) {
		boolean valid = true;
		if(!(valid = SNMPTreeData.isValidOID(oid))) {
			checkMIB(oid);
		}
		return valid;
	}
	
	private Enumeration getChildren(Object o) {
		return ((DefaultMutableTreeNode) o).children(); 	
	}
	
	private void checkMIB(String oid) {
		MibTreePanel mibPanel = (MibTreePanel) _dataPane.getMibPanel();
		String mibFile = SNMPTreeData.getMIB(oid);
		Matcher matt = SNMPTreeData.MIB_PATTERN.matcher(oid);
		String msg = "OID translation is not supported for " + oid + ". ";
		if(mibFile != null) {
			msg += "\nYou need to obtain the numerical OID from " + mibFile + ". ";
			if(!mibPanel.containsMib(mibFile)) {
				msg += "\nWould you like to load "+ mibFile +"?";
				int result = JOptionPane.showConfirmDialog(null, msg, "OID translation not supported", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION) {
					try {
						mibPanel.loadDefaultMib(mibFile);
						//mibPanel.findMibNode(mibFile, matt.group(2));
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(null, "Can't find " + mibFile + ". Try to locate and load it manually");
					} catch (MibLoaderException e1) {
						JOptionPane.showMessageDialog(null, "Can't load " + mibFile + ". Try to locate and load it manually");
					}
				}
			} 
			return;
		} 
		JOptionPane.showMessageDialog(null, msg);
	}
	
	public abstract void loadDefaultMibs();
	
	public abstract void scanNetwork(String ip, Integer mask, String netType, boolean usePing, int timeout);
	
	public abstract void stopScanning();
	
	public abstract void doneScan(SwingWorker worker);
	
	//public abstract void runSNMP(ArrayList<SNMPTreeData> treeData, SNMPSessionOptionModel model, String filename);
	public abstract void runSNMP(ArrayList<SNMPTreeData> treeData, String filename);
	
	//public abstract boolean stopSNMP();
	public abstract void stopSNMP();
	
	public abstract void doneSNMP(SwingWorker worker);
	
	public abstract void closeWriter();
	
	public void toggleNetScan(boolean isrun) {
		((DevicePanel)_dataPane.getNetworkPanel()).toggleNetScan(isrun);
	}
	
/*
	public void refreshMibTree() {
		((MibPanel) _dataPane.getMibPanel()).refreshMibTree();
	}
*/
	
	public void resetOutputSearch() {
		_outputPane.resetSearch(true);
	}
	
	public void toggleSNMPRun(boolean isrun) {
		_outputPane.toggleSNMPRun(isrun);
		_runSNMPButton.setEnabled(!isrun);
		_cancelSNMPButton.setEnabled(isrun);	
	}
	
	public JPanel getOutputPane() {
		return _outputPane;
	}
	
	public JPanel getMibBrowserPane() {
		return _mibBrowserPane;
	}
	
	public void addAddress(InetAddress address) {
		((DevicePanel)_dataPane.getNetworkPanel()).addAddress(address);
	}

	
	public String getResult() {
		return _outputPane.getResult();
	}
	
	public void appendResult(String result) {
		_outputPane.appendResult(result);
	}

	
/*
	//TODO: filters
	//Form Field
	private class FormField extends JPanel {
		public FormField(String label, JTextField field) {
			super(new GridLayout(1, 2));
			JLabel lab = new JLabel(label, JLabel.LEFT);
			add(lab);
			add(field);
		}		
	}
	
*/
}

