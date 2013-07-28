package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;

public class SNMPOutputPanel extends JPanel {
	private static final Color HILIT_COLOR = Color.YELLOW;
	
	private JTextField _logFileField;
	private String _logFile = "";
	private AttributeSet _docAttributes;
	//private AttributeSet _highlightAttributes;
	private JTextWrapPane _logArea;
	//private StyledDocument _logDoc;
	//private JTextPane _logArea;
	private JLabel _loadingSNMPImg;
	private final Highlighter hilit;
	private final Highlighter.HighlightPainter painter;
	//private JPanel _searchPane;
	
	public SNMPOutputPanel(String logFile) {
		super(new BorderLayout());
		_logFile = logFile;
		hilit = new DefaultHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
		try {
			_loadingSNMPImg = new JLabel(new ImageIcon(new URL("file:img/loader.gif")));
			_loadingSNMPImg.setVisible(false);
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		init();
	}
	
	public void init() {
		//JPanel rightPane = new JPanel(new BorderLayout());
		//rightPane.setPreferredSize(new Dimension(600, 800));
		
		//Output panel
		JPanel filePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filePane.add(new JLabel("Output SNMP result to "));
		_logFileField = new JTextField();
		_logFileField.setPreferredSize(new Dimension(300, 20));
		filePane.add(_logFileField);
		JButton choosefile = new JButton("Log directory or file");
		final JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
		fc.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
		choosefile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int returnval = fc.showOpenDialog(null);
				if(returnval == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					_logFileField.setText(file.getAbsolutePath());
				}		
			}	
		});
		_logFileField.getDocument().addDocumentListener(new DocumentListener() {
			private void setLogFile() {
				_logFile = _logFileField.getText();
			}
			public void changedUpdate(DocumentEvent e) {
				setLogFile();		
			}
			public void insertUpdate(DocumentEvent e) {
				setLogFile();				
			}
			public void removeUpdate(DocumentEvent e) {
				setLogFile();		
			}
			
		});
		filePane.add(choosefile);
		add(filePane, BorderLayout.NORTH);
		
		JPanel logPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		logPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Output")));
		StyleContext sc = StyleContext.getDefaultStyleContext();
		_docAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
		//_docAttributes = new SimpleAttributeSet();
		//StyleConstants.setForeground(_docAttributes, Color.BLACK);
		//_highlightAttributes = sc.addAttribute(_docAttributes, StyleConstants.Background, Color.YELLOW);
//		if(_loadingSNMPImg != null) {
//			logPane.add(_loadingSNMPImg);
//		}
		JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JTextField searchField = new JTextField();
		searchField.setPreferredSize(new Dimension(300, 20));
		SearchListener searchLis = new SearchListener(searchField);
		searchField.addActionListener(searchLis);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(searchLis);
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hilit.removeAllHighlights();		
			}
		});
		searchPane.add(searchField);
		searchPane.add(searchButton);
		searchPane.add(clearButton);
		logPane.add(searchPane);
		if(_loadingSNMPImg != null) {
			logPane.add(_loadingSNMPImg);
		}
		
		JScrollPane sp = new JScrollPane();
		//sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		//sp.setPreferredSize(new Dimension(WIDTH/2, 670));
		//_logDoc = new DefaultStyledDocument();
		StyledDocument logDoc = new DefaultStyledDocument();
		//JTextWrapPane logArea = new JTextWrapPane(_logDoc, false);
		_logArea = new JTextWrapPane(logDoc, false);
		_logArea.setEditable(false);
		_logArea.setHighlighter(hilit);
		//_logArea = new JTextPane();
		//_logArea.setEditable(false);
		//sp.getViewport().add(_logArea);
		sp.getViewport().add(_logArea);
		sp.getViewport().setPreferredSize(new Dimension(SNMPSessionFrame.WIDTH/2 - 50, SNMPSessionFrame.HEIGHT - 140));
		logPane.add(sp);
		
		add(logPane);
	}
	
	public void setResult(String result) {
		_logArea.setText(result);
	}
	
	public String getResult() {
		String result = "";
		Document doc = _logArea.getDocument();
		try {
			result = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		//return _logArea.getText();
	}
	
	public void appendResult(String result) {
		//_logArea.append(result);
		Document doc = _logArea.getDocument();
		try {
			doc.insertString(doc.getLength(), result, _docAttributes);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void toggleSNMPRun(boolean isrun) {
		_loadingSNMPImg.setVisible(isrun);	
		_logFileField.setEditable(!isrun);
		_logFileField.setEnabled(!isrun);
	}
	
	private class SearchListener implements ActionListener {
		private JTextField _searchField;
		
		public SearchListener(JTextField searchField) {
			_searchField = searchField;
		}
		
		public void actionPerformed(ActionEvent e) {
			//List<Integer> positions = new ArrayList<Integer>();
			String str = _logArea.getText();
			Pattern patt = Pattern.compile(_searchField.getText());
			Matcher matt = patt.matcher(str);
			while(matt.find()) {
				//positions.add(matt.start());
				try {
					hilit.addHighlight(matt.start(), matt.end(), painter);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
			}
		}	
	}
	
	private class JTextWrapPane extends JTextPane {
	    
	    boolean _wrapState = true;
	    JTextArea j = new JTextArea();
	    
	    /*
	     * Constructor
	     */
	    JTextWrapPane(boolean wrapState) {
	        //super();
	    	super();
	    	_wrapState = wrapState;
	    }
	      
	    public JTextWrapPane(StyledDocument log, boolean wrapState) {
	    	super(log);
	    	_wrapState = wrapState;
	    }
	    
	    /*
	     *
	     */
	    public boolean getScrollableTracksViewportWidth() {
	        return _wrapState;
	    }
	    
	    /*
	     *
	     */
	    public void setLineWrap(boolean wrap) {
	        _wrapState = wrap;
	    }
	    
	    /*
	     *
	     */
	    public boolean getLineWrap(boolean wrap) {
	        return _wrapState;
	    }
	} 

}
