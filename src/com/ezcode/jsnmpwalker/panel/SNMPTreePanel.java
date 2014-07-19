package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.browser.MibNode;

import com.ezcode.jsnmpwalker.SNMPTreeCellEditor;
import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.command.AddCommand;
import com.ezcode.jsnmpwalker.command.CreateSNMPCommand;
import com.ezcode.jsnmpwalker.command.CutCommand;
import com.ezcode.jsnmpwalker.command.EditSNMPCommand;
import com.ezcode.jsnmpwalker.command.InsertCommand;
import com.ezcode.jsnmpwalker.command.PasteCommand;
import com.ezcode.jsnmpwalker.command.RemoveCommand;
import com.ezcode.jsnmpwalker.command.TreeNodeCommandStack;
import com.ezcode.jsnmpwalker.data.SNMPDeviceData;
import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.data.SNMPTreeData;
import com.ezcode.jsnmpwalker.data.TransferableSNMPDeviceData;
import com.ezcode.jsnmpwalker.dialog.CommandDialog;
import com.ezcode.jsnmpwalker.layout.WrapLayout;
import com.ezcode.jsnmpwalker.listener.FieldPopupListener;
import com.ezcode.jsnmpwalker.listener.TreeDragGestureListener;
import com.ezcode.jsnmpwalker.menu.SNMPPopupMenu;
import com.ezcode.jsnmpwalker.target.TreeDropTarget;
import com.ezcode.jsnmpwalker.utils.ClipboardUtils;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPTreePanel extends JPanel  implements ClipboardOwner {
	public static final int OID_NODE = 4;
	public static final int IP_NODE = 3;
	public static final int COMMAND_NODE = 2;
	public static final int ROOT = 1;
	
	private JFrame _frame;
	private final JTabbedPane _snmpOptionPane = new JTabbedPane(JTabbedPane.TOP);
	private MibTreePanel _mibPanel;
	private JTree _tree;
	private DefaultTreeModel _treeModel;
	private TreeCellEditor _cellEditor;
	private TreeNodeCommandStack _commandStack;
	private FieldPopupListener _fieldPopupListener;
	private JButton _addCommand;
	private JButton _editCommand;
	
	public SNMPTreePanel(JFrame frame, MibTreePanel mibPanel, TreeNodeCommandStack commandStack) {
		super(new BorderLayout());
		_frame = frame;
		_mibPanel = mibPanel;
		_commandStack = commandStack;
		_fieldPopupListener = new FieldPopupListener();
		init();		
	}
	
	private void init() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Command");
		_treeModel = new DefaultTreeModel(root);
		_tree = new JTree(_treeModel);
		_tree.setEditable(true);
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
				tree.getSelectionModel().addSelectionPath(path);

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
					clearSNMPOptionPanel();
				} else if(_snmpOptionPane.getSelectedIndex() >= 0) {
					setSNMPOptionPanel();
				}
			}
		});
		DragSource treeDragSource = new DragSource();
		treeDragSource.createDefaultDragGestureRecognizer(_tree,
				DnDConstants.ACTION_COPY, new TreeDragGestureListener(_tree));
		
		
		TreeDropTarget dropTarget = new TreeDropTarget(this);
		_tree.setDropTarget(dropTarget);
		this.setDropTarget(dropTarget);

			
		final TreeSelectionModel selmodel = _tree.getSelectionModel();
		//allow multiple selection only on the same level
		selmodel.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath[] paths = selmodel.getSelectionPaths();
				if(paths == null || paths.length == 1) {
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
		
		JScrollPane sp = new JScrollPane(_tree);
		this.add(sp, BorderLayout.CENTER);
		
		JPanel bottomPane = new JPanel(new BorderLayout());
		
		//View options panel
		_snmpOptionPane.setUI(new MetalTabbedPaneUI());	
		java.net.URL imgURL = getClass().getResource("/img/properties.gif");
		ImageIcon icon = null;
		if(imgURL != null) {
			icon = new ImageIcon(imgURL);
		}
		_snmpOptionPane.addTab("", null);
		final JLabel bookmark = new JLabel("SNMP Options", icon, SwingConstants.LEFT);
		bookmark.setBackground(PanelUtils.UI_DEFAULTS.getColor("Button.light"));
		_snmpOptionPane.setTabComponentAt(0, bookmark);
		_snmpOptionPane.setSelectedIndex(-1);
		
		_snmpOptionPane.getTabComponentAt(0).addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				bookmark.setOpaque(false);
				if(_snmpOptionPane.getSelectedIndex() >= 0) {
					clearSNMPOptionPanel();
				} else {
					setSNMPOptionPanel();
				}
			}
			public void mouseEntered(MouseEvent e) {
				if(_snmpOptionPane.getSelectedIndex() < 0) {
					bookmark.setOpaque(true);
					bookmark.repaint();
				}
			}
			public void mouseExited(MouseEvent e) {
				if(_snmpOptionPane.getSelectedIndex() < 0) {
					bookmark.setOpaque(false);
					bookmark.repaint();
				}
			}
		});
		
		//button to add/edit commands
		JPanel commandButtPane = new JPanel(new WrapLayout(FlowLayout.LEFT));
		_addCommand = new JButton("Add Command");
		_addCommand.setMnemonic(KeyEvent.VK_A);
		_addCommand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openCommandDialog();
			}
			
		});
		
		_editCommand = new JButton("Edit Command");
		_editCommand.setMnemonic(KeyEvent.VK_E);
		_editCommand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath path = _tree.getSelectionPath();
				if(path != null && (path.getPathCount() == IP_NODE || path.getPathCount() == OID_NODE)) {
					openCommandDialog("Edit Command", path);
				} else {
					JOptionPane.showMessageDialog(null, "No command selected in the tree");
				}
			}
		});
		
		/*
		JButton removecommand = new JButton("Remove Commands");
		removecommand.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				removeNodes();
			}
			
		});
		*/
		commandButtPane.add(_addCommand);
		commandButtPane.add(_editCommand);
		//commandButtons.add(removecommand);
		
		bottomPane.add(commandButtPane, BorderLayout.NORTH);
		bottomPane.add(new JSeparator(SwingConstants.HORIZONTAL));
		bottomPane.add(_snmpOptionPane, BorderLayout.SOUTH);
		
		this.add(bottomPane, BorderLayout.SOUTH);
		
		//set keystrokes for action buttons
		_addCommand.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "AddCommand");
		_addCommand.getActionMap().put("AddCommand", new ButtonAction(_frame, _addCommand));
		_editCommand.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "EditCommand");
		_editCommand.getActionMap().put("EditCommand", new ButtonAction(_frame, _editCommand));
		
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "SNMP Command Tree"));
	}
	
	private void setSNMPOptionPanel() {
		_snmpOptionPane.setSelectedIndex(0);
		TreePath path = _tree.getSelectionPath();
		if(path != null && path.getPathCount() == IP_NODE) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			SNMPDeviceData data = (SNMPDeviceData) node.getUserObject();
			if(data != null)
				_snmpOptionPane.setComponentAt(0, new SNMPOptionEditPanel(this, data));
		} else {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel("Please, select an IP node to see SNMP options"));
			_snmpOptionPane.setComponentAt(0, panel);
		}
		_snmpOptionPane.repaint();
	}
	
	public void clearSNMPOptionPanel() {
		_snmpOptionPane.setSelectedIndex(-1);
		_snmpOptionPane.setComponentAt(0, null);
	}
	
	public JTree getTree() {
		return _tree;
	}
	
	public FieldPopupListener getFieldPopupListener() {
		return _fieldPopupListener;
	}
	
	public void enableCommandButtons(boolean enabled) {
		if(_addCommand != null)
			_addCommand.setEnabled(enabled);
		if(_editCommand != null)
			_editCommand.setEnabled(enabled);
	}
	
	public void openCommandDialog() {
		openCommandDialog("Create Command", null);
	}
	
	public void openCommandDialog(Object obj, int level) {
		openCommandDialog("Create Command", obj, level, null);
	}
	
	
	public void openCommandDialog(String title, TreePath path) {
		JDialog dg = null;
		if(path != null) {
			DefaultMutableTreeNode ipNode = null;
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if(path.getPathCount() == SNMPTreePanel.IP_NODE) {
				ipNode = (DefaultMutableTreeNode) node;
			} else if(path.getPathCount() == SNMPTreePanel.OID_NODE) {
				path = path.getParentPath();
				ipNode = (DefaultMutableTreeNode) node.getParent();
			}
			if(ipNode != null) {
				ArrayList<String> oids = new ArrayList<String>();
				Enumeration children = ipNode.children();
				while(children.hasMoreElements()) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
					oids.add((String) child.getUserObject());
				}
				DefaultMutableTreeNode commandNode = (DefaultMutableTreeNode) ipNode.getParent();
				SNMPDeviceData ipData = (SNMPDeviceData) ipNode.getUserObject();
				dg = new CommandDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, this, (String) commandNode.getUserObject(), ipData.getIp(), oids, (SNMPOptionModel) ipData.getOptions(), path);
			}
		} else {
			dg = new CommandDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, this);
		}
		if(dg != null) {
			dg.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(this, "Data not provided");
		}
	}
	
	public void openCommandDialog(String title, Object obj, int level, TreePath path) {
		if(obj != null) {
			JDialog dg = null;
			if(level == SNMPTreePanel.IP_NODE) {
				if(obj instanceof String) {
					dg = new CommandDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, this, (String) obj);
				} else if(obj instanceof SNMPDeviceData) {
					dg = new CommandDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, this, (SNMPDeviceData) obj, path);
				}
			} else if(level == SNMPTreePanel.OID_NODE && obj instanceof String[]) {
				dg = new CommandDialog((JFrame) SwingUtilities.getWindowAncestor(this), title, this, (String[]) obj);
			}
			if(dg != null)
				dg.setVisible(true);
		} else {
			openCommandDialog(title, path);
		}
	}
	
	
	public void addNodes(Object obj) {
		TreePath[] paths = _tree.getSelectionPaths();
		if(paths != null && paths.length > 0) {
			TreeNodeCommandStack.Command add = new AddCommand(this, paths, obj);
			_commandStack.add(add);
		}
	}
	
	public void addNode(DefaultMutableTreeNode parent, Object obj) {
		addNode(parent, obj, null);
	}
	
	public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode node) {
		addNode(parent, node, false);
	}
	
	public void addNode(DefaultMutableTreeNode parent, Object obj, DefaultMutableTreeNode node) {
		boolean isEditing = (obj == null || obj.toString().length() == 0);
		DefaultMutableTreeNode child = (node == null) ? new DefaultMutableTreeNode(obj) : node;
		addNode(parent, child, isEditing);
	}
	
	public void addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode node, boolean isEditing) {
		_treeModel.insertNodeInto(node, parent, parent.getChildCount());
		TreeNode[] path = parent.getPath();
		TreePath treepath = new TreePath(path);
		_tree.expandPath(treepath);
		if(isEditing) {
			_tree.startEditingAtPath(treepath.pathByAddingChild(node));
			((SNMPTreeCellEditor) _cellEditor).setCommandData(node);
		} 
	}
	
	
	public void editNode(Object obj) {
		TreePath path = _tree.getSelectionPath();
		if(path != null) {
			boolean isEditing = (obj == null || obj.toString().length() == 0);
			if(isEditing) {
				_tree.startEditingAtPath(path);
				((SNMPTreeCellEditor) _cellEditor).setCommandData((TreeNode) path.getLastPathComponent());
			} else {
				TreeNodeCommandStack.Command paste = new PasteCommand(this, path, obj);
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
			List<Object> list = new ArrayList<Object>();
			for(TreePath path: paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				Object obj = node.getUserObject();
				list.add(obj);
			}
			if(!list.isEmpty()) {
				ClipboardUtils.setClipboardContents(this, list);
			} 
		}
	}
	
	public void copyData(List<TreeNode> nodes) {
		StringBuilder str = new StringBuilder();
		List<Object> list = new ArrayList<Object>();
		for(TreeNode node: nodes) {
			Object obj = ((DefaultMutableTreeNode) node).getUserObject();
			list.add((SNMPDeviceData) obj);

		}
		if(!list.isEmpty()) {
			ClipboardUtils.setClipboardContents(this, list);
		} 
	}
	
	
	public void translateData() {
		TreePath path = _tree.getSelectionPath();
		if(path != null && path.getPathCount() == 4) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			String textOid = (String) node.getUserObject();
			Matcher matt = SNMPTreeData.MIB_PATTERN.matcher(textOid);
			if(matt.find()) {
				String mibFile = matt.group(1);
				String name = matt.group(2);
				String tail = matt.group(3);
				if(!_mibPanel.containsMib(mibFile)) {
					int result = JOptionPane.showConfirmDialog(null, mibFile + " is not loaded. Would you like to load " + mibFile + "?", "Mib file missing", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.YES_OPTION) {
						try {
							_mibPanel.loadDefaultMib(mibFile);
							//mibPanel.findMibNode(mibFile, matt.group(2));
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(null, "Can't find " + mibFile + ". Try to locate and load it manually");
							return;
						} catch (MibLoaderException e1) {
							JOptionPane.showMessageDialog(null, "Can't load " + mibFile + ". Try to locate and load it manually");
							return;
						}
					}
				}
				TreePath mibPath = _mibPanel.findMibNode(mibFile, name, true);
				if(mibPath != null) {
					MibNode mibNode = (MibNode) mibPath.getLastPathComponent();
					String oid = mibNode.getOid() + ((tail == null)?"":tail);
					pasteData(path, oid);
				}
			} else {
				JOptionPane.showMessageDialog(null, "This entry doesn't have a MIB specified. Please, add it in the beginning with a '::' or translate it manually");
			}
		}
	}
	
	public void pasteData() {
		TreePath[] paths = _tree.getSelectionPaths();
		pasteData(paths, null);
	}
	
	public void pasteData(TreePath path, Object obj) {
		if(path != null)
			pasteData(new TreePath[] {path}, new Object[] {obj});
	}
	
	public void pasteData(TreePath path, Object[] obj) {
		if(path != null)
			pasteData(new TreePath[] {path}, obj);
	}
	
	private void pasteData(TreePath[] paths, Object[] obj) {
		if(paths != null && paths.length > 0) {			
			TreeNodeCommandStack.Command paste = new PasteCommand(this, paths, obj);
			_commandStack.add(paste);
		}
	}
	
	public void insertData() {
		TreePath[] paths = _tree.getSelectionPaths();
		insertData(paths, null);
	}
	
	public void insertData(TreePath path, Object[] obj) {
		if(path != null)
			insertData(new TreePath[] {path}, obj);
	}
	
	public void insertData(TreePath[] paths, Object[] obj) {
		if(paths != null && paths.length > 0) {
			TreeNodeCommandStack.Command insert = new InsertCommand(this, paths, obj);
			_commandStack.add(insert);
		}
	}
	
	private void insertData(TreePath path, Object obj) {
		if(path != null) {
			TreeNodeCommandStack.Command insert = new InsertCommand(this, path, obj);
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
	
	public void removeNodes(TreePath[] paths) {
		if(paths != null && paths.length > 0 && paths[0].getPathCount() > 1) {
			int result = JOptionPane.showConfirmDialog(null, "Do you want to delete the selection?");
			if(result == JOptionPane.YES_OPTION) {
				TreeNodeCommandStack.Command remove = new RemoveCommand(this, paths);
				_commandStack.add(remove);
			}
		}
	}
	
	public void removeNode(TreePath path) {
		removeNodes(new TreePath[] {path});
	}
	
	public void removeNodes() {
		TreePath[] paths = _tree.getSelectionPaths();
		removeNodes(paths);
	}
	
	public void createSNMP(Object[] obj) {
		if(obj != null && obj.length > 0) {
			TreeNodeCommandStack.Command createSNMP = new CreateSNMPCommand(this, obj);
			_commandStack.add(createSNMP);
		}
	}
	
	public void editSNMP(TreePath path, Object ip, Collection<String> oids) {
		TreeNodeCommandStack.Command editSNMP = new EditSNMPCommand(this, path, ip, oids);
		_commandStack.add(editSNMP);
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
	
/*
	
	public void setClipboardContents( Object obj ){
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if(obj instanceof String) {
			StringSelection stringSelection = new StringSelection( obj.toString() );
			clipboard.setContents( stringSelection, this );
		} else if (obj instanceof SNMPDeviceData){
			TransferableSNMPDeviceData dataSelection = new TransferableSNMPDeviceData((SNMPDeviceData) obj, SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
			clipboard.setContents(dataSelection, this);
		} else if (obj instanceof List) {
			List list = (List) obj;
			if(!list.isEmpty()) {
				Object first = list.get(0);
				if(first instanceof String) {
					StringBuilder str = new StringBuilder();
					for(Object s: list) {
						str.append(s);
						str.append("\n");
					}
					StringSelection stringSelection = new StringSelection( str.toString() );
					clipboard.setContents( stringSelection, this );
				} else if(first instanceof SNMPDeviceData) {
					TransferableSNMPDeviceData dataSelection = new TransferableSNMPDeviceData((List<SNMPDeviceData>) obj, SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
					clipboard.setContents(dataSelection, this);
				}
			}
		}
	}
	
	public Object getClipboardContents() {
		Object result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		if(contents != null) {
			try {
				if (contents.isDataFlavorSupported(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR)) {
					result = contents.getTransferData(SNMPDeviceData.SNMP_DEVICE_DATA_FLAVOR);
				} else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			    }
			} catch (UnsupportedFlavorException ex){
			    System.out.println(ex);
			    ex.printStackTrace();
			} catch (IOException ex) {
			    System.out.println(ex);
			    ex.printStackTrace();
			 }
		 }
		 return result;
	}
*/
	
	private class SNMPTreeRenderer extends DefaultTreeCellRenderer {
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row, boolean focus) {
    		TreePath path = tree.getPathForRow(row);
    		if(path != null) {
    			int level = path.getPathCount();
    			if(row > SNMPTreePanel.ROOT) {
    				if(value == null || value.toString().length() == 0) {
    					switch(level) {
			    			//case COMMAND_NODE: value = "Add Method..."; break;
			    			case SNMPTreePanel.IP_NODE: value = "Add IP..."; break;
			    			case SNMPTreePanel.OID_NODE: value = "Add OID..."; break;
			    			default: break;
    					}
    				}
    			}
    		}
			return super.getTreeCellRendererComponent(tree, value, isSelected, expanded,
					leaf, row, focus);
			}
	}
	

}
