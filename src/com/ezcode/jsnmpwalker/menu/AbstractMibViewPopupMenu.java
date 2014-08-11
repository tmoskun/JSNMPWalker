package com.ezcode.jsnmpwalker.menu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;

import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.search.SearchableText;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public abstract class AbstractMibViewPopupMenu extends JPopupMenu {
	final public static String CLOSE_ITEM = "Close";
	final public static String CLOSE_OTHERS_ITEM = "Close Others";
	final public static String CLOSE_ALL_ITEM = "Close All";
	final public static String RENAME_ITEM = "Rename";
	final public static String SHOW_FLOATING_ITEM = "Show in Floating Window";
	final public static String SHOW_TAB_ITEM = "Show in Tab";
	final public static String FIND_ITEM = "Find";
	final public static String FIND_NEXT_ITEM = "Find Next";
	final public static String FIND_PREVIOUS_ITEM = "Find Previous";
	
	protected MibBrowserPanel _mibPanel;
	protected SearchableText _searchable;
	
	public AbstractMibViewPopupMenu(MibBrowserPanel mibPanel, SearchableText searchable) {
		_mibPanel = mibPanel;
		_searchable = searchable;
	}
	
	abstract public void buildMenu();
	
}
