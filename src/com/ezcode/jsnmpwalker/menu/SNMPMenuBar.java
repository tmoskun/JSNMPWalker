package com.ezcode.jsnmpwalker.menu;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.command.TreeNodeCommandStack;
import com.ezcode.jsnmpwalker.data.SNMPSessionOptionModel;
import com.ezcode.jsnmpwalker.listener.SNMPEditListener;
import com.ezcode.jsnmpwalker.listener.SNMPRadioButtonListener;
import com.ezcode.jsnmpwalker.panel.MibTreePanel;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;
import com.ezcode.jsnmpwalker.storage.SNMPConfigurationStorage;

public class SNMPMenuBar extends JMenuBar {
	private SNMPSessionFrame _frame;
	private SNMPTreePanel _treePane;
	private JTree _tree;
	private DefaultTreeModel _treeModel;
	private SNMPConfigurationStorage _confStorage;
	private SNMPSessionOptionModel _optionModel;
	private JMenu _mibMenu;
	private TreeNodeCommandStack _commandStack;
	
	public SNMPMenuBar(SNMPSessionFrame frame, JMenu mibMenu, TreeNodeCommandStack commandStack) {
		super();
		_frame = frame;
		_treePane = frame.getTreePane();
		_tree = _treePane.getTree();
		_treeModel = (DefaultTreeModel) _tree.getModel();
		_confStorage = frame.getConfStorage();
		_optionModel = frame.getOptionModel();
		_mibMenu = mibMenu;
		_commandStack = commandStack;
		init();
	}
	
	private void init() {
		JMenu filemenu = new JMenu("File");
		filemenu.setMnemonic(KeyEvent.VK_F);
		JMenu editmenu = new JMenu("Edit");
		editmenu.setMnemonic(KeyEvent.VK_E);
		JMenu optmenu = new JMenu("Options");
		optmenu.setMnemonic(KeyEvent.VK_O);
//		_mibMenu = new JMenu("MIB");
//		_mibMenu.setMnemonic(KeyEvent.VK_M);
			
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
			public void actionPerformed(ActionEvent e) {
				String path = _frame.getSaveConfigPath();
				if(_confStorage.saveConfiguration(_treeModel, _optionModel, path))
					save.setEnabled(true);	
				
			}
			
		});

		filemenu.add(saveas);
		filemenu.addSeparator();
		
		JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_X);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(_frame.promptCloseApp())
					_frame.closeScanning();
					System.exit(0);			
			}
			
		});

		filemenu.add(exit);
		
		//Edit menu
		SNMPEditListener editListener = new SNMPEditListener(_treePane);
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
		
		ActionListener seclevlis = new SNMPRadioButtonListener(_optionModel, SNMPSessionOptionModel.SECURITY_LEVEL_KEY);		
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
		
		ActionListener authtypelis = new SNMPRadioButtonListener(_optionModel, SNMPSessionOptionModel.AUTH_TYPE_KEY);
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
		
		ActionListener privtypelis = new SNMPRadioButtonListener(_optionModel, SNMPSessionOptionModel.PRIV_TYPE_KEY);
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
		
		add(filemenu);
		add(editmenu);
		add(optmenu);
		
		add(Box.createHorizontalStrut(SNMPSessionFrame.WIDTH/7));

		//_mibMenu = ((MibPanel) _dataPane.getMibPanel()).getMibMenu();
		add(_mibMenu);
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

}
