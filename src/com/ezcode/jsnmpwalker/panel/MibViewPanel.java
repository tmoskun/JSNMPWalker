package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */
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

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.menu.AbstractMibViewPopupMenu;
import com.ezcode.jsnmpwalker.menu.MibViewTabPopupMenu;
import com.ezcode.jsnmpwalker.menu.MibViewWindowPopupMenu;
import com.ezcode.jsnmpwalker.search.SearchResultNotFoundException;
import com.ezcode.jsnmpwalker.search.SearchableText;


public class MibViewPanel extends JScrollPane implements SearchableText {

	static final Color HILIT_COLOR = Color.YELLOW;
	
	private MibBrowserPanel _mibBrowser;
	private JFrame _mibWindow;
	
	private AttributeSet _docAttributes;
	private JTextWrapPane _mibArea;
	private TreeMap<Integer, Integer> _searchPositions;
	private int _currentSearchPosition;
	private final Highlighter _hilit;
	private final Highlighter.HighlightPainter _painter;
	
	public MibViewPanel(MibBrowserPanel mibBrowser, String descr) {
		this(null, mibBrowser, SNMPSessionFrame.WIDTH/2, SNMPSessionFrame.HEIGHT/4, descr);
	}
	
	public MibViewPanel(MibBrowserPanel mibBrowser, int width, int height, String descr) {
		this(null, mibBrowser, width, height, descr);
	}
	
	public MibViewPanel(JFrame mibWindow, MibBrowserPanel mibBrowser, int width, int height, String descr) {
		this(mibWindow, mibBrowser, width, height);
		setMibDescription(descr);
	}
	
	public MibViewPanel(JFrame mibWindow, MibBrowserPanel mibBrowser, int width, int height) {
		super(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		_mibWindow = mibWindow;
		_mibBrowser = mibBrowser;
		_searchPositions = new TreeMap<Integer, Integer>();
		_currentSearchPosition = -1;
		_hilit = new DefaultHighlighter();
		_painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
		init(width, height);
	}
	
	
	private void init(int width, int height) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		_docAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
		StyledDocument logDoc = new DefaultStyledDocument();
		_mibArea = new JTextWrapPane(logDoc, false);
		_mibArea.setEditable(false);
		_mibArea.setHighlighter(_hilit);
		_mibArea.setSelectedTextColor(logDoc.getForeground(_docAttributes));
		_mibArea.setText("");
		_mibArea.setPreferredSize(new Dimension(width, height));
		getViewport().add(_mibArea, BorderLayout.CENTER);
		
		_mibArea.addMouseListener(new MouseAdapter() {
			private void MibViewPopupEvent(MouseEvent event) {
				int x = event.getX();
				int y = event.getY();
				
	            Component comp = (Component) event.getSource();
	            
	            AbstractMibViewPopupMenu popup = null;
	            if(_mibWindow == null) {
	            	popup = new MibViewTabPopupMenu(_mibBrowser);
	            } else {
	            	popup = new MibViewWindowPopupMenu(_mibWindow, _mibBrowser);
	            }
				popup.buildMenu();
				popup.show(comp, x, y);
			}
			
			
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					MibViewPopupEvent(e);
				} 
			}
			
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					MibViewPopupEvent(e);
				} 
			}
		});
	}
	
	public void setFrame(JFrame mibWindow) {
		_mibWindow  = mibWindow;
	}
	
	public void removeFrame() {
		_mibWindow = null;
	}
	
	public void setMibDescription(String descr) {
		_mibArea.setText(descr);
	}
	
/*
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof SearchableText) {
			SearchableText searchable = (SearchableText) obj;
			return searchable.getSearchText().equals(getSearchText()) && searchable.getSearchPositions().equals(getSearchPositions());
		}
		return false;
	}
*/

	@Override
	public void resetSearch(boolean clearPrevious) {
		if(clearPrevious) {
			_hilit.removeAllHighlights();
		}
		_mibArea.setCaretPosition(0);
	}

	@Override
	public void disableSearchControls(boolean isrun) {
		//none
	}

	@Override
	public String getCurrentSearchKey() {
		return _mibBrowser.getCurrentSearchKey();
	}

	@Override
	public void setCurrentSearchKey(String searchKey) {
		_mibBrowser.setCurrentSearchKey(searchKey);
		
	}
	
	@Override
	public boolean isRegex() {
		return _mibBrowser.getSearchPrefs().getBoolean(MibBrowserPanel.CURRENT_REGEX_OPTION, false);
	}

	@Override
	public void setRegex(boolean isRegex) {
		_mibBrowser.getSearchPrefs().putBoolean(MibBrowserPanel.CURRENT_REGEX_OPTION, isRegex);
		
	}

	@Override
	public boolean isCaseSensitive() {
		return _mibBrowser.getSearchPrefs().getBoolean(MibBrowserPanel.CURRENT_CASE_SENSITIVITY_OPTION, false);
	}

	@Override
	public void setCaseSensitivity(boolean isCaseSensitive) {
		_mibBrowser.getSearchPrefs().putBoolean(MibBrowserPanel.CURRENT_CASE_SENSITIVITY_OPTION, isCaseSensitive);
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
		return _searchPositions != null && !_searchPositions.isEmpty() && _currentSearchPosition >= _searchPositions.firstKey() && _currentSearchPosition <= _searchPositions.lastKey();
	}
	
	

	@Override
	public void moveToNext() throws SearchResultNotFoundException {
		try {
			_currentSearchPosition = _searchPositions.higherKey(_currentSearchPosition);
			moveToCurrentPosition();
		} catch(Exception e) {
			throw new SearchResultNotFoundException(true, _mibBrowser.getCurrentSearchKey());
			//_currentSearchPosition = _searchPositions.isEmpty() ? -1 : _searchPositions.firstKey();
			//_currentSearchPosition = _searchPositions.isEmpty() ? -1 :_searchPositions.lastKey() + 1;
			//JOptionPane.showMessageDialog(this, "No more results found for '" + _mibBrowser.getCurrentSearchKey() + "'");
		}
		
	}

	@Override
	public void moveToPrevious()  throws SearchResultNotFoundException {
		//wrap around if it's finished
		try {
			_currentSearchPosition = _searchPositions.lowerKey(_currentSearchPosition);
			moveToCurrentPosition();
		} catch(Exception e) {
			throw new SearchResultNotFoundException(true, _mibBrowser.getCurrentSearchKey());
			//_currentSearchPosition = _searchPositions.isEmpty() ? -1 : _searchPositions.firstKey() - 1;
			//_currentSearchPosition = _searchPositions.isEmpty() ? -1 : _searchPositions.lastKey();
			//JOptionPane.showMessageDialog(this, "No more results found for '" + _mibBrowser.getCurrentSearchKey() + "'");
		}
		
	}

	@Override
	public void findSearchResult() throws SearchResultNotFoundException {
		String searchKey = getCurrentSearchKey();
		String searchKeyAdjusted = isRegex() ? searchKey : Pattern.quote(searchKey);
		Pattern patt = null;
		if(isCaseSensitive()) {
			patt = Pattern.compile(searchKeyAdjusted);
		} else {
			patt = Pattern.compile(searchKeyAdjusted, Pattern.CASE_INSENSITIVE);
		}
		Matcher matt = patt.matcher(this.getSearchText());
		_searchPositions.clear();
		while(matt.find()) {
			_searchPositions.put(matt.start(), matt.end());
		}
		_currentSearchPosition = _searchPositions.isEmpty() ? -1 : _searchPositions.firstKey();
		if(_searchPositions.isEmpty())
			throw new SearchResultNotFoundException(false, _mibBrowser.getCurrentSearchKey());
	}

	@Override
	public void showResult() {
		_hilit.removeAllHighlights();
		//highlight the results
		Set<Integer> positions = _searchPositions.keySet();
		for(int pos: positions) {
			try {
				_hilit.addHighlight(pos, _searchPositions.get(pos), _painter);
			} catch (BadLocationException e1) {
					e1.printStackTrace();
			}
		}
		//move to the position
		moveToCurrentPosition();
		
	}
	
	private void moveToCurrentPosition() {
		String currentSearchKey = _mibBrowser.getCurrentSearchKey();
		int end = _currentSearchPosition + currentSearchKey.length();
		int x = end - 1;
		int row = _currentSearchPosition;
		try {
			if(!_mibArea.getDocument().getText(_currentSearchPosition + currentSearchKey.length(), 1).matches("[\n\r]")) {
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

	
}
