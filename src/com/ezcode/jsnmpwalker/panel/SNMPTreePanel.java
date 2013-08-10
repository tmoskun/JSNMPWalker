package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.SNMPTreeCellEditor;
import com.ezcode.jsnmpwalker.command.AddCommand;
import com.ezcode.jsnmpwalker.command.CutCommand;
import com.ezcode.jsnmpwalker.command.InsertCommand;
import com.ezcode.jsnmpwalker.command.PasteCommand;
import com.ezcode.jsnmpwalker.command.RemoveCommand;
import com.ezcode.jsnmpwalker.command.TreeNodeCommandStack;
import com.ezcode.jsnmpwalker.menu.SNMPPopupMenu;
import com.ezcode.jsnmpwalker.target.TreeDropTarget;

public class SNMPTreePanel extends JScrollPane  implements ClipboardOwner {
	private JTree _tree;
	private DefaultTreeModel _treeModel;
	private TreeCellEditor _cellEditor;
	private TreeNodeCommandStack _commandStack;
	
	public SNMPTreePanel(TreeNodeCommandStack commandStack) {
		_commandStack = commandStack;
		init();		
	}
	
	private void init() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Command");
		_treeModel = new DefaultTreeModel(root);
		_tree = new JTree(_treeModel);
		_tree.setEditable(true);
		//ToolTipManager.sharedInstance().registerComponent(_tree);
		_tree.setCellRenderer(new SNMPTreeRenderer());
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
				
				SNMPPopupMenu popup = new SNMPPopupMenu(SNMPTreePanel.this);
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
		_tree.setDropTarget(new TreeDropTarget(this));

			
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
		
		//if not removed, those shortcuts don't work
		//menu
		_tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK), "none");
		_tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK), "none");
		_tree.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK), "none");
		
		this.getViewport().add(_tree);
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "SNMP Walk"));
	}
	
	public JTree getTree() {
		return _tree;
	}
	
	public void addNodes(String str) {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0) {
			TreeNodeCommandStack.Command add = new AddCommand(this, paths, str);
			_commandStack.add(add);
		}
	}
	
	public void addNode(DefaultMutableTreeNode parent, String str) {
		addNode(parent, str, null);
	}
		
	public void addNode(DefaultMutableTreeNode parent, String str, DefaultMutableTreeNode node) {
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
	
	public void editNode(String str) {
		TreePath path = _tree.getSelectionPath();
		if(path != null) {
			boolean isEditing = (str == null || str.length() == 0);
			if(isEditing) {
				_tree.startEditingAtPath(path);
				((SNMPTreeCellEditor) _cellEditor).setCommandData((TreeNode) path.getLastPathComponent());
			} else {
				TreeNodeCommandStack.Command paste = new PasteCommand(this, path, str);
				_commandStack.add(paste);
			}
		}
	}

	public void copyData() {
		Set<TreePath> newpaths = new HashSet<TreePath>();
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0) {
			Collections.addAll(newpaths, paths);
			copyData(newpaths);
		}
		
	}
	
	private void copyData(Set<TreePath> paths) {
		if(paths != null && paths.size() > 0) {
			StringBuilder str = new StringBuilder();
			for(TreePath path: paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				copyNodeData(node, str);
			}
			if(str.length() > 0)
				setClipboardContents(str.toString());
		}
	}
	
	public void copyData(List<TreeNode> nodes) {
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
	
	
	public void pasteData() {
		TreePath[] paths = _tree.getSelectionPaths();
		pasteData(paths, null);
	}
	
	public void pasteData(TreePath path, String[] str) {
		if(path != null)
			pasteData(new TreePath[] {path}, str);
	}
	
	private void pasteData(TreePath[] paths, String[] str) {
		if(paths != null && paths.length > 0) {			
			TreeNodeCommandStack.Command paste = new PasteCommand(this, paths, str);
			_commandStack.add(paste);
		}
	}
	
	
	public void insertData() {
		TreePath[] paths = _tree.getSelectionPaths();
		insertData(paths, null);
	}
	
	public void insertData(TreePath path, String[] str) {
		if(path != null)
			insertData(new TreePath[] {path}, str);
	}
	
	public void insertData(TreePath[] paths, String[] str) {
		if(paths != null && paths.length > 0) {
			//if(str == null)
			//	str = getClipboardContents().split("\\r?\\n");
			TreeNodeCommandStack.Command insert = new InsertCommand(this, paths, str);
			_commandStack.add(insert);
		}
	}
	
	private void insertData(TreePath path, String str) {
		if(path != null) {
			TreeNodeCommandStack.Command insert = new InsertCommand(this, path, str);
			_commandStack.add(insert);
		}
	}
	
	
	public void cutNodes() {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0  && paths[0].getPathCount() > 1) {
			int result = JOptionPane.showConfirmDialog(null, "Do you want to cut the node(s)?");
			if(result == JOptionPane.YES_OPTION) {
				TreeNodeCommandStack.Command cut = new CutCommand(this, paths);
				_commandStack.add(cut);
			}
		}
	}
	
	
	public void removeNodes() {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0 && paths[0].getPathCount() > 1) {
			int result = JOptionPane.showConfirmDialog(null, "Do you want to delete the node(s)?");
			if(result == JOptionPane.YES_OPTION) {
				TreeNodeCommandStack.Command remove = new RemoveCommand(this, paths);
				_commandStack.add(remove);
			}
		}
	}
	
	
	public void undo() {
		_commandStack.undo();
	}
	
	public void redo() {
		_commandStack.redo();
	}
	
	@Override
	public void lostOwnership(Clipboard clip, Transferable str) {
		//do nothing
	} 
	
	public void setClipboardContents( String str ){
		StringSelection stringSelection = new StringSelection( str );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}
	
	public String getClipboardContents() {
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
	
	private class SNMPTreeRenderer extends DefaultTreeCellRenderer {
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row, boolean focus) {
    		TreePath path = tree.getPathForRow(row);
    		if(path != null) {
    			int level = path.getPathCount();
    			if(row > SNMPTreeCellEditor.ROOT) {
    				if(value == null || value.toString().length() == 0) {
    					switch(level) {
			    			//case COMMAND_NODE: value = "Add Command..."; break;
			    			case SNMPTreeCellEditor.IP_NODE: value = "Add IP..."; break;
			    			case SNMPTreeCellEditor.OID_NODE: value = "Add OID..."; break;
			    			default: break;
    					}
//    				} else if(level > SNMPTreeCellEditor.COMMAND_NODE) {
//    					setToolTipText("Drap and drop to duplicate");
    				}
    			}
    		}
			return super.getTreeCellRendererComponent(tree, value, isSelected, expanded,
					leaf, row, focus);
			}
	}
	

}
