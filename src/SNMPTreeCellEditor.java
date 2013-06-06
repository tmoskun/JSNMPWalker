/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under MIT license
 */

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.CellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class SNMPTreeCellEditor implements TreeCellEditor {
	
	public static final int ROOT = 1;
	public static final int COMMAND_NODE = 2;
	public static final int IP_NODE = 3;
	public static final int OID_NODE = 4;
	
	private SNMPCellEditor _fieldEditor;
	private TreeNodeCommandStack _commandStack;
	private TreeNodeCommandStack.Command _command = null;
	private JTree _tree;
		
	public SNMPTreeCellEditor(JTree tree, TreeNodeCommandStack commandStack) {
		_fieldEditor = new SNMPCellEditor();
		_commandStack = commandStack;
		_tree = tree;
	}
		
	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value,
			boolean isSelected, boolean expanded, boolean leaf, int row) {
	    TreePath path = tree.getPathForRow(row);
	    if(value == null || value.toString().length() == 0) {
		    switch(path.getPathCount()) {
		    	case COMMAND_NODE: _fieldEditor.setText("Add Command..."); break;
		    	case IP_NODE: _fieldEditor.setText("Add IP..."); break;
		    	case OID_NODE: _fieldEditor.setText("Add OID..."); break;
		    	default: break;
		    }
	    } else {
	    	_fieldEditor.setText(value.toString());
	    }
	    return (Component) _fieldEditor;
	}
		
	@Override
	public Object getCellEditorValue() {
		return _fieldEditor.getCellEditorValue();
	}
		
	@Override
	public void cancelCellEditing() {
		_fieldEditor.cancelCellEditing();			
	}

	@Override
	public boolean isCellEditable(EventObject event) { 
	    if (event != null && event.getSource() instanceof JTree && event instanceof MouseEvent && _fieldEditor.isCellEditable(event))  {  
	        MouseEvent mouseEvent = (MouseEvent)event;  
	        JTree tree = (JTree)event.getSource();  
	        TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());  
	        return path.getPathCount() > COMMAND_NODE; // root and command are not editable  
	    } else if(event == null && _fieldEditor.isCellEditable(event)) {
	    	return true; //editing called explicitly
	    }
	    return false;
	}
		
	@Override
	public boolean shouldSelectCell(EventObject event) {
		return _fieldEditor.shouldSelectCell(event);
	}

	@Override
	public boolean stopCellEditing() {
		return _fieldEditor.stopCellEditing();
	}
		
	@Override
	public void addCellEditorListener(CellEditorListener l) {
		_fieldEditor.addCellEditorListener(l);
			
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		_fieldEditor.removeCellEditorListener(l);		
	}
		
	public void fireEditingStopped() {
		_fieldEditor.fireEditingStopped();
	}
	
	public void setCommandData(TreeNode node) {
		_fieldEditor.setCommandData(node);
	}
		
		
	private class SNMPCellEditor extends JTextField implements CellEditor {
		private String _value = "";
		private Vector _listeners;
		private ActionListener _editListener;
			
		public SNMPCellEditor() {
			this("", 20);
		}
		public SNMPCellEditor(String str) {
			this(str, 20);
		}
		public SNMPCellEditor(int i) {
			this("", i);
		}
		public SNMPCellEditor(String str, int i) {
			super(str, i);
			_listeners = new Vector();
			_editListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
				    if (stopCellEditing()) {		
				        fireEditingStopped();
				    }		
				}	
			};
			addActionListener(_editListener);
			this.addMouseListener(new MouseAdapter() {
				//remove text when the field is clicked, if the text is default
				public void mousePressed(MouseEvent event) {
					if(_value == null || _value.length() == 0) {
						setText("");
					}
				} 
			});
						
		}
		
		protected void setCommandData(TreeNode node) {
			if(_command != null) {
				((EditCommand) _command).setNode(node);
			}
		}
		
		
		@Override
		public void cancelCellEditing() {
//			if(_tree.isEditing()) {
				setText("");
				_command = null;
//			} else {
//				stopCellEditing();
//				fireEditingStopped();
//			}
		}

		@Override
		public Object getCellEditorValue() {
			return _value;
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			if ((event == null) || ((event instanceof MouseEvent) && (((MouseEvent) event).getClickCount() == 2))) {
				if(event == null) {
					_command = new EditCommand();				
				} else {
					JTree tree = (JTree) event.getSource();
					TreePath path = tree.getSelectionPath();
					_command = new EditCommand(path);
				}
				return true;
			}
			return false;
		}
			
		@Override
		public boolean shouldSelectCell(EventObject event) {
			String text = getText();
			if(isValidText(text)) 
				_value = text;
			else
				_value = "";
			return true;
		}

		@Override
		public boolean stopCellEditing() {
			String text = getText().trim();
			if(isValidText(text)) {
				if(_tree.isEditing()) {
					_value = text;
				} else if(_tree.getSelectionCount() > 0) {
					TreePath path = _tree.getSelectionPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
					node.setUserObject(getText());
				} else {
					_value = text;
				}
				if(_command != null)
					_commandStack.add(_command);
				return true;
			}
			return false;
		}
		
		private boolean isValidText(String text) {
			//TODO: better validation
			return !text.trim().endsWith("...");
		}
		
		@Override
		public void addCellEditorListener(CellEditorListener l) {
			_listeners.add(l);
				
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			_listeners.remove(l);		
		}
			
		protected void fireEditingStopped() {
			if (_listeners.size() > 0) {
				ChangeEvent ce = new ChangeEvent(this);
				for (int i = _listeners.size() - 1; i >= 0; i--) {
				    ((CellEditorListener) _listeners.elementAt(i))
				            .editingStopped(ce);
				}
			}
		}
			
	}	
	
	private class EditCommand implements TreeNodeCommandStack.Command {
		protected String _savedData;
		protected String _newData = null;
		protected TreeNode _node = null;
		
		public EditCommand() {
			this(null);
		}
		
		public EditCommand(TreePath path) {
			setNode(path);
		}
		
		protected void setNode(TreePath path) {
			if(path == null) {
				_savedData = "";
			} else {
				setNode((TreeNode) path.getLastPathComponent());
			}
		}
		
		protected void setNode(TreeNode node) {
			_node = node;
			_savedData = _node.toString();
		}
		
		protected void setData(String str) {
			if(str != null) {
				((DefaultMutableTreeNode) _node).setUserObject(str);
				((DefaultTreeModel)_tree.getModel()).nodeChanged(_node);
			}
		}
		
		public void execute() {
			setData(_newData);
			
		}

		public void undo() {
			_newData = _node.toString();
			setData(_savedData);
		}
		
	}
	
}
