/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under MIT license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public abstract class SNMPSessionFrame extends JFrame implements ClipboardOwner {


	private static final String[] FILTERS = {"Type", "Access", "Status", "Units", "Hint", "ModuleID", "Enums", "Indexes"};
	
	private JTree _tree;
	private DefaultTreeModel _treeModel;
	private TreeCellEditor _cellEditor;
	private TreeNodeCommandStack _commandStack;
	
	//private Map<String, JTextField> _fields;
	private SNMPSessionOptionModel _optionModel;
	private JTextField _logFileField;
	private String _logFile = "";
	private JTextArea _logArea;
	private JLabel _loadingImg;
	
	private SNMPConfigurationStorage _confStorage;
	
	private MouseAdapter _treeListener;
	
	private JButton _runbutt;
	private JButton _cancelbutt;
	
	public SNMPSessionFrame() {
		this("Editable Tree");
	}
	
	public SNMPSessionFrame(String title) {
		super(title);
		setSize(1200, 500);
		//_fields = new Hashtable<String, JTextField>();
		_optionModel = new SNMPSessionOptionModel();
		_logArea = new JTextArea();
		_logArea.setEditable(false);
		try {
			_loadingImg = new JLabel(new ImageIcon(new URL("file:img/loader.gif")));
			_loadingImg.setVisible(false);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		_confStorage = new SNMPConfigurationStorage();
		
		_treeListener = new SNMPTreeMenuListener();
		
		_runbutt = new JButton("Run SNMP");
		_cancelbutt = new JButton("Stop");
		
		_commandStack = new TreeNodeCommandStack();
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	protected void init() {
		
		//tree panel
		JPanel leftPane = new JPanel(new BorderLayout());
		leftPane.setPreferredSize(new Dimension(500, 500));
		
		//Tree to set up data: commands, ips and oids
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Command");
		
		_treeModel = new DefaultTreeModel(root);
		_tree = new JTree(_treeModel);
		_tree.setEditable(true);
		_tree.setCellRenderer(new SNMPRenderer());
		_cellEditor = new SNMPTreeCellEditor(_tree, _commandStack);
		_tree.setCellEditor(_cellEditor);
		_tree.addMouseListener(new MouseAdapter() {
			private void SNMPPopupEvent(MouseEvent event) {
				int x = event.getX();
				int y = event.getY();
	            JTree tree = (JTree)event.getSource(); 			
				TreePath path = tree.getPathForLocation(x, y);
				if (path == null)
					return;	

				int level = path.getPathCount();
				
				SNMPPopupMenu popup = new SNMPPopupMenu();
				popup.buildMenu(level, path);
				popup.show(tree, x, y);
			}
			
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					SNMPPopupEvent(e);
				} 
			}
			
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					SNMPPopupEvent(e);
				} 
			}
			
			public void mouseClicked(MouseEvent e) {
				int row = _tree.getRowForLocation(e.getX(),e.getY());
				if(row == -1) {
					_tree.clearSelection();
				}
			}
		});
			
		final TreeSelectionModel selmodel = _tree.getSelectionModel();
		//allow multiple selection only on the same level
		selmodel.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath[] paths = selmodel.getSelectionPaths();
				if(paths.length == 1) {
					return;
				}
				if(e.isAddedPath()) {
					TreePath newpath = e.getNewLeadSelectionPath();
					int level = newpath.getPathCount();
					List<TreePath> removedPaths = new ArrayList<TreePath>();
					for(TreePath path: paths) {
						if(path.getPathCount() != level) {
							removedPaths.add(path);
						}
					}
					if(!removedPaths.isEmpty())
						selmodel.removeSelectionPaths(removedPaths.toArray(new TreePath[0]));
				}
			}
			
		});
		
		
		JScrollPane treePane = new JScrollPane();
		treePane.getViewport().add(_tree);

		leftPane.add(treePane, BorderLayout.CENTER);
		
		JPanel southPane = new JPanel(new BorderLayout());
		southPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				
		JPanel centerPane = new JPanel(new BorderLayout());
		
		//Community and SNMP version fields
		JPanel opts = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
		opts.add(new JLabel("         "));
		opts.add(new JLabel("SNMP version"));
		ButtonGroup vergroup = new ButtonGroup();
		
		String snmpverdefault = _optionModel.get(SNMPSessionOptionModel.SNMP_VERSION_KEY);
		JRadioButton v1 = new JRadioButton("1", snmpverdefault.equals("1"));
		JRadioButton v2c = new JRadioButton("2c", snmpverdefault.equals("2c"));
		JRadioButton v3 = new JRadioButton("3", snmpverdefault.equals("3"));
		SNMPRadioButtonListener verlist = new SNMPRadioButtonListener(SNMPSessionOptionModel.SNMP_VERSION_KEY);
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
		_cancelbutt.setMnemonic(KeyEvent.VK_S);
		_cancelbutt.setEnabled(false);
		_runbutt.setMnemonic(KeyEvent.VK_R);
		_runbutt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ArrayList<SNMPTreeData> treeData = getTreeData();
				if(treeData.isEmpty()) {
					JOptionPane.showMessageDialog(null, "Commands, IP or OID data are not provided", "Data not provided", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(_logFile != null && _logFile.length() > 0) {
					File log = new File(_logFile);
					if(log.isDirectory()) {
						int result = JOptionPane.showConfirmDialog(null, "The log file is a directory. Would you like to write log into " + _logFile + ".txt?");
						if(result != JOptionPane.OK_OPTION) 
							return;
					}
					if(!log.getName().endsWith(".txt")) {
						_logFile = log.getAbsolutePath() + ".txt";
						log = new File(_logFile);
					}
					if(log.exists()) {
						int result = JOptionPane.showConfirmDialog(null, "The file already exists, do you want to override it?");
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
				
				runSNMP(treeData, getModel(), _logFile);
			}
			
		});
		//set keystrokes for action buttons
		_runbutt.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "Run");
		_runbutt.getActionMap().put("Run", new ButtonAction(_runbutt));
		runp.add(_runbutt);
		
		_cancelbutt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!stopSNMP()) {
					JOptionPane.showMessageDialog(null, "Can't cancel the process", "Warning", JOptionPane.WARNING_MESSAGE);
				}
			}			
		});
		_cancelbutt.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "Cancel");
		_cancelbutt.getActionMap().put("Cancel", new ButtonAction(_cancelbutt));
		runp.add(_cancelbutt);
		
		southPane.add(runp, BorderLayout.SOUTH);
		
		leftPane.add(southPane, BorderLayout.SOUTH);
		
		getContentPane().add(leftPane, BorderLayout.WEST);
		
		JPanel rightPane = new JPanel(new BorderLayout());
		rightPane.setPreferredSize(new Dimension(600, 500));
		
		//Output panel
		JPanel filePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filePane.add(new JLabel("Output to "));
		_logFileField = new JTextField();
		_logFileField.setPreferredSize(new Dimension(300, 20));
		filePane.add(_logFileField);
		JButton choosefile = new JButton("Log directory or file");
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
		fc.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
		choosefile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnval = fc.showOpenDialog(null);
				if(returnval == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					_logFileField.setText(file.getAbsolutePath());
				}		
			}	
		});
		_logFileField.getDocument().addDocumentListener(new DocumentListener() {
			private void setLogFile() {
				_logFile = _logFileField.getText();
			}
			public void changedUpdate(DocumentEvent e) {
				setLogFile();		
			}
			public void insertUpdate(DocumentEvent e) {
				setLogFile();				
			}
			public void removeUpdate(DocumentEvent e) {
				setLogFile();		
			}
			
		});
		filePane.add(choosefile);
		rightPane.add(filePane, BorderLayout.NORTH);
		
		JPanel logPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		logPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Snmp Output")));
		if(_loadingImg != null) {
			logPane.add(_loadingImg);
		}
		JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(660, 370));
		sp.getViewport().add(_logArea);
		logPane.add(sp);
		
		rightPane.add(logPane);
		
		getContentPane().add(rightPane, BorderLayout.CENTER);
		
		//Menu Bar
		this.setJMenuBar(createMenuBar());
		
		
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				if(closeApp()) {				
					super.windowClosing(e);
					System.exit(0);
				}
			}

			public void windowClosed(WindowEvent e) {
				stopSNMP();
			}
		});
		//if not removed, those shortcuts don't work
		//menu
		_tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), "none");
		_tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), "none");
		_tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "none");
	}
	
	private boolean closeApp() {
		int result = showConfirmDialog("Would you like to save the configuration before closing?", "Application closing");
		if(result == 0 || result == 1) {
			String path = _confStorage.getPath();
			if(path == null || result == 1) {
				path = getSaveConfigPath();
			}
			if(path != null) {
				_confStorage.saveConfiguration(_treeModel, _optionModel, path);
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
					butt.getActionMap().put(name, new ButtonAction(butt));
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
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu filemenu = new JMenu("File");
		filemenu.setMnemonic(KeyEvent.VK_F);
		JMenu editmenu = new JMenu("Edit");
		editmenu.setMnemonic(KeyEvent.VK_E);
		//JMenu mibmenu = new JMenu("MIB");
		JMenu optmenu = new JMenu("Options");
		optmenu.setMnemonic(KeyEvent.VK_O);
		
		
		//File menu
		JMenuItem open = new JMenuItem("Open Configuration", KeyEvent.VK_G);
			
		final JMenuItem save = new JMenuItem("Save", KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		save.setEnabled(false);
		
		open.addActionListener(new ActionListener() {
			final JFileChooser fc = new JFileChooser();
			public void actionPerformed(ActionEvent e) {
				fc.setFileFilter(new FileNameExtensionFilter("Text files", "xml"));
				int returnval = fc.showOpenDialog(null);
				if(returnval == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					if(file == null || file.getName().length() == 0)
						return;
					if(file.exists()) {
						TreeNode root = (TreeNode) _treeModel.getRoot();
						Enumeration children = root.children();
						while(children.hasMoreElements()) {
							MutableTreeNode node = (MutableTreeNode) children.nextElement();
							_treeModel.removeNodeFromParent(node);
						}
						if(_confStorage.readConfiguration(_treeModel, _optionModel, file.getAbsolutePath()))
							save.setEnabled(true);	
						for (int i = 0; i < _tree.getRowCount(); i++) {
					         _tree.expandRow(i);
						}
					}
				}
				
			}
			
		});
		
			
		filemenu.add(open);
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_confStorage.saveConfiguration(_treeModel, _optionModel);				
			}
			
		});

		
		filemenu.add(save);
		
		JMenuItem saveas = new JMenuItem("Save As...", KeyEvent.VK_A);
		saveas.setDisplayedMnemonicIndex(5);
		saveas.addActionListener(new ActionListener() {
			final JFileChooser fc = new JFileChooser() {
		        public void approveSelection() {
		             if (getSelectedFile().isFile()) {
		                 return;
		             } else
		                 super.approveSelection();
		        }
		    };
			public void actionPerformed(ActionEvent e) {
				String path = getSaveConfigPath();
				if(_confStorage.saveConfiguration(_treeModel, _optionModel, path))
					save.setEnabled(true);	
				
			}
			
		});

		filemenu.add(saveas);
		filemenu.addSeparator();
		
		JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeApp();
				System.exit(0);			
			}
			
		});

		filemenu.add(exit);
		
		//Edit menu
		SNMPEditListener editListener = new SNMPEditListener();
		JMenuItem undo = new JMenuItem("Undo", KeyEvent.VK_U);
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		JMenuItem redo = new JMenuItem("Redo", KeyEvent.VK_R);
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		JMenuItem cut = new JMenuItem("Cut", KeyEvent.VK_T);
		cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		JMenuItem copy = new JMenuItem("Copy", KeyEvent.VK_C);
		copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		JMenuItem paste = new JMenuItem("Paste", KeyEvent.VK_P);
		paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		JMenuItem insert = new JMenuItem("Insert", KeyEvent.VK_I);
		insert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		JMenuItem remove = new JMenuItem("Delete", KeyEvent.VK_D);
		remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		undo.addActionListener(editListener);
		redo.addActionListener(editListener);
		cut.addActionListener(editListener);
		copy.addActionListener(editListener);
		paste.addActionListener(editListener);
		insert.addActionListener(editListener);
		remove.addActionListener(editListener);	
		editmenu.add(undo);
		editmenu.add(redo);
		editmenu.addSeparator();
		editmenu.add(cut);
		editmenu.add(copy);
		editmenu.add(paste);
		editmenu.add(insert);
		editmenu.addSeparator();
		editmenu.add(remove);
		undo.setEnabled(false);
		redo.setEnabled(false);
		_commandStack.registerButtons(undo, redo);
				
		SNMPSetOptionListener menuListener = new SNMPSetOptionListener();		
		JMenu snmp3submenu = new JMenu("SNMP3 options");
		snmp3submenu.setMnemonic(KeyEvent.VK_3);
		JMenuItem port = new JMenuItem("Port Number", KeyEvent.VK_P);
		JMenuItem timeout = new JMenuItem("Timeout", KeyEvent.VK_T);
		JMenuItem retries = new JMenuItem("Retries", KeyEvent.VK_R);
		port.addActionListener(menuListener);
		timeout.addActionListener(menuListener);
		retries.addActionListener(menuListener);
		_optionModel.setTitle(port.getText(), SNMPSessionOptionModel.PORT_KEY);
		_optionModel.setTitle(timeout.getText(), SNMPSessionOptionModel.TIMEOUT_KEY);
		_optionModel.setTitle(retries.getText(), SNMPSessionOptionModel.RETRIES_KEY);
		
		JMenuItem secname = new JMenuItem("Security Name", KeyEvent.VK_N);
		JMenu seclevsubmenu = new JMenu("Security Level");
		seclevsubmenu.setMnemonic(KeyEvent.VK_L);
		JMenuItem auth = new JMenuItem("Authentication Passphrase", KeyEvent.VK_A);
		JMenu authtypesubmenu = new JMenu("Authentication Type");
		authtypesubmenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem privpassphrase = new JMenuItem("Privacy Passphrase", KeyEvent.VK_I);
		JMenu privtypesubmenu = new JMenu("Privacy Type");	
		privtypesubmenu.setMnemonic(KeyEvent.VK_V);
		secname.addActionListener(menuListener);
		auth.addActionListener(menuListener);
		authtypesubmenu.addActionListener(menuListener);
		privpassphrase.addActionListener(menuListener);	
		snmp3submenu.add(secname);
		snmp3submenu.add(seclevsubmenu);
		snmp3submenu.add(auth);
		snmp3submenu.add(authtypesubmenu);
		snmp3submenu.add(privpassphrase);
		snmp3submenu.add(privtypesubmenu);
		_optionModel.setTitle(secname.getText(), SNMPSessionOptionModel.SECURITY_NAME_KEY);
		_optionModel.setTitle(auth.getText(), SNMPSessionOptionModel.AUTH_PASSPHRASE_KEY);
		_optionModel.setTitle(privpassphrase.getText(), SNMPSessionOptionModel.PRIV_PASSPHRASE_KEY);
		
		ActionListener seclevlis = new SNMPRadioButtonListener(SNMPSessionOptionModel.SECURITY_LEVEL_KEY);		
		ButtonGroup seclevgroup = new ButtonGroup();
		String secdefault = _optionModel.get(SNMPSessionOptionModel.SECURITY_LEVEL_KEY);
		JRadioButtonMenuItem noAuthNoPriv = new JRadioButtonMenuItem("noAuthNoPriv", secdefault.equalsIgnoreCase("noAuthNoPriv"));
		JRadioButtonMenuItem authNoPriv = new JRadioButtonMenuItem("authNoPriv", secdefault.equalsIgnoreCase("authNoPriv"));
		JRadioButtonMenuItem authPriv = new JRadioButtonMenuItem("authPriv", secdefault.equalsIgnoreCase("authPriv"));
		noAuthNoPriv.addActionListener(seclevlis);
		authNoPriv.addActionListener(seclevlis);
		authPriv.addActionListener(seclevlis);
		seclevgroup.add(noAuthNoPriv);
		seclevgroup.add(authNoPriv);
		seclevgroup.add(authPriv);
		seclevsubmenu.add(noAuthNoPriv);
		seclevsubmenu.add(authNoPriv);
		seclevsubmenu.add(authPriv);
		
		ActionListener authtypelis = new SNMPRadioButtonListener(SNMPSessionOptionModel.AUTH_TYPE_KEY);
		ButtonGroup authtypegroup = new ButtonGroup();
		String authtypedefault = _optionModel.get(SNMPSessionOptionModel.AUTH_TYPE_KEY);
		JRadioButtonMenuItem md5 = new JRadioButtonMenuItem("MD5", authtypedefault.equalsIgnoreCase("MD5"));
		JRadioButtonMenuItem sha = new JRadioButtonMenuItem("SHA", authtypedefault.equalsIgnoreCase("SHA"));
		md5.addActionListener(authtypelis);
		sha.addActionListener(authtypelis);
		authtypegroup.add(md5);
		authtypegroup.add(sha);
		authtypesubmenu.add(md5);
		authtypesubmenu.add(sha);
		
		ActionListener privtypelis = new SNMPRadioButtonListener(SNMPSessionOptionModel.PRIV_TYPE_KEY);
		ButtonGroup privtypegroup = new ButtonGroup();
		String privtypedefault = _optionModel.get(SNMPSessionOptionModel.PRIV_TYPE_KEY);
		JRadioButtonMenuItem des = new JRadioButtonMenuItem("DES", privtypedefault.equalsIgnoreCase("DES"));
		JRadioButtonMenuItem des3 = new JRadioButtonMenuItem("3DES", privtypedefault.equalsIgnoreCase("3DES"));
		JRadioButtonMenuItem aes128 = new JRadioButtonMenuItem("AES128", privtypedefault.equalsIgnoreCase("AES128"));
		JRadioButtonMenuItem aes192 = new JRadioButtonMenuItem("AES192", privtypedefault.equalsIgnoreCase("AES192"));
		JRadioButtonMenuItem aes256 = new JRadioButtonMenuItem("AES256", privtypedefault.equalsIgnoreCase("AES256"));
		des.addActionListener(privtypelis);
		des3.addActionListener(privtypelis);
		aes128.addActionListener(privtypelis);
		aes192.addActionListener(privtypelis);
		aes256.addActionListener(privtypelis);
		privtypegroup.add(des);
		privtypegroup.add(des3);
		privtypegroup.add(aes128);
		privtypegroup.add(aes192);
		privtypegroup.add(aes256);
		privtypesubmenu.add(des);
		privtypesubmenu.add(des3);
		privtypesubmenu.add(aes128);
		privtypesubmenu.add(aes192);
		privtypesubmenu.add(aes256);
		
		optmenu.add(snmp3submenu);
		optmenu.add(port);
		optmenu.add(timeout);
		optmenu.add(retries);
		
		menuBar.add(filemenu);
		menuBar.add(editmenu);
		//menuBar.add(mibmenu);
		menuBar.add(optmenu);
		return menuBar;
	}
	
	private String getSaveConfigPath() {
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
	
	private void addNodes(String str) {
		TreePath[] paths = _tree.getSelectionPaths();
		TreeNodeCommandStack.Command add = new AddCommand(paths, str);
		_commandStack.add(add);
	}
	
	private void addNode(DefaultMutableTreeNode parent, String str) {
		addNode(parent, str, null);
	}
		
	private void addNode(DefaultMutableTreeNode parent, String str, DefaultMutableTreeNode node) {
		TreeNode[] path = parent.getPath();
		boolean isEditing = (str == null || str.length() == 0);
		DefaultMutableTreeNode child = (node == null) ? new DefaultMutableTreeNode(str) : node;
		_treeModel.insertNodeInto(child, parent, parent.getChildCount());
		TreePath treepath = new TreePath(path);
		_tree.expandPath(treepath);
		if(isEditing) {
			_tree.startEditingAtPath(treepath.pathByAddingChild(child));
			((SNMPTreeCellEditor) _cellEditor).setCommandData(child);
		} 
	}
	
	private void editNode(String str) {
		TreePath path = _tree.getSelectionPath();
		boolean isEditing = (str == null || str.length() == 0);
		if(isEditing) {
			_tree.startEditingAtPath(path);
			((SNMPTreeCellEditor) _cellEditor).setCommandData((TreeNode) path.getLastPathComponent());
		} else {
			TreeNodeCommandStack.Command paste = new PasteCommand(path, str);
			_commandStack.add(paste);
		}
	}

	private void copyData() {
		Set<TreePath> newpaths = new HashSet<TreePath>();
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0) {
			Collections.addAll(newpaths, paths);
			copyData(newpaths);
		}
		
	}
	
	private void copyData(Set<TreePath> paths) {
		StringBuilder str = new StringBuilder();
		for(TreePath path: paths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			copyNodeData(node, str);
		}
		if(str.length() > 0)
			setClipboardContents(str.toString());
	}
	
	private void copyData(List<TreeNode> nodes) {
		StringBuilder str = new StringBuilder();
		for(TreeNode node: nodes) {
			copyNodeData(node, str);
		}
		if(str.length() > 0)
			setClipboardContents(str.toString());
	}
	
	private void copyNodeData(TreeNode node, StringBuilder str) {
		if(str.length() > 0)
			str.append("\n");
		str.append((String)((DefaultMutableTreeNode) node).getUserObject());
	}
	
	
	private void pasteData() {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0) {
			TreeNodeCommandStack.Command paste = new PasteCommand(paths);
			_commandStack.add(paste);
		}
	}
	
	
	private void insertData() {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0) {
			TreeNodeCommandStack.Command insert = new InsertCommand(paths, getClipboardContents().split("\\r?\\n"));
			_commandStack.add(insert);
		}
	}
	
	
	private void cutNodes() {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0  && paths[0].getPathCount() > 1) {
			int result = JOptionPane.showConfirmDialog(null, "Do you want to cut the node(s)?");
			if(result == JOptionPane.YES_OPTION) {
				TreeNodeCommandStack.Command cut = new CutCommand(paths, _treeModel);
				_commandStack.add(cut);
			}
		}
	}
	
	
	private void removeNodes() {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0 && paths[0].getPathCount() > 1) {
			int result = JOptionPane.showConfirmDialog(null, "Do you want to delete the node(s)?");
			if(result == JOptionPane.YES_OPTION) {
				TreeNodeCommandStack.Command remove = new RemoveCommand(paths);
				_commandStack.add(remove);
			}
		}
	}
	
	
	private void undo() {
		_commandStack.undo();
	}
	
	private void redo() {
		_commandStack.redo();
	}
	
	@Override
	public void lostOwnership(Clipboard clip, Transferable str) {
		//do nothing
	} 
	
	private void setClipboardContents( String str ){
		StringSelection stringSelection = new StringSelection( str );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}
	
	private String getClipboardContents() {
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		    if ( hasTransferableText ) {
		      try {
		        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
		      }
		      catch (UnsupportedFlavorException ex){
		        //highly unlikely since we are using a standard DataFlavor
		        System.out.println(ex);
		        ex.printStackTrace();
		      }
		      catch (IOException ex) {
		        System.out.println(ex);
		        ex.printStackTrace();
		      }
		 }
		 return result;
	}
	
	protected SNMPSessionOptionModel getModel() {
		return _optionModel;
	}
	
	protected  ArrayList<SNMPTreeData> getTreeData() {
		ArrayList<SNMPTreeData> treeData = new ArrayList<SNMPTreeData>();
		Object root = _treeModel.getRoot();
		walk(treeData, getChildren(root));
		return treeData;
	}
	
	
	private void walk(ArrayList<SNMPTreeData> data, Enumeration<Object> children) {
		while(children.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			if(_treeModel.isLeaf(node)) {
				TreeNode[] path = node.getPath();
				//0 - root, 1 - command, 2 - ip, 3 - oid
				if(path.length == 4) {
					SNMPTreeData row = new SNMPTreeData(path);
					if(row.isValid())
						data.add(row);
				}
			} else {		
				Enumeration ch = getChildren(node);
				walk(data, ch);
			}
		}		
	}
	
	private Enumeration getChildren(Object o) {
		return ((DefaultMutableTreeNode) o).children(); 	
	}
	
	
	public abstract void runSNMP(ArrayList<SNMPTreeData> treeData, SNMPSessionOptionModel model, String filename);
	
	public abstract boolean stopSNMP();
	
	public abstract void done(SwingWorker worker);
	
	public void toggleRun(boolean isrun) {
		_loadingImg.setVisible(isrun);
		_runbutt.setEnabled(!isrun);
		_cancelbutt.setEnabled(isrun);	
		_logFileField.setEditable(!isrun);
		_logFileField.setEnabled(!isrun);
	}
	
	public String getLogFile() {
		return _logFile;
	}

	
	public void clearResult() {
		setResult("");
	}
	
	public void setResult(String result) {
		_logArea.setText(result);
	}
	
	public String getResult() {
		return _logArea.getText();
	}
	
	public void appendResult(String result) {
		_logArea.append(result);
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
	
	//Command classes
	
	private class AddCommand implements TreeNodeCommandStack.Command {
		protected String[] _userData = null;
		protected Map<TreePath, List<TreeNode>> _pathMap;
		
		
		public AddCommand(TreePath path) {
			this(path, "");
		}
		
		public AddCommand(TreePath path, String userData) {
			this(new TreePath[] {path}, new String[] {userData});
		}
		
		public AddCommand(TreePath[] paths) {
			this(paths, new String[] {""});
		}
		
		public AddCommand(TreePath[] paths, String userData) {
			this(paths, new String[] {userData});
		}

		public AddCommand(TreePath[] paths, String[] userData) {
			_userData = userData;
			_pathMap = new Hashtable<TreePath, List<TreeNode>>();
			for(TreePath path: paths) {
				List<TreeNode> nodes = new ArrayList<TreeNode>();
				for(String str: userData) {
					DefaultMutableTreeNode node = new DefaultMutableTreeNode(str);
					nodes.add(node);
				}
				_pathMap.put(path, nodes);
			}
		}
		
		public void execute() {
			Set<TreePath> paths = getPaths();
			for(TreePath path: paths) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();
				if(_userData != null) {
					List<TreeNode> nodes = getChildren(path);
					int index = 0;
					int lastIndex = _userData.length-1;
					for(TreeNode node: nodes) {
						if(index > lastIndex)
							break;
						addNode(parent, _userData[index], (DefaultMutableTreeNode) node);
						index++;
					}
				}
			}
		}
	
		public void undo() {
			Set<TreePath> paths = getPaths();
			for(TreePath path: paths) {
				List<TreeNode> nodes = getChildren(path);
				for(TreeNode node: nodes) {
					_treeModel.removeNodeFromParent((MutableTreeNode) node);
				}
			}	
		}
		
		
		protected Set<TreePath> getPaths() {
			return _pathMap.keySet();
		}
		
		protected List<TreeNode> getChildren(TreePath path) {
			return _pathMap.get(path);
		}	
		
	}
	
	
	private class InsertCommand extends AddCommand {
		
		public InsertCommand(TreePath path, String str) {
			this(new TreePath[] {path}, new String[] {str});
		}

		public InsertCommand(TreePath[] paths, String[] str) {
			super(paths, str);
		}
		
	}

	
	private class RemoveCommand implements TreeNodeCommandStack.Command {
		
		protected Map<TreePath, Map<Integer, TreeNode>> _pathMap;
					
		public RemoveCommand(TreePath[] paths) {
			//super(paths);
			_pathMap = new Hashtable<TreePath, Map<Integer, TreeNode>>();
			if(paths != null) {
				for(TreePath path: paths) {
					TreePath parentPath = path.getParentPath();
					MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
					MutableTreeNode parent = (MutableTreeNode) parentPath.getLastPathComponent();
					int index = _treeModel.getIndexOfChild(parent, node);
					Map<Integer, TreeNode> nodes = _pathMap.get(parentPath);
					if(nodes == null) {
						nodes = new TreeMap<Integer, TreeNode>();
						_pathMap.put(parentPath, nodes);
					}
					nodes.put(index, node);
				}
			} 
		}
			
		public void execute() {
			List<TreeNode> nodes = getAllNodes();
			for(TreeNode node: nodes) {
				if(!((DefaultMutableTreeNode) node).isRoot())
					_treeModel.removeNodeFromParent((MutableTreeNode) node);
			}
		}
		
		public void undo() {
			Set<TreePath> paths = getPaths();
			for(TreePath path: paths) {
				MutableTreeNode parent = (MutableTreeNode) path.getLastPathComponent();
				Map<Integer, TreeNode> children = getChildren(path);
				//add in the ascending order
				for(int index: children.keySet()) {
					MutableTreeNode node = (MutableTreeNode) children.get(index);
					_treeModel.insertNodeInto(node, parent, index);
				}
				//expand all
				Enumeration<TreeNode> nodes = ((DefaultMutableTreeNode) parent).depthFirstEnumeration();
				while(nodes.hasMoreElements()) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
					_tree.expandPath(new TreePath(node.getPath()));
				}
			}
		}
		
		protected Set<TreePath> getPaths() {
			return _pathMap.keySet();
		}
		
		protected Map<Integer, TreeNode> getChildren(TreePath path) {
			return _pathMap.get(path);
		}
			
		protected Collection<TreeNode> getChildNodes(TreePath path) {
			return _pathMap.get(path).values();
		}
		
		protected List<TreeNode> getAllNodes() {
			List<TreeNode> nodes = new ArrayList<TreeNode>();
			for(TreePath path: _pathMap.keySet()) {
				Collection<TreeNode> childNodes = getChildNodes(path);
				nodes.addAll(childNodes);
			}
			return nodes;
		}
		
	}
	
	private class CutCommand extends RemoveCommand {
		StringBuilder _savedClipboardData;
				
		public CutCommand(TreePath[] paths, DefaultTreeModel _treeModel) {
			super(paths);
			_savedClipboardData = new StringBuilder();
			_savedClipboardData.append(getClipboardContents());
		}
				
		public void execute() {
			super.execute();
			copyData(getAllNodes());
		}	
		
		public void undo() {
			super.undo();
			if(_savedClipboardData.length() > 0)
				setClipboardContents(_savedClipboardData.toString());
		}
				
	}
	
	private class PasteCommand implements TreeNodeCommandStack.Command {
		public Map<TreePath, Object> _pathMap;
		public List<DefaultMutableTreeNode> _addedNodes;
		private String[] _copyData;
		
		public PasteCommand(TreePath path) {
			this(path, null);
		}
		
		public PasteCommand(TreePath[] paths) {
			this(paths, null);
		}
		
		public PasteCommand(TreePath path, String copyData) {
			this(new TreePath[] {path}, new String[] {copyData});
		}
			
		public PasteCommand(TreePath[] paths, String[] copyData) {		
			if(copyData == null) {
				 String str = getClipboardContents();
				 if(str != null)
					 _copyData = str.split("\\r?\\n");
			} else {
				_copyData = copyData;
			}

			_pathMap = new Hashtable<TreePath, Object>();
			_addedNodes = new ArrayList<DefaultMutableTreeNode>();
			if(paths != null) {
				for(TreePath path: paths) {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					_pathMap.put(path, node.getUserObject());
				}
				int addNum = paths.length;
				while(addNum < _copyData.length) {
					_addedNodes.add(new DefaultMutableTreeNode(_copyData[addNum]));
					addNum++;
				}
			}			
		}
		
		public void execute() {
			if(_copyData != null && _copyData.length > 0) {	
				Set<TreePath> paths = getPaths();
				int index = 0;
				int lastline = _copyData.length - 1;
				DefaultMutableTreeNode parent = null;
				
				for(TreePath path: paths) {
					if(index > lastline)
						break;
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					parent = (DefaultMutableTreeNode) node.getParent();
					node.setUserObject(_copyData[index]);
					_treeModel.nodeChanged(node);
					index++;
				}
				if(parent != null) {
					for(DefaultMutableTreeNode node: _addedNodes) {
						if(index > lastline)
							break;
						addNode(parent, _copyData[index], node);
					}
				}
			} 
		}

		public void undo() {
			Set<TreePath> paths = getPaths();
			for(TreePath path: _pathMap.keySet()) {
				Object obj = _pathMap.get(path);
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				node.setUserObject(obj);
				_treeModel.nodeChanged(node);
			}
			for(DefaultMutableTreeNode node: _addedNodes) {
				_treeModel.removeNodeFromParent(node);
			}	
		}
		
		protected Set<TreePath> getPaths() {
			return _pathMap.keySet();
		}
		
		protected Object getPathData(TreePath path) {
			return _pathMap.get(path);
		}
			
	}
	
	//Tree popup menu
	private class SNMPPopupMenu extends JPopupMenu {
		TreePath _path;
		
		public JMenu buildCommandMenu(String title, MouseListener lis) {
			JMenu comms = new JMenu(title);
			for(String str: SNMPTreeData.COMMANDS) {
				JMenuItem comm = new JMenuItem(str);
				comm.addMouseListener(lis);
				comms.add(comm);
			}
			return comms;
		}
		
		private boolean noChildren() {
			String val = (String) ((DefaultMutableTreeNode) _path.getLastPathComponent()).getUserObject();
			return (val == null || val.length() == 0);
		}
		
		public void buildMenu(int type, TreePath p) {
			_path = p;
			//Add
			switch(type) {
				case SNMPTreeCellEditor.ROOT: 
							this.add(buildCommandMenu("Add Command", _treeListener));
							break;
				case SNMPTreeCellEditor.COMMAND_NODE:
							JMenuItem addip = new JMenuItem("Add IP");
							addip.addMouseListener(_treeListener);
							this.add(addip);
							break;
				case SNMPTreeCellEditor.IP_NODE: 
							if(noChildren())
								break;
							JMenuItem addoid = new JMenuItem("Add OID");
							addoid.addMouseListener(_treeListener);
							this.add(addoid);
							break;
				default: break;			
			}
			if(type > SNMPTreeCellEditor.ROOT) {
				//Edit
				if(_tree.getSelectionCount() == 1) {
					if(type == SNMPTreeCellEditor.COMMAND_NODE) {
						this.add(buildCommandMenu("Edit", _treeListener));
					} else {
						JMenuItem edit = new JMenuItem("Edit");
						edit.addMouseListener(_treeListener);
						this.add(edit);
					}
				}
				//Cut
				JMenuItem cut = new JMenuItem("Cut");
				cut.addMouseListener(_treeListener);
				this.add(cut);
				//Copy
				JMenuItem copy = new JMenuItem("Copy");
				copy.addMouseListener(_treeListener);
				this.add(copy);
							
				//Paste
				JMenuItem paste = new JMenuItem("Paste");
				paste.addMouseListener(_treeListener);
				this.add(paste);			
			}
			//Insert
			switch(type) {
				case SNMPTreeCellEditor.ROOT: 
							JMenuItem insertcomm = new JMenuItem("Insert Commands");
							insertcomm.addMouseListener(_treeListener);
							this.add(insertcomm);
							break;
				case SNMPTreeCellEditor.COMMAND_NODE:
							JMenuItem insertip = new JMenuItem("Insert IPs");
							insertip.addMouseListener(_treeListener);
							this.add(insertip);
							break;
				case SNMPTreeCellEditor.IP_NODE: 
							if(noChildren())
								break;
							JMenuItem insertoid = new JMenuItem("Insert OIDs");
							insertoid.addMouseListener(_treeListener);
							this.add(insertoid);
							break;
				default: break;			
			}
			if(type > SNMPTreeCellEditor.ROOT) {
				//Remove
				this.addSeparator();
				JMenuItem remove = new JMenuItem("Delete");
				remove.addMouseListener(_treeListener);
				this.add(remove);
			}
			
		}
		
		
		public TreePath getObjectPath() {
			return _path;
		}
	}

	//Renderer
	private class SNMPRenderer extends DefaultTreeCellRenderer {
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row, boolean focus) {
				if(row > SNMPTreeCellEditor.ROOT && (value == null || value.toString().length() == 0)) {
		    		TreePath path = tree.getPathForRow(row);
		    		if(path != null) {
			    		switch(path.getPathCount()) {
			    			//case COMMAND_NODE: value = "Add Command..."; break;
			    			case SNMPTreeCellEditor.IP_NODE: value = "Add IP..."; break;
			    			case SNMPTreeCellEditor.OID_NODE: value = "Add OID..."; break;
			    			default: break;
			    		}
		    		}
				}
        return super.getTreeCellRendererComponent(tree, value, isSelected, expanded,
                leaf, row, focus);
		}
	}
	
	
	//Listeners
	private class SNMPSetOptionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object obj = event.getSource();
			if(obj instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) obj;
				String title = item.getText();
				String key = _optionModel.getTitle(title);
				String s = JOptionPane.showInputDialog(null, title, _optionModel.get(key));
				if(s != null && s.length() > 0) {
					_optionModel.put(key, s);
				} else {
					JOptionPane.showMessageDialog(null, "Value wasn't changed", "Value not provided", JOptionPane.WARNING_MESSAGE);
				}
			}
		}
		
	}
	
	private class SNMPEditListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Object obj = event.getSource();
			if(obj instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) obj;
				String title = item.getText();
				if(title.startsWith("Undo")) {
					undo();
				} else if(title.startsWith("Redo")) {
					redo();
				} else if(title.startsWith("Delete")) {
					removeNodes();
				} else if(title.startsWith("Cut")) {
					cutNodes();
				} else if(title.startsWith("Copy")) {
					copyData();
				} else if(title.startsWith("Paste")) {
					pasteData();
				} else if(title.startsWith("Insert")) {
					insertData();
				}
			}
			
		}
		
	}
	
	private class SNMPRadioButtonListener implements ActionListener {
		String _key;
		
		public SNMPRadioButtonListener(String k) {
			_key = k;
		}
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			String str = "";
			if(obj instanceof JRadioButton) {
				str = ((JRadioButton) obj).getText();
			} else if(obj instanceof JRadioButtonMenuItem) {
				str = ((JRadioButtonMenuItem) obj).getText();
			}
			_optionModel.put(_key, str);	
		}
		
	}
	
	
	private class SNMPTreeMenuListener extends MouseAdapter {
		private void executeCommand(String command, SNMPPopupMenu menu, String str) {
			if(command.startsWith("Add")) {
				addNodes(str);
			} else if(command.startsWith("Edit")) {
				editNode(str);
			} else if(command.startsWith("Delete")) {
				removeNodes();
			} else if(command.startsWith("Cut")) {
				cutNodes();
			} else if(command.startsWith("Copy")) {
				copyData();
			} else if(command.startsWith("Paste")) {
				pasteData();
			} else if(command.startsWith("Insert")) {
				insertData();
			}
		}
			
		public void mousePressed(MouseEvent event) {
			JMenuItem item = (JMenuItem)event.getSource();
			JPopupMenu parent = (JPopupMenu) item.getParent();
			if(parent instanceof SNMPPopupMenu) {
				executeCommand(item.getText(), (SNMPPopupMenu) parent, null);
			} else {
				String str = item.getText();
				JMenu invoker = (JMenu)parent.getInvoker();
				JPopupMenu parent2 = (JPopupMenu) invoker.getParent();
				if(parent2 instanceof SNMPPopupMenu) {
					executeCommand(invoker.getText(), (SNMPPopupMenu) parent2, str);
				}
			}
		}
	}
	
	private static class ButtonAction extends AbstractAction {
		private AbstractButton _butt;
		public ButtonAction(AbstractButton butt) {
			_butt = butt;
		}
		public void actionPerformed(ActionEvent e) {
			_butt.doClick();			
		}
		
	}
		
}

