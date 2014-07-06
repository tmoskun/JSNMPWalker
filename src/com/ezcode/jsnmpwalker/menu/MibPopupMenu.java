package com.ezcode.jsnmpwalker.menu;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import net.percederberg.mibble.browser.MibNode;

import com.ezcode.jsnmpwalker.panel.MibTreePanel;

public class MibPopupMenu extends JPopupMenu {
	
	final public static String SHOW_ITEM = "Show in Current Window";
	final public static String SHOW_NEW_WIN_ITEM = "Show in New Window";
	
	private MibMenuListener _mibMenuListener;
	private MibTreePanel _mibTreePane;
	private Point _position;
	
	public MibPopupMenu(MibTreePanel mibTreePane, Point position) {
		_mibTreePane = mibTreePane;
		_position = position;
		_mibMenuListener = new MibMenuListener();
	}
	
	public void buildMenu() {
		JMenuItem showMib = new JMenuItem(SHOW_ITEM);
		showMib.addMouseListener(_mibMenuListener);
		this.add(showMib);
		JMenuItem showMibNewWindow = new JMenuItem(SHOW_NEW_WIN_ITEM);
		showMibNewWindow.addMouseListener(_mibMenuListener);
		this.add(showMibNewWindow);
	}
	
	public Point getPositionRelativeToInvoker() {
		return _position;
	}
	
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
	
}
