package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.browser.MibNode;
import net.percederberg.mibble.browser.MibTreeBuilder;
import net.percederberg.mibble.value.ObjectIdentifierValue;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.listener.MibDragGestureListener;

public class MibPanel extends JPanel {
	private JMenu _mibMenu;
	private JMenuItem _findmib;
	private JMenuItem _findnext;
	private JMenuItem _findprev;
	private String _searchKey = "";
	private TreePath _searchResult = null;
	private JMenuItem _unloadmib;
	private JMenuItem _unloadall;
	
	private JTree _mibTree;
	private JLabel _loadingDataImg;
	private MibLoader _mibLoader;
	private SNMPOutputPanel _outputPane;
	
	public MibPanel(JLabel loadingDataImg, SNMPOutputPanel outputPane) {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "MIB Browser"));
		_loadingDataImg = loadingDataImg;
		_outputPane = outputPane;
		init();
		createMenu();
	}
	
	private void init() {

		_mibLoader = new MibLoader();
		_mibTree = MibTreeBuilder.getInstance().getTree();
		_mibTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		JScrollPane mibTreePane = new JScrollPane();
		//mibTreePane.getViewport().setPreferredSize(new Dimension(250, 800));
		mibTreePane.getViewport().add(_mibTree);
		
		JPanel oidPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		oidPane.add(new JLabel("OID"));
		final JTextField showOid = new JTextField();
		showOid.setPreferredSize(new Dimension(220, 20));
		showOid.setEditable(false);
		showOid.setBackground(Color.decode("#F9F9F9"));
		oidPane.add(showOid);
        _mibTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
            	TreePath path = _mibTree.getSelectionPath();
            	if(path == null) {
	            		showOid.setText("");
	            } else {
	            		MibNode node = (MibNode) path.getLastPathComponent();
	            		showOid.setText(node.getOid());
	            }
            }
        });
		DragSource mibDragSource = new DragSource();
		mibDragSource.createDefaultDragGestureRecognizer(_mibTree,
				DnDConstants.ACTION_COPY, new MibDragGestureListener(_mibTree));
//		ds.createDefaultDragGestureRecognizer(oid,
//				DnDConstants.ACTION_COPY, new MibDragGestureListener());
		
		add(mibTreePane, BorderLayout.CENTER);
		add(oidPane, BorderLayout.SOUTH);
		
	}
	
/*
	public void printPreorder() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _mibTree.getModel().getRoot();
		Enumeration _preorder = root.preorderEnumeration();
		while(_preorder.hasMoreElements()) {
			System.out.println(_preorder.nextElement());
		}
	}
*/
	
	private void createMenu() {
		_mibMenu = new JMenu("MIB");
		_mibMenu.setMnemonic(KeyEvent.VK_M);
		_findmib = new JMenuItem("Find MIB node", KeyEvent.VK_F);
		_findmib.setEnabled(false);
		_findnext = new JMenuItem("Find next MIB node", KeyEvent.VK_N);
		_findnext.setEnabled(false);
		_findprev = new JMenuItem("Find previous MIB node", KeyEvent.VK_P);
		_findprev.setEnabled(false);
		JMenuItem loadfile = new JMenuItem("Load MIB file", KeyEvent.VK_L);
		loadfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		JMenuItem loaddir = new JMenuItem("Load MIBs from directory", KeyEvent.VK_D);
		_unloadmib = new JMenuItem("Unload MIB", KeyEvent.VK_U);
		_unloadmib.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		_unloadmib.setEnabled(false);
		_unloadall = new JMenuItem("Unload All", KeyEvent.VK_A);
		_unloadall.setEnabled(false);
		
		_findmib.addActionListener(new KeyMibSearchListener());
		_findnext.addActionListener(new NextMibSearchListener());
		_findprev.addActionListener(new PrevMibSearchListener());
		
		_mibMenu.add(_findmib);
		_mibMenu.add(_findnext);
		_mibMenu.add(_findprev);
		_mibMenu.addSeparator();

		
		loadfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileHidingEnabled(false);
				fc.setFileSelectionMode( JFileChooser.FILES_ONLY);
				int returnval = fc.showOpenDialog(null);
				if(returnval == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
			        Loader loader = new Loader(new File[] {file});
			        loader.start();
				}
				
			}
			
		});
		_mibMenu.add(loadfile);
		
		loaddir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setFileHidingEnabled(false);
				fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
				int returnval = fc.showOpenDialog(null);
				if(returnval == JFileChooser.APPROVE_OPTION) {
					File dirfile = fc.getSelectedFile();
					File[] files = dirfile.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return (new File(dir, name)).isFile();
						}			
					});
					Loader loader = new Loader(files);
					loader.start();
				}
				
			}	
		});
		_mibMenu.add(loaddir);
		_mibMenu.addSeparator();
		
		_unloadmib.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath path = _mibTree.getSelectionPath();
				if(path != null) {
					unloadMib(path.getLastPathComponent().toString());
				}
				
			}
		});
		_mibMenu.add(_unloadmib);
		
		_unloadall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unloadAllMibs();
			}
		});
		_mibMenu.add(_unloadall);
	}
	
	
	public JMenu getMibMenu() {
		return _mibMenu;
	}
	
	
	public void loadDefaultMib(String src) {
        Mib mib = null;
		try {
			mib = _mibLoader.load(src);
	        MibTreeBuilder.getInstance().addMib(mib);
	        enableButtons(true);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MibLoaderException e) {
            e.getLog().printTo(new PrintStream(System.out));
		}
	}

	
	public void loadMib(String src) {
		File file = new File(src);
		loadMib(file);
	}
	
	public void loadMib(File file) {
		//_mibLoader.addDir(file.getParentFile());
        String message = null;
        Mib mib = null;
        //setStatus("Loading " + file + "...");
		try {
			if(file.exists()) {
	            if (_mibLoader.getMib(file) != null) {
	                return;
	            }
	            if (!_mibLoader.hasDir(file.getParentFile())) {
	                _mibLoader.removeAllDirs();
	                _mibLoader.addDir(file.getParentFile());
	            }
	            mib = _mibLoader.load(file);
			} else {
				_mibLoader.load(file);
			}
	        MibTreeBuilder.getInstance().addMib(mib);
	        enableButtons(true);
		} catch (IOException e) {
			 message = "Can't find file " + file.getAbsolutePath() + ": " + e.getMessage();
		} catch (MibLoaderException e) {
            message = "Failed to load " + file.getAbsolutePath();
            ByteArrayOutputStream panelOut = new ByteArrayOutputStream();
            e.getLog().printTo(new PrintStream(panelOut));
            _outputPane.appendResult(panelOut.toString());
            System.out.println(panelOut.toString());
		}
        if (message != null) {
            JOptionPane.showMessageDialog(null, message, "Can't load MIB", JOptionPane.ERROR_MESSAGE);
        }
	}
		
    public void unloadMib(String name) {
        Mib mib = _mibLoader.getMib(name);
        if (mib != null) {
            //File file = mib.getFile();
            try {
                _mibLoader.unload(name);
                MibTreeBuilder.getInstance().unloadMib(name);
                refreshMibTree();
                if(_mibLoader.getAllMibs().length == 0) {
                	enableButtons(false);
                }
            } catch (MibLoaderException ignore) {
                // MIB loader unloading is best-attempt only
            }
        }
    }
    
    public void unloadAllMibs() {
    	_mibLoader.unloadAll();
		MibTreeBuilder.getInstance().unloadAllMibs();
		refreshMibTree();
		enableButtons(false);
    }
    
    public void refreshMibTree() {
        ((DefaultTreeModel) _mibTree.getModel()).reload();
        _mibTree.repaint();
    }
    
    private void enableButtons(boolean enable) {
    	_findmib.setEnabled(enable);
    	_findnext.setEnabled(enable);
    	_findprev.setEnabled(enable);
    	_unloadmib.setEnabled(enable);
    	_unloadall.setEnabled(enable);
    }
    
    private abstract class MibSearchListener implements ActionListener {
    	protected Iterator<TreeNode> _mibSearch;
    	
    	public MibSearchListener(boolean forward) {
    		_mibSearch = new MibSearchIterator(forward);
    	}
    	
        protected String getSearchKey() {
    		String str = (String) JOptionPane.showInputDialog(null, "Enter MIB name or IP", "Find", JOptionPane.PLAIN_MESSAGE, null, null, _searchKey);
    		if(str != null) {
    			str = str.trim(); 
    			if(str.length() == 0) {
    				JOptionPane.showMessageDialog(null, "Search Value not provided", "Value not provided", JOptionPane.WARNING_MESSAGE);
    			}
    		}
    		return str;
        }
        
        protected TreePath find(String key) {
        	return find(key, null);
        }
        
        protected TreePath find(String key, TreePath prevResult) {
        	TreePath path  = null;
        	TreePath foundPath = null;
			if(key != null && key.length() > 0) {
				_searchKey = key;
    			((MibSearchIterator)_mibSearch).init(prevResult);
    			while(_mibSearch.hasNext()) {
    				//path = _mibSearch.next();
    				MibNode node = (MibNode) _mibSearch.next();
    				//MibNode node = (MibNode) path.getLastPathComponent();
    				String name = node.getName().replaceAll("\\(\\d+\\)\\s*$", "").trim();
    				String oid = node.getOid().trim();
    				if(key.equalsIgnoreCase(name) || key.equals(oid)) {
    					path = new TreePath(node.getPath());
    					foundPath = path;
    					_mibTree.setSelectionPath(path);
    					break;
    				}
    			}
    			//System.out.println("result " + foundPath);
    			if(foundPath == null)
    				JOptionPane.showMessageDialog(null, "Key not found");
			}
//			} else {
//				JOptionPane.showMessageDialog(null, "Key not provided");
//			}
			return foundPath;
        }
        
        protected TreePath findNext() {
			if(_searchKey == null || _searchKey.length() == 0) {
				_searchKey = getSearchKey();
			} 
			return find(_searchKey, _searchResult);	
        }
    }
    
    private class KeyMibSearchListener extends MibSearchListener {
    	
    	public KeyMibSearchListener() {
			super(true);
		}
    	
		@Override
    	public void actionPerformed(ActionEvent e) {
			String key = getSearchKey();
			_searchResult = find(key);
		}
    }
    
    private class NextMibSearchListener extends MibSearchListener {

		public NextMibSearchListener() {
			super(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath result = findNext();
			if(result != null)
				_searchResult = result;
		}
    	
    }
    
    private class PrevMibSearchListener extends MibSearchListener {

		public PrevMibSearchListener() {
			super(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath result = findNext();
			if(result != null)
				_searchResult = result;		
		}
    	
    }
    
    private class MibSearchIterator implements Iterator<TreeNode> {
    	
    	private boolean _forward;
    	private List<TreeNode> _nodes;
    	private Enumeration<TreeNode> _traversalOrder;
    	
    	public MibSearchIterator(boolean forward) {
    		//_next = next;
    		_forward = forward;
    		_nodes = new ArrayList<TreeNode>();
    	}
    	
    	public void init() {
    		init(null);
    	}
    	
    	public void init(TreePath prevResult) {
    		_nodes.clear();
    		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _mibTree.getModel().getRoot();
        	TreePath startPath = null;
        	if(prevResult != null) { 
        		startPath = prevResult;
        	} else {
//        		selPath = _mibTree.getSelectionPath();
//    		if(selPath == null)
        		DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) root.getLastChild();
    			startPath = _forward ? _mibTree.getPathForRow(0) : new TreePath(lastNode.getPath());
       	    }

    		TreePath path = getPathAfterRoot(root, startPath);   		
        	setTraversal(path, startPath);
    		int index = _mibTree.getModel().getIndexOfChild(root, path.getLastPathComponent());
        	if(_forward) {
	        	int last = root.getChildCount();
				for(int i = index + 1; i < last; i++) {
					_nodes.add(root.getChildAt(i));
				}
    		} else {
    			for(int i = index - 1; i >= 0; i--) {
					_nodes.add(root.getChildAt(i));
    			}
    		}
        	if(prevResult != null) {
        		next(); 
        	}
    	}
    	
    	private TreePath getPathAfterRoot(TreeNode root, TreePath path) {
    		TreeNode node = (TreeNode) path.getLastPathComponent();
    		if(node.getParent().equals(root)) {
    			return path;
    		} else {
    			return getPathAfterRoot(root, path.getParentPath());
    		}
    	}
    	
    	private void setTraversal(DefaultMutableTreeNode node) {
    		if(_forward) {
    			_traversalOrder = node.preorderEnumeration();
    		} else {
    			_traversalOrder = node.postorderEnumeration();
    		}
    	}
    	
    	private void setTraversal(TreePath path, TreePath startPath) {
    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    		setTraversal(node);
    		if(startPath != null) {
    			while(_traversalOrder.hasMoreElements() && !path.equals(startPath)) {
    				DefaultMutableTreeNode n = (DefaultMutableTreeNode) _traversalOrder.nextElement();
    				path = new TreePath(n.getPath());
    			}
    		}
    	}
    	
    	
    	private Iterator<TreeNode> getIterator(DefaultMutableTreeNode node) {
    		Enumeration depthFirst = node.depthFirstEnumeration();
			List<TreeNode> depthFirstList = Collections.list(depthFirst);
			if(_forward) {
				Collections.reverse(depthFirstList);
			}
			return depthFirstList.iterator();
    	}
    		

		@Override
		public boolean hasNext() {
			return _traversalOrder.hasMoreElements() || !_nodes.isEmpty();
			//return !_stack.isEmpty() || !_nodes.isEmpty();
			//return _depthFirstIterator.hasNext() || !_nodes.isEmpty();
		}

		@Override
		public TreeNode next() {
			if(!_traversalOrder.hasMoreElements()) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) _nodes.get(0);
				setTraversal(node);
				_nodes.remove(0);
			}
			//TreeNode next = (TreeNode) _traversalOrder.nextElement();
			//System.out.println(next);
			//return next;
			return (TreeNode) _traversalOrder.nextElement();
		}

		@Override
		public void remove() {
			//ignore			
		}
					
    }
      
	
    /**
     * A background MIB loader. This class is needed in order to
     * implement the runnable interface to be able to load MIB files
     * in a background thread.
     */
    private class Loader implements Runnable {

        /**
         * The MIB files to load.
         */
        private File[] files;

        /**
         * Creates a new background MIB loader.
         *
         * @param files          the MIB files to load
         */
        public Loader(File[] files) {
            this.files = files;
        }
        
        /**
         * Toggles gadgets
         */
        
        private void toggleMibLoad(boolean isrun) {
        	_loadingDataImg.setVisible(isrun);
        	_mibMenu.setEnabled(!isrun);
        }

        /**
         * Starts the background loading thread.
         */
        public void start() {
            Thread  thread;

            if (files.length > 0) {
                thread = new Thread(this);
                thread.start();
            }
        }

        /**
         * Runs the MIB loading. This method should only be called by
         * the thread created through a call to start().
         */
        public void run() {
            //setBlocked(true);
        	toggleMibLoad(true);
            for (int i = 0; i < files.length; i++) {
                loadMib(files[i].toString());
            }
            refreshMibTree();
            //setBlocked(false);
            toggleMibLoad(false);
        }
    }

}
