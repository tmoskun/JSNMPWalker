package com.ezcode.jsnmpwalker.menu;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.panel.SNMPTreePanel;

public class SNMPPopupMenu extends JPopupMenu {
	private TreePath _path;
	private JTree _tree;
	private SNMPTreeMenuListener _treeListener;
	
	public SNMPPopupMenu(SNMPTreePanel panel) {
		_tree = panel.getTree();
		_treeListener = new SNMPTreeMenuListener(panel);
	}
	
	public JMenu buildCommandMenu(String title, MouseListener lis) {
		JMenu comms = new JMenu(title);
		ArrayList<String> methods = new ArrayList<String>(Arrays.asList(SNMPTreeData.METHODS));
		TreeNode root = (TreeNode) _tree.getModel().getRoot();
		Enumeration children = root.children();
		while(children.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			methods.remove(node.getUserObject());
		}
		if(methods.isEmpty())
			return null;
		for(String str: methods) {
			JMenuItem comm = new JMenuItem(str);
			comm.addMouseListener(lis);
			comms.add(comm);
		}
		return comms;
	}
	
	private boolean noChildren() {
		Object val = ((DefaultMutableTreeNode) _path.getLastPathComponent()).getUserObject();
		return (val == null || val.toString().length() == 0);
	}
	
	public void buildMenu(int type, TreePath p) {
		_path = p;
		//Add
		switch(type) {
			case SNMPTreePanel.ROOT: 
						JMenu commandMenu = buildCommandMenu("Add Method", _treeListener);
						if(commandMenu != null) {
							this.add(commandMenu);
						}
						break;
			case SNMPTreePanel.COMMAND_NODE:
						JMenuItem addip = new JMenuItem("Add IP");
						addip.addMouseListener(_treeListener);
						this.add(addip);
						break;
			case SNMPTreePanel.IP_NODE: 
						if(noChildren())
							break;
						JMenuItem addoid = new JMenuItem("Add OID");
						addoid.addMouseListener(_treeListener);
						this.add(addoid);
						break;
			default: break;			
		}
		if(type > SNMPTreePanel.ROOT) {			
			if(_tree.getSelectionCount() == 1) {
				//Translate from MIB
				if(type == SNMPTreePanel.OID_NODE) {
					String oid = (String) ((DefaultMutableTreeNode) _path.getLastPathComponent()).getUserObject();
					if(!SNMPTreeData.isValidOID(oid)) {
						String mibFile = SNMPTreeData.getMIB(oid);
						JMenuItem trans;
						if(mibFile == null) {
							trans = new JMenuItem("Translate from MIB");
						} else {
							trans = new JMenuItem("Translate from " + mibFile);
						}
						trans.addMouseListener(_treeListener);
						this.add(trans);
					}
				}
				//Edit
				if(type == SNMPTreePanel.COMMAND_NODE) {
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
			case SNMPTreePanel.ROOT: 
						JMenuItem insertcomm = new JMenuItem("Insert Commands");
						insertcomm.addMouseListener(_treeListener);
						this.add(insertcomm);
						break;
			case SNMPTreePanel.COMMAND_NODE:
						JMenuItem insertip = new JMenuItem("Insert IPs");
						insertip.addMouseListener(_treeListener);
						this.add(insertip);
						break;
			case SNMPTreePanel.IP_NODE: 
						if(noChildren())
							break;
						JMenuItem insertoid = new JMenuItem("Insert OIDs");
						insertoid.addMouseListener(_treeListener);
						this.add(insertoid);
						break;
			default: break;			
		}
		if(type > SNMPTreePanel.ROOT) {
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
	
	private class SNMPTreeMenuListener extends MouseAdapter {
		private SNMPTreePanel _panel;
		
		public SNMPTreeMenuListener(SNMPTreePanel panel) {
			_panel = panel;
		}
		
		private void executeCommand(String command, SNMPPopupMenu menu, Object obj) {
			if(command.startsWith("Add")) {
				_panel.addNodes(obj);
			} else if(command.startsWith("Edit")) {
				_panel.editNode(obj);
			} else if(command.startsWith("Delete")) {
				_panel.removeNodes();
			} else if(command.startsWith("Cut")) {
				_panel.cutNodes();
			} else if(command.startsWith("Copy")) {
				_panel.copyData();
			} else if(command.startsWith("Paste")) {
				_panel.pasteData();
			} else if(command.startsWith("Insert")) {
				_panel.insertData();
			} else if(command.startsWith("Translate")) {
				_panel.translateData();
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
}
