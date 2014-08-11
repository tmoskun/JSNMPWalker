package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2012-2014 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.browser.MibNode;

import com.ezcode.jsnmpwalker.dialog.MibTextSearchDialog;
import com.ezcode.jsnmpwalker.menu.MibViewTabPopupMenu;
import com.ezcode.jsnmpwalker.search.SearchableText;
import com.ezcode.jsnmpwalker.window.MibBrowserWindow;

public class MibBrowserPanel extends JPanel {
	
	final public static String CURRENT_REGEX_OPTION = "regex";
	final public static String CURRENT_CASE_SENSITIVITY_OPTION = "case";
	
	final public static int CURRENT_WINDOW_MODE = 0;
	final public static int NEW_TAB_MODE = 1;
	final public static int FLOATING_WINDOW_MODE = 2;
	
	private final JTabbedPane _tp;
	private final MibTextSearchDialog _dg;
	private String _currentSearchKey;
	private Preferences _searchPrefs;

	public MibBrowserPanel(Frame frame) {
		super(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "MIB Browser")));
		_currentSearchKey = "";
		_searchPrefs = Preferences.userNodeForPackage(getClass());
		
		_tp = new JTabbedPane() {
			@Override
			public void setSelectedComponent(Component comp) {
				super.setSelectedComponent(comp);
		        int index = _tp.getSelectedIndex();
				TabPanel tabPanel = (TabPanel) _tp.getTabComponentAt(index);
				if(tabPanel != null) {
					JLabel tabLabel = tabPanel.getLabel();
					Rectangle tabRect = this.getBoundsAt(index);
					int width = tabRect.width + 40;
					tabRect.setSize(width, tabRect.height);
					this.scrollRectToVisible(tabRect);
					tabLabel.scrollRectToVisible(new Rectangle(0, 0, tabLabel.getWidth()+40, tabLabel.getHeight()));
				}
			}

			/*
			@Override
			public void setSelectedIndex(int index) {
				super.setSelectedIndex(index);
				System.out.println("sel index");
				TabPanel tabPanel = (TabPanel) _tp.getTabComponentAt(index);
				if(tabPanel != null) {
					JLabel tabLabel = tabPanel.getLabel();
					Rectangle tabRect = this.getBoundsAt(index);
					int width = tabRect.width + 40;
					tabRect.setSize(width, tabRect.height);
					this.scrollRectToVisible(tabRect);
					tabLabel.scrollRectToVisible(new Rectangle(0, 0, tabLabel.getWidth()+40, tabLabel.getHeight()));
				}
			}
			*/
		};
		//_tp.setUI(new MetalTabbedPaneUI());	
		_tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		_tp.setVisible(false);
		add(_tp, BorderLayout.CENTER);
		
		_dg = new MibTextSearchDialog(frame, _currentSearchKey, _searchPrefs.getBoolean(CURRENT_REGEX_OPTION, false), _searchPrefs.getBoolean(CURRENT_CASE_SENSITIVITY_OPTION, false));
	}
	
		
	public JTabbedPane getTabbedPane() {
		return _tp;
	}

	public void setMibDescription(String descr, String name, MibNode node) {
		setMibDescription(descr, name, CURRENT_WINDOW_MODE);
	}
	
	public void setMibDescription(String descr, String name, int mode) {
		switch(mode) {
			case CURRENT_WINDOW_MODE:
				if(_tp.getTabCount() > 0) {
					int index = _tp.getSelectedIndex();
					MibViewPanel sel = (MibViewPanel) _tp.getComponentAt(index);
					TabPanel tabPanel = (TabPanel)_tp.getTabComponentAt(index);
					tabPanel.setTitle(name);
					sel.setMibDescription(descr);
				} else  {
					addMibPanel(descr, name);
				}
				break;
			case FLOATING_WINDOW_MODE:
				MibBrowserWindow mibFrame = new MibBrowserWindow(this, name, descr);
				mibFrame.setVisible(true);
				break;
			case NEW_TAB_MODE: 
			default: 
				addMibPanel(descr, name);
				break;
		}
	}
	
	
	public void addMibPanel(MibViewPanel panel, TabPanel tabPanel) {
		panel.removeFrame();
		String title = tabPanel.getTitle();
		int nextIndex = _tp.getTabCount();
		int index = nextIndex - 1;
		TabPanel foundTabPanel = null;
		while(index >= 0) {
			MibViewPanel mibView = (MibViewPanel) _tp.getComponentAt(index);
			if(mibView.getSearchText().equals(panel.getSearchText())) {
				foundTabPanel = (TabPanel) _tp.getTabComponentAt(index);
				break;
			}
			index--;
		}
		if(foundTabPanel != null) {
			title = foundTabPanel.getTitle();
			String[] titles = title.split("\\s*[\\[\\]]");
			int num = 1;
			if(titles.length > 1 && titles[1] != null) {
				try {
					num = Integer.parseInt(titles[1].trim());
				} catch (Exception e) {
					//do nothing
				}
			}
			title = titles[0] + " [" + (num + 1) + "]";
		}
		tabPanel.setTitle(title);
		_tp.addTab("", panel);
		
		_tp.setTabComponentAt(nextIndex, tabPanel);
		_tp.setSelectedComponent(panel);
		_tp.setVisible(true);
		_tp.repaint();
	}
	
	public void addMibPanel(MibViewPanel panel, String name) {
		addMibPanel(panel, new TabPanel(this, name));
	}
	
	public void addMibPanel(String descr, String name) {
		if(descr != null && descr.length() > 0) {
			addMibPanel(new MibViewPanel(this, descr), name);
		}
		//_tp.repaint();
	}
	
	public void addErrorPanel(String errorMessage) {
		addMibPanel(new MibViewPanel(this, errorMessage), "Error");
	}
	
	public static String getMibName(String name) {
		return name.replaceAll("\\(\\d+\\)\\s*$", "").trim();
	}
	
	public void removeMibPanel(int index) {
		if(index >= 0) {
			_tp.remove(index);
			_tp.repaint();
			if(_tp.getTabCount() == 0) {
				_tp.setVisible(false);
			}
		}
	}
	
	public void removeMibPanel(Component comp) {
        int i = _tp.indexOfTabComponent(comp);
        removeMibPanel(i);
	}
	
	public void removeMibPanelsExcept(Component comp) {
		int i = _tp.indexOfTabComponent(comp);
		int count = _tp.getTabCount() ; 
		for (int j = count-1 ; j >= 0 ; j--) { 
			if (j != i) {
				_tp.remove(j) ;
			}
		}
	}
	
	public void removeAllMibPanels() {
		_tp.removeAll();
		_tp.setVisible(false);
	}
	
	public void moveToFloating(TabPanel tabPanel) {
		int i = _tp.indexOfTabComponent(tabPanel);
		MibViewPanel mibView = (MibViewPanel) _tp.getComponentAt(i);
		MibBrowserWindow mibFrame = new MibBrowserWindow(this, mibView, tabPanel.getTitle());
		mibFrame.setVisible(true);
		//removeMibPanel(i);
	}
	
	public void moveToTab(MibBrowserWindow mibFrame) {
		addMibPanel(mibFrame.getMibViewPanel(), mibFrame.getTitle());
		mibFrame.setVisible(false);
		mibFrame.dispose();
	}
	
	public void openSearchDialog(SearchableText searchable) {
		_dg.setSearchable(searchable);
		_dg.setVisible(true);
	}
	
	public String getCurrentSearchKey() {
		return _currentSearchKey;
	}


	public void setCurrentSearchKey(String currentSearchKey) {
		_currentSearchKey = currentSearchKey;
	}


	public Preferences getSearchPrefs() {
		return _searchPrefs;
	}


	public void setSearchPrefs(Preferences searchPrefs) {
		_searchPrefs = searchPrefs;
	}
	
	public SearchableText getSearchable(String name, String descr) {
		for(int i = 0; i < _tp.getTabCount(); i++) {
			TabPanel tabPane = (TabPanel) _tp.getTabComponentAt(i);
			if(tabPane.getTitle().equals(name)) {
				MibViewPanel mibView = (MibViewPanel) _tp.getComponentAt(i);
				if(mibView != null && mibView.getSearchText().equals(descr)) {
					return mibView;
				}
			}
		}
		return null;
	}
	
	
}
