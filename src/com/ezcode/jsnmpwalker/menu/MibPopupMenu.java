package com.ezcode.jsnmpwalker.menu;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import net.percederberg.mibble.browser.MibNode;

import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.panel.MibTreePanel;
import com.ezcode.jsnmpwalker.search.TreeSearcher;

public class MibPopupMenu extends JPopupMenu {
	
	final public static String SHOW_ITEM = "Show in Current Window";
	final public static String SHOW_NEW_WIN_ITEM = "Show in New Tab";
	final public static String SHOW_FLOATING_ITEM = "Show in Floating Window";
	
	final private static MouseListener MENU_LISTENER = new MouseAdapter() {
		private MibTreePanel _mibTreePane = MibTreePanel.getInstance();
		
		public void mousePressed(MouseEvent event) {
			JMenuItem item = (JMenuItem)event.getSource();
			String command = item.getText();
			JPopupMenu menu = (JPopupMenu) item.getParent();
			if(_mibTreePane != null && menu instanceof MibPopupMenu) {
				Point position = ((MibPopupMenu) menu).getPositionRelativeToInvoker();
				TreePath path = _mibTreePane.getTree().getPathForLocation(position.x, position.y);
				if (path == null)
					return;	
				Object obj = path.getLastPathComponent();
				if(obj instanceof MibNode) {
					MibNode node = (MibNode) obj;
					if(command.equalsIgnoreCase(MibPopupMenu.SHOW_ITEM)) {
						_mibTreePane.showMibDescription(node, MibBrowserPanel.CURRENT_WINDOW_MODE);
					} else if(command.equalsIgnoreCase(MibPopupMenu.SHOW_NEW_WIN_ITEM)) {
						_mibTreePane.showMibDescription(node, MibBrowserPanel.NEW_TAB_MODE);
					} else if(command.equalsIgnoreCase(MibPopupMenu.SHOW_FLOATING_ITEM)) {
						_mibTreePane.showMibDescription(node, MibBrowserPanel.FLOATING_WINDOW_MODE);
					} else if(command.equalsIgnoreCase(MibTreePanel.FIND_MIB_ITEM)) {
						_mibTreePane.openSearchDialod();
					} else if(command.equalsIgnoreCase(MibTreePanel.FIND_NEXT_MIB_ITEM)) {
						_mibTreePane.getNextResult(true);
					} else if(command.equalsIgnoreCase(MibTreePanel.FIND_PREV_MIB_ITEM)) {
						_mibTreePane.getNextResult(false);
					} 
				}
			}
		}
	};
	
	private Point _position;
	
	public MibPopupMenu(Point position) {
		_position = position;
	}
	
	public void buildMenu() {
		JMenuItem showMib = new JMenuItem(SHOW_ITEM);
		showMib.addMouseListener(MENU_LISTENER);
		this.add(showMib);
		JMenuItem showMibNewWindow = new JMenuItem(SHOW_NEW_WIN_ITEM);
		showMibNewWindow.addMouseListener(MENU_LISTENER);
		this.add(showMibNewWindow);
		JMenuItem showMibFloating = new JMenuItem(SHOW_FLOATING_ITEM);
		showMibFloating.addMouseListener(MENU_LISTENER);
		add(showMibFloating);
		this.addSeparator();
		JMenuItem findMib = new JMenuItem(MibTreePanel.FIND_MIB_ITEM);
		findMib.addMouseListener(MENU_LISTENER);
		this.add(findMib);
		JMenuItem findNextMib = new JMenuItem(MibTreePanel.FIND_NEXT_MIB_ITEM);
		findNextMib.addMouseListener(MENU_LISTENER);
		this.add(findNextMib);
		JMenuItem findPrevMib = new JMenuItem(MibTreePanel.FIND_PREV_MIB_ITEM);
		findPrevMib.addMouseListener(MENU_LISTENER);
		this.add(findPrevMib);
	}
	
	public Point getPositionRelativeToInvoker() {
		return _position;
	}
	
/*
	private class MibMenuListener extends MouseAdapter {
		
		
		public void mousePressed(MouseEvent event) {
			JMenuItem item = (JMenuItem)event.getSource();
			String command = item.getText();
			JPopupMenu menu = (JPopupMenu) item.getParent();
			if(menu instanceof MibPopupMenu) {
				Point position = ((MibPopupMenu) menu).getPositionRelativeToInvoker();
				TreePath path = _mibTreePane.getTree().getPathForLocation(position.x, position.y);
				if (path == null)
					return;	
				Object obj = path.getLastPathComponent();
				if(obj instanceof MibNode) {
					MibNode node = (MibNode) obj;
					if(command.equalsIgnoreCase(MibPopupMenu.SHOW_ITEM)) {
						_mibTreePane.showMibDescription(node);
					} else if(command.equalsIgnoreCase(MibPopupMenu.SHOW_NEW_WIN_ITEM)) {
						_mibTreePane.showMibDescription(node, true);
					}
				}
			}
		}
	}
	*/
	
}
