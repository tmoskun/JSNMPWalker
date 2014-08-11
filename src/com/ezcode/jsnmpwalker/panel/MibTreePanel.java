package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FilenameFilter;
import java.net.URL;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.browser.MibNode;
import net.percederberg.mibble.browser.MibTreeBuilder;

import com.ezcode.jsnmpwalker.dialog.MibSearchDialog;
import com.ezcode.jsnmpwalker.iterator.TreeSearchIterator;
import com.ezcode.jsnmpwalker.listener.MibDragGestureListener;
import com.ezcode.jsnmpwalker.menu.MibPopupMenu;
import com.ezcode.jsnmpwalker.search.SearchResultNotFoundException;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.search.SearchableTree;
import com.ezcode.jsnmpwalker.search.TreeSearcher;

public class MibTreePanel extends JPanel implements SearchableTree {
	final private static String CURRENT_INCLUDE_TEXT_OPTION = "includetext";
	
	final public static String FIND_MIB_ITEM = "Find MIB node";
	final public static String FIND_NEXT_MIB_ITEM = "Find next MIB node";
	final public static String FIND_PREV_MIB_ITEM = "Find previous MIB node";
	final public static String LOAD_MIB_ITEM = "Load MIB file";
	final public static String LOAD_DIR_ITEM = "Load MIBs from directory";
	final public static String UNLOAD_MIB_ITEM = "Unload MIB";
	final public static String UNLOAD_ALL_ITEM = "Unload All";

	private JMenu _mibMenu;
	private JMenuItem _findmib;
	private JMenuItem _findnext;
	private JMenuItem _findprev;
	private JMenuItem _unloadmib;
	private JMenuItem _unloadall;
	
	private JTree _mibTree;
	private JLabel _loadingDataImg;
	private MibLoader _mibLoader;
	private MibBrowserPanel _mibBrowserPane;
	
	private Preferences _prefs;
	private TreeSearchIterator _nextSearchIterator;
	private TreeSearchIterator _prevSearchIterator;
	private String _currentSearchKey = "";
	private TreePath _validSearchResult = null;
	private TreePath _currentSearchResult = null;
	private MibViewPanel _mibView = null;
	private MibSearchDialog _dg;
	
	private static MibTreePanel _instance = null;

	
	public static MibTreePanel getInstance() {
		return _instance;
	}
	
	public static MibTreePanel getInstance(JLabel loadingDataImg, MibBrowserPanel mibBrowserPane) {
		if(_instance == null) {
			_instance = new MibTreePanel(loadingDataImg, mibBrowserPane);
		}
		return _instance;
	}
	
	private MibTreePanel(JLabel loadingDataImg, MibBrowserPanel mibBrowserPane) {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "MIB Tree"));
		_prefs = Preferences.userNodeForPackage(getClass());
		_loadingDataImg = loadingDataImg;
		_mibBrowserPane = mibBrowserPane;
		init();
		createMenu();
		_dg = new MibSearchDialog((JFrame) SwingUtilities.getWindowAncestor(this), this, _currentSearchKey, _prefs.getBoolean(CURRENT_INCLUDE_TEXT_OPTION, false));
	}
	
	
	
	private void init() {

		_mibLoader = new MibLoader();
		_mibTree = MibTreeBuilder.getInstance().getTree();
		_mibTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		_nextSearchIterator = new TreeSearchIterator(_mibTree, true);
		_prevSearchIterator = new TreeSearchIterator(_mibTree, false);
		
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
	            	//showMibDescription(null);
	            } else {
	            	MibNode node = (MibNode) path.getLastPathComponent();
	            	showOid.setText(node.getOid());
	            	//showMibDescription(node);
	            }
            }
        });
		_mibTree.addMouseListener(new MouseAdapter() {
			private void MibPopupEvent(MouseEvent event) {
				int x = event.getX();
				int y = event.getY();
	            JTree tree = (JTree)event.getSource(); 			
				TreePath path = tree.getPathForLocation(x, y);
				if (path == null)
					return;	
				tree.getSelectionModel().addSelectionPath(path);
				//tree.setSelectionPath(path);
				MibNode node = (MibNode) path.getLastPathComponent();
				if(node.getLevel() == 1 || node.getValue() != null) {
					MibPopupMenu popup = new MibPopupMenu(new Point(x, y));
					popup.buildMenu();
					popup.show(tree, x, y);
				}
			}
			
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					MibPopupEvent(e);
				} 
			}
			
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					MibPopupEvent(e);
				} 
			}

		});
		DragSource mibDragSource = new DragSource();
		mibDragSource.createDefaultDragGestureRecognizer(_mibTree,
				DnDConstants.ACTION_COPY, new MibDragGestureListener(_mibTree));
		
		add(mibTreePane, BorderLayout.CENTER);
		add(oidPane, BorderLayout.SOUTH);
		
	}
	
	
	private void showMibDescription(int mode) {
		showMibDescription(_mibTree.getSelectionPath(), mode);
	}
	
	private void showMibDescription(TreePath path, int mode) {
		if(path != null) {
			TreeNode node = (TreeNode) path.getLastPathComponent();
			if(node instanceof MibNode && (((DefaultMutableTreeNode) node).getLevel() == 1 || ((MibNode) node).getValue() != null)) {
				MibTreePanel.this.showMibDescription((MibNode) node, mode);
			} else {
				JOptionPane.showMessageDialog(MibTreePanel.this, "No MIB selected");
			}
		} else {
			JOptionPane.showMessageDialog(MibTreePanel.this, "No MIB selected");
		}
	}
	
	public void showMibDescription(MibNode node) {
		showMibDescription(node, MibBrowserPanel.CURRENT_WINDOW_MODE);
	}
	
	public void showMibDescription(MibNode node, int mode) {
		if(node != null) {
			_mibBrowserPane.setMibDescription(getMibDescription(node), MibBrowserPanel.getMibName(node.getName()), mode);
		}
	}
	
	public String getMibDescription(MibNode node) {
		String descr = node.getDescription();
		if(descr == null || descr.trim().length() == 0) {
			Mib mib = _mibLoader.getMib(node.getName());
			if ( mib != null ) {
				File file = mib.getFile();
				if(file != null && file.exists()) {
					try {
						descr = new Scanner(file).useDelimiter("\\Z").next();
					} catch (FileNotFoundException e) {
						System.out.println("File not found");
						e.printStackTrace();
					}
				} else {
					//search in resources
					String name = mib.getName();
					ClassLoader loader = getClass().getClassLoader();
					String[] resourceDirs = _mibLoader.getResourceDirs();
					for(String dir: resourceDirs) {
						try {
							URL resource = loader.getResource(dir + File.separator + name);
							descr = new Scanner(resource.openStream()).useDelimiter("\\Z").next();
						} catch(Exception ex) {
							//ignore
						} 
					}
				}
			}
		}
		return descr;
	}
	
	
	public JTree getTree() {
		return _mibTree;
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
		JMenuItem showItem = new JMenuItem(MibPopupMenu.SHOW_ITEM, KeyEvent.VK_S);
		showItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		showItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MibTreePanel.this.showMibDescription(MibBrowserPanel.CURRENT_WINDOW_MODE);
			}
			
		});
		JMenuItem showItemNewWin = new JMenuItem(MibPopupMenu.SHOW_NEW_WIN_ITEM, KeyEvent.VK_E);
		showItemNewWin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		showItemNewWin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MibTreePanel.this.showMibDescription(MibBrowserPanel.NEW_TAB_MODE);
			}
		});
		JMenuItem showItemFloating = new JMenuItem(MibPopupMenu.SHOW_FLOATING_ITEM, KeyEvent.VK_O);
		showItemFloating.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
		showItemFloating.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MibTreePanel.this.showMibDescription(MibBrowserPanel.FLOATING_WINDOW_MODE);
			}
		});
		ActionListener mibListener = new MibListener();
		_findmib = new JMenuItem(FIND_MIB_ITEM, KeyEvent.VK_F);
		_findmib.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		_findmib.setEnabled(false);
		_findmib.addActionListener(mibListener);
		_findnext = new JMenuItem(FIND_NEXT_MIB_ITEM, KeyEvent.VK_N);
		_findnext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		_findnext.setEnabled(false);
		_findnext.addActionListener(mibListener);
		_findprev = new JMenuItem(FIND_PREV_MIB_ITEM, KeyEvent.VK_P);
		_findprev.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		_findprev.setEnabled(false);
		_findprev.addActionListener(mibListener);
		JMenuItem loadfile = new JMenuItem(LOAD_MIB_ITEM, KeyEvent.VK_L);
		loadfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		loadfile.addActionListener(mibListener);
		JMenuItem loaddir = new JMenuItem(LOAD_DIR_ITEM, KeyEvent.VK_D);
		loaddir.addActionListener(mibListener);
		_unloadmib = new JMenuItem(UNLOAD_MIB_ITEM, KeyEvent.VK_U);
		_unloadmib.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		_unloadmib.setEnabled(false);
		_unloadmib.addActionListener(mibListener);
		_unloadall = new JMenuItem(UNLOAD_ALL_ITEM, KeyEvent.VK_A);
		_unloadall.setEnabled(false);
		_unloadall.addActionListener(mibListener);
			
		_mibMenu.add(showItem);
		_mibMenu.add(showItemNewWin);
		_mibMenu.add(showItemFloating);
		_mibMenu.addSeparator();
		_mibMenu.add(_findmib);
		_mibMenu.add(_findnext);
		_mibMenu.add(_findprev);
		_mibMenu.addSeparator();

		_mibMenu.add(loadfile);
		_mibMenu.add(loaddir);
		_mibMenu.addSeparator();
		
		_mibMenu.add(_unloadmib);
		_mibMenu.add(_unloadall);
		
	}
	
	
	public JMenu getMibMenu() {
		return _mibMenu;
	}
	
    /**
     * Returns the application MIB file preferences.
     *
     * @return the list of MIB files to load
     */
    public ArrayList<String> getFilePrefs() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            String str = _prefs.get("file" + i, null);
            if (str != null) {
                list.add(str);
            }
        }
        return list;
    }

    /**
     * Removes all application MIB file preferences.
     */
    public void removeFilePrefs() {
        for (int i = 0; i < 1000; i++) {
            _prefs.remove("file" + i);
        }
    }
    
    /**
     * Adds a specified MIB file preference. The file may be either a built-in
     * MIB name or an absolute MIB file path.
     *
     * @param file           the MIB file or name to add
     */
    private void addFilePref(String file) {
        ArrayList<String> list = getFilePrefs();
        if (!list.contains(file)) {
            _prefs.put("file" + list.size(), file);
        }
    }

    /**
     * Removes a specified MIB file preference. The file may be either a
     * built-in MIB name or an absolute MIB file path.
     *
     * @param file           the MIB file or name to remove
     */
    private void removeFilePref(String file) {
        ArrayList<String> list = getFilePrefs();
        removeFilePrefs();
        list.remove(file);
        for (int i = 0; i < list.size(); i++) {
            _prefs.put("file" + i, list.get(i).toString());
        }
    }
    
    
    public void loadPrefMibs(List<String> mibs) {
    	File[] files = new File[mibs.size()];
    	for(int i = 0; i < mibs.size(); i++) {
    		files[i] = new File(mibs.get(i));
    	}
        Loader loader = new Loader(files);
        loader.start();
    }
	
	
	public void loadDefaultMib(String src) throws IOException, MibLoaderException {
        Mib mib = null;
		mib = _mibLoader.load(src);
	    MibTreeBuilder.getInstance().addMib(mib);
        addFilePref(src);
	    enableMibMenuItems(true);
	    refreshMibTree();
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
	            addFilePref(file.getAbsolutePath());
			} else {
				mib = _mibLoader.load(file.getName());
	            addFilePref(file.getName());
			}
	        MibTreeBuilder.getInstance().addMib(mib);
	        enableMibMenuItems(true);
            refreshMibTree();
		} catch (IOException e) {
			 message = "Can't find file " + file.getAbsolutePath() + ": " + e.getMessage();
		} catch (MibLoaderException e) {
            message = "Failed to load " + file.getAbsolutePath();
            ByteArrayOutputStream panelOut = new ByteArrayOutputStream();
            e.getLog().printTo(new PrintStream(panelOut));
            _mibBrowserPane.addErrorPanel(panelOut.toString());
            System.out.println(panelOut.toString());
		}
        if (message != null) {
            JOptionPane.showMessageDialog(null, message, "Can't load MIB", JOptionPane.ERROR_MESSAGE);
        }
	}
		
    public void unloadMib(String name) {
        Mib mib = _mibLoader.getMib(name);
        if (mib != null) {
            File file = mib.getFile();
            removeFilePref(file.getAbsolutePath());
            if (!file.exists()) {
                removeFilePref(mib.getName());
            }
            try {
                _mibLoader.unload(name);
                MibTreeBuilder.getInstance().unloadMib(name);
                refreshMibTree();
                if(_mibLoader.getAllMibs().length == 0) {
                	enableMibMenuItems(false);
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
		enableMibMenuItems(false);
    }
    
    public boolean containsMib(String mibFile) {
    	return findMibFile(mibFile) != null;
    }
    
    private MibNode findMibFile(String mibFile) {
    	MibNode root = (MibNode) _mibTree.getModel().getRoot();
    	Enumeration<MibNode> mibs = root.children();
    	while(mibs.hasMoreElements()) {
    		MibNode node = mibs.nextElement();
    		if(mibFile.equalsIgnoreCase(node.getUserObject().toString())) {
    			return node;
    		} 
    	}
    	return null;
    }
      
    public TreePath findMibNode(String mibFile, String mibNode, boolean select) {
    	MibNode node = findMibFile(mibFile);
    	TreePath prevResult = new TreePath(node.getPath());
    	TreeSearchIterator iter = new TreeSearchIterator(_mibTree);
    	TreePath path = findMibNode(iter, mibNode, prevResult);
		if(select) {
			showPath(path);
		}
		if(path == null) {
			JOptionPane.showMessageDialog(null, "Key not found");
		}
		return path;
    }
    
    private TreePath findMibNode(Iterator iter, String key, TreePath prevResult) {
    	TreePath path  = null;
		if(key != null && key.length() > 0) {
			((TreeSearchIterator) iter).init(prevResult);
			Pattern patt = Pattern.compile(key, Pattern.CASE_INSENSITIVE);;
			while(iter.hasNext()) {
				MibNode node = (MibNode) iter.next();
				String name = MibBrowserPanel.getMibName(node.getName());
				String oid = node.getOid().trim();
				if(key.equalsIgnoreCase(name) || key.equals(oid)) {
					path = new TreePath(node.getPath());
					break;
				} 
			}
		}
		return path;
    }
    
    private void showPath(TreePath path) {
    	if(path != null) {
			_mibTree.setSelectionPath(path);
			Rectangle bounds = _mibTree.getPathBounds(path);
			_mibTree.scrollRectToVisible(bounds);
    	}
    }
    
    public void refreshMibTree() {
        ((DefaultTreeModel) _mibTree.getModel()).reload();
        _mibTree.repaint();
    }
    
    
    private void enableMibMenuItems(boolean enable) {
    	_findnext.setEnabled(enable);
    	_findprev.setEnabled(enable);
	    _findmib.setEnabled(enable);
	    _unloadmib.setEnabled(enable);
	    _unloadall.setEnabled(enable);
    }
    
    
    private void findNext(TreeSearchIterator iter)  throws SearchResultNotFoundException {
    	find(iter, _currentSearchKey, _validSearchResult, isCurrentInludeText());
    }
    
    private void find(TreeSearchIterator iter, String key, boolean includeText)  throws SearchResultNotFoundException {
		find(iter, key, null, includeText);	
    }
    
    private void find(TreeSearchIterator iter, String key, TreePath prevResult, boolean includeText) {
		if(key != null && key.length() > 0) {
			setCurrentSearchKey(key);
			_currentSearchResult = findMibNode(iter, key, prevResult);
			if(_currentSearchResult == null && includeText) {
				if(key != null && key.length() > 0) {
					((TreeSearchIterator) iter).init(prevResult);
					while(iter.hasNext()) {
						MibNode node = (MibNode) iter.next();
						String descr = node.getDescription();
						SearchableText searchable = _mibBrowserPane.getSearchable(MibBrowserPanel.getMibName(node.getName()), descr);
						if(searchable == null) {
							searchable = new MibViewPanel(_mibBrowserPane, descr);
						}
						searchable.resetSearch(true);
						searchable.setCurrentSearchKey(key);
						searchable.setCaseSensitivity(false);
						try {
							searchable.findSearchResult();
							if(searchable.hasSearchResults()) {
								_currentSearchResult = new TreePath(node.getPath());
								_mibView = (MibViewPanel) searchable;
								break;
							}
						} catch(SearchResultNotFoundException ex) {
							//do nothing
						}
					}
				}
			}
	    	if(_currentSearchResult != null) {
	    		_validSearchResult = _currentSearchResult;
	    	}
			return;
		}
    }
    
	

	@Override
	public String getCurrentSearchKey() {
		return _currentSearchKey;
	}

	@Override
	public void setCurrentSearchKey(String searchKey) {
		_currentSearchKey = searchKey;

	}
	
	@Override
	public void resetSearch(boolean clearPrevious) {
		_nextSearchIterator = new TreeSearchIterator(_mibTree, true);
		_prevSearchIterator = new TreeSearchIterator(_mibTree, false);
		_currentSearchResult = null;
		_mibView = null;
	}

	@Override
	public void disableSearchControls(boolean isrun) {
		enableMibMenuItems(!isrun);
	}

	@Override
	public boolean hasSearchResults() {
		return _currentSearchResult != null;
	}

	@Override
	public void moveToNext()  throws SearchResultNotFoundException {
		findNext(_nextSearchIterator);
		if(!hasSearchResults()) {
			throw new SearchResultNotFoundException(true, _currentSearchKey);
		}
	}

	@Override
	public void moveToPrevious()  throws SearchResultNotFoundException {
		findNext(_prevSearchIterator);
		if(!hasSearchResults()) {
			throw new SearchResultNotFoundException(true, _currentSearchKey);
		}
	}

	@Override
	public boolean isCurrentInludeText() {
		return _prefs.getBoolean(CURRENT_INCLUDE_TEXT_OPTION, false);
	}

	@Override
	public void setCurrentIncludeText(boolean isIncludeText) {
		_prefs.putBoolean(CURRENT_INCLUDE_TEXT_OPTION, isIncludeText);
		
	}

	@Override
	public TreePath getCurrentSearchPosition() {
		return _currentSearchResult;
	}
	
	@Override
	public void findSearchResult() throws SearchResultNotFoundException {
		find(_nextSearchIterator, _currentSearchKey, isCurrentInludeText());
		if(!hasSearchResults()) {
			throw new SearchResultNotFoundException(false, _currentSearchKey);
		}
	}
	
	@Override
	public void showResult() {
		showPath(_currentSearchResult);
		if(_currentSearchResult != null && _mibView != null) {
			MibNode node = (MibNode) _currentSearchResult.getLastPathComponent();
			String name = MibBrowserPanel.getMibName(node.getName());
			int index = -1;
			JTabbedPane tabbedPane = _mibBrowserPane.getTabbedPane();
			if(tabbedPane != null) {
				index = tabbedPane.indexOfComponent(_mibView);
				if(index < 0) {
					_mibBrowserPane.addMibPanel(_mibView, name);
				}
				tabbedPane.setSelectedComponent(_mibView);
				_mibView.showResult();
			}
		}
	}
	
	public void openMibLoadDialog(boolean isDirectory) {
		final JFileChooser fc = new JFileChooser();
		fc.setFileHidingEnabled(false);
		int type = isDirectory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY;
		fc.setFileSelectionMode(type);
		int returnval = fc.showOpenDialog(null);
		if(returnval == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			File[] files = {};
			if(isDirectory) {
				files = file.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return (new File(dir, name)).isFile();
					}			
				});
			} else {
				files = new File[] {file};
			}
	        Loader loader = new Loader(files);
	        loader.start();
		}
	}
	
	public void getNextResult(boolean forward) {
		if(_currentSearchKey == null || _currentSearchKey.length() == 0) {
			_dg.setVisible(true);
		} else {
			Thread searcherThread = new TreeSearcher(MibTreePanel.this, forward, true);
			searcherThread.start();
		}
	}
	
	public void openSearchDialod() {
		_dg.setVisible(true);
	}
	
	

	private class MibListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent event) {
			JMenuItem item = (JMenuItem)event.getSource();
			String command = item.getText();
			if(command.equalsIgnoreCase(FIND_MIB_ITEM)) {
				openSearchDialod();
			} else if(command.equalsIgnoreCase(FIND_NEXT_MIB_ITEM)) {
				getNextResult(true);
			} else if(command.equalsIgnoreCase(FIND_PREV_MIB_ITEM)) {
				getNextResult(false);
			} else if(command.equalsIgnoreCase(LOAD_MIB_ITEM)) {
				openMibLoadDialog(false);
			} else if(command.equalsIgnoreCase(LOAD_DIR_ITEM)) {
				openMibLoadDialog(true);
			} else if(command.equalsIgnoreCase(UNLOAD_MIB_ITEM)) {
				TreePath path = _mibTree.getSelectionPath();
				if(path != null) {
					unloadMib(path.getLastPathComponent().toString());
				}
			} else if(command.equalsIgnoreCase(UNLOAD_ALL_ITEM)) {
				unloadAllMibs();
			}

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
