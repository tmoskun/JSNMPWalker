package com.ezcode.jsnmpwalker.menu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.search.SearchResultNotFoundException;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.window.MibBrowserWindow;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public class MibViewWindowPopupMenu extends AbstractMibViewPopupMenu {
	private MouseListener _menuListener;
	private JFrame _mibWindow;
	
	public MibViewWindowPopupMenu(JFrame mibWindow, MibBrowserPanel mibPanel) {
		this(mibWindow, mibPanel, ((MibBrowserWindow) mibWindow).getMibViewPanel());
	}
	
	public MibViewWindowPopupMenu(JFrame mibWindow, MibBrowserPanel mibPanel, SearchableText searchable) {
		super(mibPanel, searchable);
		_mibWindow = mibWindow;
		_menuListener = new WindowMenuListener();
	}

	@Override
	public void buildMenu() {
		this.removeAll();
		JMenuItem rename = new JMenuItem(RENAME_ITEM);
		rename.addMouseListener(_menuListener);
		this.add(rename);
		JMenuItem showTab = new JMenuItem(SHOW_TAB_ITEM);
		showTab.addMouseListener(_menuListener);
		this.add(showTab);
		this.addSeparator();
		JMenuItem find = new JMenuItem(FIND_ITEM);
		find.addMouseListener(_menuListener);
		this.add(find);
		if(_searchable.hasSearchResults()) {
			JMenuItem findNext = new JMenuItem(FIND_NEXT_ITEM);
			findNext.addMouseListener(_menuListener);
			this.add(findNext);
			JMenuItem findPrev = new JMenuItem(FIND_PREVIOUS_ITEM);
			findPrev.addMouseListener(_menuListener);
			this.add(findPrev);
		}
		this.addSeparator();
		JMenuItem close = new JMenuItem(CLOSE_ITEM);
		close.addMouseListener(_menuListener);
		this.add(close);
	}
	
	private class WindowMenuListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent event) {
			MibViewWindowPopupMenu.this.setVisible(false);
			JMenuItem item = (JMenuItem)event.getSource();
			String command = item.getText();
			if(command.equalsIgnoreCase(CLOSE_ITEM)) {
				_mibWindow.setVisible(false);
				_mibWindow.dispose();
			} else if(command.equalsIgnoreCase(RENAME_ITEM)) {
				((MibBrowserWindow) _mibWindow).editTitle();
			} else if(command.equalsIgnoreCase(SHOW_TAB_ITEM)) {
				_mibPanel.moveToTab((MibBrowserWindow) _mibWindow);
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
