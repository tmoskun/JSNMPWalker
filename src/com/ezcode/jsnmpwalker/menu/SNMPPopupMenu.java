package com.ezcode.jsnmpwalker.menu;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.MouseListener;
import java.util.regex.Matcher;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.SNMPTreeCellEditor;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.listener.SNMPTreeMenuListener;
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
			if(_tree.getSelectionCount() == 1) {
				//Translate from MIB
				if(type == SNMPTreeCellEditor.OID_NODE) {
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
