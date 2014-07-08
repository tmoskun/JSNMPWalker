package com.ezcode.jsnmpwalker.menu;

/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.panel.Searchable;
import com.ezcode.jsnmpwalker.panel.TabPanel;

public class TabPopupMenu extends JPopupMenu {
	final public static String CLOSE_ITEM = "Close";
	final public static String CLOSE_OTHERS_ITEM = "Close Others";
	final public static String CLOSE_ALL_ITEM = "Close All";
	final public static String RENAME_ITEM = "Rename";
	final public static String FIND_ITEM = "Find";
	final public static String FIND_NEXT_ITEM = "Find Next";
	final public static String FIND_PREVIOUS_ITEM = "Find Previous";
	
	
	private TabMenuListener _mibTabListener;
	private MibBrowserPanel _mibPanel;
	private TabPanel _tabPane;
	private Searchable _searchable;
	
	public TabPopupMenu(MibBrowserPanel mibPanel) {
		_mibPanel = mibPanel;
		JTabbedPane tabbedPane = mibPanel.getTabbedPane();
		int index = tabbedPane.getSelectedIndex();
		_tabPane = (TabPanel) tabbedPane.getTabComponentAt(index);
		_searchable = (Searchable) tabbedPane.getComponentAt(index);
		_mibTabListener = new TabMenuListener();
	}
	
	public TabPopupMenu(MibBrowserPanel mibPanel, TabPanel tabPanel) {
		_mibPanel = mibPanel;
		_tabPane = tabPanel;
		JTabbedPane tabbedPane = mibPanel.getTabbedPane();
		int index = tabbedPane.indexOfTabComponent(tabPanel);
		_searchable = (Searchable) tabbedPane.getComponentAt(index);
		_mibTabListener = new TabMenuListener();
	}
	
	public void buildMenu(int tabCount) {
		JMenuItem rename = new JMenuItem(RENAME_ITEM);
		rename.addMouseListener(_mibTabListener);
		this.add(rename);
		this.addSeparator();
		JMenuItem find = new JMenuItem(FIND_ITEM);
		find.addMouseListener(_mibTabListener);
		this.add(find);
		if(_searchable.hasSearchResults()) {
			JMenuItem findNext = new JMenuItem(FIND_NEXT_ITEM);
			findNext.addMouseListener(_mibTabListener);
			this.add(findNext);
			JMenuItem findPrev = new JMenuItem(FIND_PREVIOUS_ITEM);
			findPrev.addMouseListener(_mibTabListener);
			this.add(findPrev);
		}
		this.addSeparator();
		JMenuItem close = new JMenuItem(CLOSE_ITEM);
		close.addMouseListener(_mibTabListener);
		this.add(close);
		if(tabCount > 1) {
			JMenuItem closeOthers = new JMenuItem(CLOSE_OTHERS_ITEM);
			closeOthers.addMouseListener(_mibTabListener);
			this.add(closeOthers);
			JMenuItem closeAll = new JMenuItem(CLOSE_ALL_ITEM);
			closeAll.addMouseListener(_mibTabListener);
			this.add(closeAll);
		}
	}
	
	private class TabMenuListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent event) {
			TabPopupMenu.this.setVisible(false);
			JMenuItem item = (JMenuItem)event.getSource();
			String command = item.getText();
			if(command.equalsIgnoreCase(TabPopupMenu.CLOSE_ITEM)) {
				_mibPanel.removeMibPanel(_tabPane);
			} else if(command.equalsIgnoreCase(TabPopupMenu.CLOSE_OTHERS_ITEM)) {
				_mibPanel.removeMibPanelsExcept(_tabPane);
			} else if(command.equalsIgnoreCase(TabPopupMenu.CLOSE_ALL_ITEM)) {
				_mibPanel.removeAllMibPanels();
			} else if(command.equalsIgnoreCase(TabPopupMenu.RENAME_ITEM)) {
				_tabPane.editTitle();
			} else if(command.equalsIgnoreCase(TabPopupMenu.FIND_ITEM)) {
				_mibPanel.openSearchDialog(_searchable);
			} else if(command.equalsIgnoreCase(TabPopupMenu.FIND_NEXT_ITEM)) {
				_searchable.moveToNext();
			} else if(command.equalsIgnoreCase(TabPopupMenu.FIND_PREVIOUS_ITEM)) {
				_searchable.moveToPrevious();
			}
		}

	}
}
