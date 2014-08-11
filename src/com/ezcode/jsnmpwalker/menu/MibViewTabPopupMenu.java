package com.ezcode.jsnmpwalker.menu;

/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.panel.TabPanel;
import com.ezcode.jsnmpwalker.search.SearchResultNotFoundException;
import com.ezcode.jsnmpwalker.search.SearchableText;

public class MibViewTabPopupMenu extends AbstractMibViewPopupMenu {
	
	private JTabbedPane _tabbedPane;
	private TabPanel _tabPane;
	private MouseListener _tabMenuListener;
	
	
	public MibViewTabPopupMenu(MibBrowserPanel mibPanel) {
		this(mibPanel, mibPanel.getTabbedPane());
	}
	
	public MibViewTabPopupMenu(MibBrowserPanel mibPanel, JTabbedPane tabbedPane) {
		this(mibPanel, tabbedPane, tabbedPane.getSelectedIndex());
	}
	
	public MibViewTabPopupMenu(MibBrowserPanel mibPanel, TabPanel tabPanel) {
		this(mibPanel, mibPanel.getTabbedPane(), tabPanel);
	}
	
	
	public MibViewTabPopupMenu(MibBrowserPanel mibPanel, JTabbedPane tabbedPane, TabPanel tabPanel) {
		this(mibPanel, tabbedPane, tabbedPane.indexOfTabComponent(tabPanel));
	}
	
	public MibViewTabPopupMenu(MibBrowserPanel mibPanel, JTabbedPane tabbedPane, int index) {
		super(mibPanel, (SearchableText) tabbedPane.getComponentAt(index));
		//super(mibPanel, (SearchableText) tabbedPane.getComponentAt(index), new TabMenuListener());
		_tabbedPane = tabbedPane;
		_tabPane = (TabPanel) tabbedPane.getTabComponentAt(index);
		_tabMenuListener = new TabMenuListener();
	}
	
	public void buildMenu() {
		this.removeAll();
		JMenuItem rename = new JMenuItem(RENAME_ITEM);
		rename.addMouseListener(_tabMenuListener);
		this.add(rename);
		JMenuItem showFloating = new JMenuItem(SHOW_FLOATING_ITEM);
		showFloating.addMouseListener(_tabMenuListener);
		this.add(showFloating);
		this.addSeparator();
		JMenuItem find = new JMenuItem(FIND_ITEM);
		find.addMouseListener(_tabMenuListener);
		this.add(find);
		if(_searchable.hasSearchResults()) {
			JMenuItem findNext = new JMenuItem(FIND_NEXT_ITEM);
			findNext.addMouseListener(_tabMenuListener);
			this.add(findNext);
			JMenuItem findPrev = new JMenuItem(FIND_PREVIOUS_ITEM);
			findPrev.addMouseListener(_tabMenuListener);
			this.add(findPrev);
		}
		this.addSeparator();
		JMenuItem close = new JMenuItem(CLOSE_ITEM);
		close.addMouseListener(_tabMenuListener);
		this.add(close);
		if(_tabbedPane.getTabCount() > 1) {
			JMenuItem closeOthers = new JMenuItem(CLOSE_OTHERS_ITEM);
			closeOthers.addMouseListener(_tabMenuListener);
			this.add(closeOthers);
			JMenuItem closeAll = new JMenuItem(CLOSE_ALL_ITEM);
			closeAll.addMouseListener(_tabMenuListener);
			this.add(closeAll);
		}
	}
	
	private class TabMenuListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent event) {
			MibViewTabPopupMenu.this.setVisible(false);
			JMenuItem item = (JMenuItem)event.getSource();
			String command = item.getText();
			if(command.equalsIgnoreCase(CLOSE_ITEM)) {
				_mibPanel.removeMibPanel(_tabPane);
			} else if(command.equalsIgnoreCase(CLOSE_OTHERS_ITEM)) {
				_mibPanel.removeMibPanelsExcept(_tabPane);
			} else if(command.equalsIgnoreCase(CLOSE_ALL_ITEM)) {
				_mibPanel.removeAllMibPanels();
			} else if(command.equalsIgnoreCase(RENAME_ITEM)) {
				_tabPane.editTitle();
			} else if(command.equalsIgnoreCase(SHOW_FLOATING_ITEM)) {
				_mibPanel.moveToFloating(_tabPane);
			} else if(command.equalsIgnoreCase(FIND_ITEM)) {
				_mibPanel.openSearchDialog(_searchable);
			} else if(command.equalsIgnoreCase(FIND_NEXT_ITEM)) {
				try {
					_searchable.moveToNext();
				} catch (SearchResultNotFoundException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			} else if(command.equalsIgnoreCase(FIND_PREVIOUS_ITEM)) {
				try {
					_searchable.moveToPrevious();
				} catch (SearchResultNotFoundException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		}

	}
}
