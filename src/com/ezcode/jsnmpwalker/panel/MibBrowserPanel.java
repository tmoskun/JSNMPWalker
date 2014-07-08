package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2012-2014 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;

import net.percederberg.mibble.browser.MibNode;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.dialog.SearchDialog;
import com.ezcode.jsnmpwalker.menu.TabPopupMenu;

public class MibBrowserPanel extends JPanel {
	
	private static final Color HILIT_COLOR = Color.YELLOW;
	private static final int FIELD_WIDTH = Math.min(SNMPSessionFrame.WIDTH/6, 300);
	final private static String CURRENT_REGEX_OPTION = "regex";
	final private static String CURRENT_CASE_SENSITIVITY_OPTION = "case";
	
	private final JTabbedPane _tp;
	private final SearchDialog _dg;
	private String _currentSearchKey;
	private Map<String, Boolean> _currentSearchOptions;

	public MibBrowserPanel(Frame frame) {
		super(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "MIB Browser")));
		_currentSearchKey = "";
		_currentSearchOptions = new Hashtable<String, Boolean>();
		_currentSearchOptions.put(CURRENT_REGEX_OPTION, Boolean.FALSE);
		_currentSearchOptions.put(CURRENT_CASE_SENSITIVITY_OPTION, Boolean.FALSE);
		
		_tp = new JTabbedPane();
		//_tp.setUI(new MetalTabbedPaneUI());	
		_tp.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		_tp.setVisible(false);
		add(_tp, BorderLayout.CENTER);
		
		_dg = new SearchDialog(frame, _currentSearchKey, _currentSearchOptions.get(CURRENT_REGEX_OPTION), _currentSearchOptions.get(CURRENT_CASE_SENSITIVITY_OPTION));
	}
	
		
	public JTabbedPane getTabbedPane() {
		return _tp;
	}

	public void setMibDescription(MibNode node) {
		setMibDescription(node, false);
	}
	
	public void setMibDescription(MibNode node, boolean newTab) {
		if(newTab) {
			addMibPanel(node);
		} else {
			if(_tp.getTabCount() > 0) {
				int index = _tp.getSelectedIndex();
				MIBViewPanel sel = (MIBViewPanel) _tp.getComponentAt(index);
				TabPanel tabPanel = (TabPanel)_tp.getTabComponentAt(index);
				tabPanel.setTitle(node.getName());
				sel.setMibDescription(node.getDescription());
			} else  {
				addMibPanel(node);
			}
		}
	}
	
	public void addMibPanel(MibNode node) {
		String descr = node.getDescription();
		if(descr != null && descr.length() > 0) {
			_tp.addTab("", new MIBViewPanel(node.getDescription()));
			int index = _tp.getTabCount()-1;
			_tp.setTabComponentAt(index, new TabPanel(this, node.getName()));
			_tp.setSelectedIndex(index);
			_tp.setVisible(true);
		}
		//_tp.repaint();
	}
	
	public void removeMibPanel(int index) {
		if(index >= 0) {
			_tp.remove(index);
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
	
	public void openSearchDialog(Searchable searchable) {
		_dg.setSearchable(searchable);
		_dg.setVisible(true);
	}
	
	private class MIBViewPanel extends JScrollPane implements Searchable {
		
		private AttributeSet _docAttributes;
		private JTextWrapPane _mibArea;
		private TreeMap<Integer, Integer> _searchPositions;
		private int _currentSearchPosition;
		private final Highlighter _hilit;
		private final Highlighter.HighlightPainter _painter;
		
		
		public MIBViewPanel() {
			super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			_searchPositions = new TreeMap<Integer, Integer>();
			_currentSearchPosition = -1;
			_hilit = new DefaultHighlighter();
			_painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
			init();
		}
		
		public MIBViewPanel(String descr) {
			this();
			setMibDescription(descr);
		}
		
		private void init() {
			StyleContext sc = StyleContext.getDefaultStyleContext();
			_docAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
			StyledDocument logDoc = new DefaultStyledDocument();
			_mibArea = new JTextWrapPane(logDoc, false);
			_mibArea.setEditable(false);
			_mibArea.setHighlighter(_hilit);
			_mibArea.setSelectedTextColor(logDoc.getForeground(_docAttributes));
			_mibArea.setText("");
			_mibArea.setPreferredSize(new Dimension(SNMPSessionFrame.WIDTH/2, SNMPSessionFrame.HEIGHT/4));
			getViewport().add(_mibArea, BorderLayout.CENTER);
			
			_mibArea.addMouseListener(new MouseAdapter() {
				private void TabPopupEvent(MouseEvent event) {
					int x = event.getX();
					int y = event.getY();
					
		            Component comp = (Component) event.getSource();
		            
					TabPopupMenu popup = new TabPopupMenu(MibBrowserPanel.this);
					popup.buildMenu(_tp.getTabCount());
					popup.show(comp, x, y);
				}
				
				
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) {
						TabPopupEvent(e);
					} 
				}
				
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger()) {
						TabPopupEvent(e);
					} 
				}
			});
		}
		
		public void setMibDescription(String descr) {
			_mibArea.setText(descr);
		}

		@Override
		public void resetSearch(boolean clearPrevious) {
			if(clearPrevious) {
				_hilit.removeAllHighlights();
			}
			_mibArea.setCaretPosition(0);
		}

		@Override
		public void enableSearchControls(boolean isrun) {
			//none
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
		public boolean isRegex() {
			return _currentSearchOptions.get(CURRENT_REGEX_OPTION);
		}

		@Override
		public void setRegex(boolean isRegex) {
			_currentSearchOptions.put(CURRENT_REGEX_OPTION, isRegex);
			
		}

		@Override
		public boolean isCaseSensitive() {
			return _currentSearchOptions.get(CURRENT_CASE_SENSITIVITY_OPTION);
		}

		@Override
		public void setCaseSensitivity(boolean isCaseSensitive) {
			_currentSearchOptions.put(CURRENT_CASE_SENSITIVITY_OPTION, isCaseSensitive);
		}
		

		@Override
		public String getSearchText() {
			Document doc = _mibArea.getDocument();
			String str = "";
			try {
				str = doc.getText(0, doc.getLength());
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
			return str;
		}
		
		@Override
		public TreeMap<Integer, Integer> getSearchPositions() {
			return _searchPositions;
		}

		@Override
		public int getCurrentSearchPosition() {
			return _currentSearchPosition;
		}
		
		@Override
		public boolean hasSearchResults() {
			return _searchPositions != null && !_searchPositions.isEmpty();
		}
		

		@Override
		public void showResults(TreeMap<Integer, Integer> searchPositions, int currentSearchPosition) {
			_hilit.removeAllHighlights();
			_searchPositions = searchPositions;
			_currentSearchPosition = currentSearchPosition;
			//highlight the results
			Set<Integer> positions = searchPositions.keySet();
			for(int pos: positions) {
				try {
					_hilit.addHighlight(pos, searchPositions.get(pos), _painter);
				} catch (BadLocationException e1) {
						e1.printStackTrace();
				}
			}
			//move to the position
			moveToCurrentPosition();
		}
		
		private void moveToCurrentPosition() {
			int end = _currentSearchPosition + _currentSearchKey.length();
			int x = end - 1;
			int row = _currentSearchPosition;
			try {
				if(!_mibArea.getDocument().getText(_currentSearchPosition + _currentSearchKey.length(), 1).matches("[\n\r]")) {
					x = Utilities.getNextWord(_mibArea, _currentSearchPosition);
				}
				row = Utilities.getRowStart(_mibArea, _currentSearchPosition);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			_mibArea.setCaretPosition(x);
			_mibArea.select(row, x);
			_mibArea.requestFocusInWindow();
		}

		@Override
		public void moveToNext() {
			try {
				_currentSearchPosition = _searchPositions.higherKey(_currentSearchPosition);
				moveToCurrentPosition();
			} catch(Exception e) {
				JOptionPane.showMessageDialog(this, "No result found");
			}
			
		}

		@Override
		public void moveToPrevious() {
			//wrap around if it's finished
			try {
				_currentSearchPosition = _searchPositions.lowerKey(_currentSearchPosition);
				moveToCurrentPosition();
			} catch(Exception e) {
				JOptionPane.showMessageDialog(this, "No result found");
			}
			
		}
		
	}

}
