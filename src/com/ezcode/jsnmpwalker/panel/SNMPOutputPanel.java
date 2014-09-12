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
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;

import com.ezcode.jsnmpwalker.action.ButtonAction;
import com.ezcode.jsnmpwalker.layout.WrapLayout;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPOutputPanel extends JPanel {
	private JFrame _frame;
	private JTextField _logFileField;
	private String _logFile = "";
	private JButton _saveFile;
	private AttributeSet _docAttributes;
	private JTextWrapPane _logArea;
	private String _currentSearchKey = "";
	private JTextField _searchField;
	private JButton _searchButton;
	private JButton _resetSearchButton;
	private JCheckBox _regCheckBox;
	private SNMPSearcher _searcherThread = null;
	private final Set<Integer> _searchPositions;
	private Iterator<Integer> _searchIterator;
	private JButton _clearLogButton;
	private JLabel _loadingImg;
	private final Highlighter _hilit;
	private final Highlighter.HighlightPainter _painter;
	
	public SNMPOutputPanel(JFrame frame) {
		//this(frame, "");
		super(new BorderLayout());
		_frame = frame;
		_hilit = new DefaultHighlighter();
		_painter = new DefaultHighlighter.DefaultHighlightPainter(PanelUtils.HILIT_COLOR);
		_searchPositions = new TreeSet<Integer>();
		_searchIterator = _searchPositions.iterator();
		java.net.URL imgURL = getClass().getResource("/img/loader.gif");
		if(imgURL != null) {
			_loadingImg = new JLabel(new ImageIcon(imgURL));
			_loadingImg.setVisible(false);
		} else {
			_loadingImg = new JLabel("Running...");
			System.out.println("image not found");
		}
		init();
	}
	
	
	public void init() {
		//Output panel
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "SNMP Output")));
		
		//JPanel logPane = new JPanel(new BorderLayout());
		StyleContext sc = StyleContext.getDefaultStyleContext();
		_docAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
		
		/*
		
		JPanel filterPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//filterPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Filter Data")));
		filterPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel ipLabel = new JLabel("IP:");
		JTextField ipFilter = new JTextField();
		ipFilter.setPreferredSize(new Dimension(100, 20));
		ipLabel.setLabelFor(ipFilter);
		filterPane.add(ipLabel);
		filterPane.add(ipFilter);
		JLabel oidLabel = new JLabel("OID:");
		JTextField oidFilter = new JTextField();
		oidFilter.setPreferredSize(new Dimension(140, 20));
		oidLabel.setLabelFor(oidFilter);
		filterPane.add(oidLabel);
		filterPane.add(oidFilter);
		JLabel typeLabel = new JLabel("Data Type:");
		JTextField typeFilter = new JTextField();
		typeFilter.setPreferredSize(new Dimension(100, 20));
		typeLabel.setLabelFor(typeFilter);
		filterPane.add(typeLabel);
		filterPane.add(typeFilter);
		JButton filterButt = new JButton("Filter Data");
		filterPane.add(filterButt);
		
		filterButt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		logPane.add(filterPane, BorderLayout.NORTH);
		*/
		
		
		JPanel searchPane = new JPanel(new WrapLayout(FlowLayout.LEFT));
		_searchField = new JTextField();
		_searchField.setPreferredSize(new Dimension(PanelUtils.FIELD_WIDTH, 20));
		SearchListener searchLis = new SearchListener(_searchField);
		_searchField.addActionListener(searchLis);
		_searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				resetSearch(false);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				resetSearch(false);	
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				resetSearch(false);
			}	
		});
		
		_searchButton = new JButton(PanelUtils.TEXT_SEARCH);
		_searchButton.addActionListener(searchLis);
		_resetSearchButton = new JButton("Reset");
		_resetSearchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetSearch(true);
			}	
		});
		_regCheckBox = new JCheckBox("Regex");
		searchPane.add(_searchField);
		searchPane.add(_searchButton);
		searchPane.add(_resetSearchButton);
		searchPane.add(_regCheckBox);
		if(_loadingImg != null) {
			searchPane.add(_loadingImg);
		}
		
		//logPane.add(searchPane, BorderLayout.CENTER);
		//add(logPane, BorderLayout.NORTH);
		add(searchPane, BorderLayout.NORTH);
		
		JScrollPane logScrollPane = new JScrollPane();
		StyledDocument logDoc = new DefaultStyledDocument();
		_logArea = new JTextWrapPane(logDoc, false);
		_logArea.setEditable(false);
		_logArea.setHighlighter(_hilit);
		_logArea.setSelectedTextColor(logDoc.getForeground(_docAttributes));
		logScrollPane.getViewport().add(_logArea);
		add(logScrollPane, BorderLayout.CENTER);
		
		//JPanel logButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//_clearLogButton = new JButton("Clear output panel");
		//_clearLogButton.setMnemonic(KeyEvent.VK_C);
		//_clearLogButton.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent e) {
		//		clearLog();	
		//		resetSearch(false);
		//	}	
		//});
		
		//_clearLogButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "Clear");
		//_clearLogButton.getActionMap().put("Clear", new ButtonAction(_frame, _clearLogButton));
		
		//logButtons.add(_clearLogButton);
		//logPane.add(logButtons, BorderLayout.SOUTH);
		
		//add(logPane, BorderLayout.CENTER);
		
		
		JPanel filePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filePane.add(new JLabel("Save Result in "));
		_logFileField = new JTextField();
		_logFileField.setPreferredSize(new Dimension(PanelUtils.FIELD_WIDTH, 20));
		filePane.add(_logFileField);
		JButton choosefile = new JButton("Directory/File");
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
		_saveFile = new JButton("Save");
		
		_saveFile.addActionListener(new ActionListener() {
			private void saveData() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						toggleOutput(true);
					}
				});
				Element root = _logArea.getDocument().getDefaultRootElement();
				int lineCount = root.getElementCount();
				if(lineCount <= 0) {
					JOptionPane.showMessageDialog(null, "The log area is empty");
					return;
				} 
				if(_logFile == null || _logFile.length() == 0) {
					JOptionPane.showMessageDialog(null, "The file name is empty");
				} else {
					File log = new File(_logFile);
					if(log.isDirectory()) {
						int result = JOptionPane.showConfirmDialog(null, "The log file is a directory. Would you like to write log into " + _logFile + ".txt?", "Confirm file name", JOptionPane.OK_CANCEL_OPTION);
						if(result != JOptionPane.OK_OPTION) 
							return;
					}
					if(!log.getName().endsWith(".txt")) {
						_logFile = log.getAbsolutePath() + ".txt";
						log = new File(_logFile);
					}
					if(log.exists()) {
						int result = JOptionPane.showConfirmDialog(null, "The file already exists, do you want to override it?", "Confirm override", JOptionPane.OK_CANCEL_OPTION);
						if(result != JOptionPane.OK_OPTION) {
							return;
						} 
					} 
					try {
						OutputStreamWriter fstream = new FileWriter(_logFile);
						BufferedWriter writer = new BufferedWriter(fstream);
						int start = 0;
						int end = 0;
						String str = "";
						for(int i = 0; i < lineCount; i++) {
							Element elem = root.getElement(i);
							start = elem.getStartOffset();
							end = elem.getEndOffset();
							str = _logArea.getText(start, end-start);
							writer.write(str);
						}
						writer.close();
						JOptionPane.showMessageDialog(null, "Data saved");
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								toggleOutput(false);
							}
						});
					} catch (IOException ex) {
						System.out.println("Can't create output stream");
						ex.printStackTrace();
					} catch (BadLocationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
			public void actionPerformed(ActionEvent e) {
				new Runnable() {

					@Override
					public void run() {
						saveData();
					}
					
				}.run();
			}
			
		});
		filePane.add(_saveFile);
		add(filePane, BorderLayout.SOUTH);
	}
	
	public void clearLog() {
		_logArea.setText("");
	}
		
	public void resetSearch(boolean clearHighlights) {
		_regCheckBox.setEnabled(true);
		_currentSearchKey = "";
		_searchPositions.clear();
		_searchIterator = _searchPositions.iterator();
		if(clearHighlights) {
			_hilit.removeAllHighlights();
		}
		_logArea.setCaretPosition(0);
	}
	
    public void toggleSearchControls(boolean isrun) {
    	_searchField.setEnabled(!isrun);
    	_resetSearchButton.setEnabled(!isrun);
    	_regCheckBox.setEnabled(!isrun);
    	if(isrun) {
    		_searchButton.setText(PanelUtils.TEXT_STOP);
    	} else {
    		_searchButton.setText(PanelUtils.TEXT_SEARCH);
    	}
    	_loadingImg.setVisible(isrun);
    }
	
	public String getResult() {
		String result = "";
		Document doc = _logArea.getDocument();
		try {
			result = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void appendResult(String result) {
		Document doc = _logArea.getDocument();
		try {
			doc.insertString(doc.getLength(), result, _docAttributes);			
		} catch (BadLocationException e) {
			e.printStackTrace();
		} 
	}
	
	public void toggleOutput(boolean isrun) {
		_loadingImg.setVisible(isrun);	
		_logFileField.setEditable(!isrun);
		_logFileField.setEnabled(!isrun);
		_saveFile.setEnabled(!isrun);
		//_clearLogButton.setEnabled(!isrun);
	}
	
	private class SearchListener implements ActionListener {
		private JTextField _searchField;
		
		public SearchListener(JTextField searchField) {
			_searchField = searchField;
		}
		
		public void actionPerformed(ActionEvent e) {
			Object obj = e.getSource();
			String name = "";
			if(obj instanceof JButton) {
				name = ((JButton) obj).getText();
			}
			if(name.equals(PanelUtils.TEXT_STOP)) {
				_searcherThread.interrupt();
			} else {
				Document doc = _logArea.getDocument();
				String str = "";
				try {
					str = doc.getText(0, doc.getLength());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				String searchKey = _searchField.getText();
				if(searchKey.length() > 0) {
					_searcherThread = new SNMPSearcher(str, searchKey);
					_searcherThread.start();
				} else {
					JOptionPane.showMessageDialog(null, "No search key provided");
				}
			}
		}	
	}
	
    private class SNMPSearcher extends Thread {
    	private String _searchText;
    	private String _searchKey;
    	
    	protected SNMPSearcher(String searchText, String searchKey) {
    		_searchText = searchText;
    		_searchKey = searchKey;
    	}
    	
        @Override
        public void interrupt() {
        	super.interrupt();
        	while(!isInterrupted()) {
        		try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	resetSearch(true);
        	toggleSearchControls(false);
        }
          

		@Override
		public void run() {	
			toggleSearchControls(true);
			if(_searchKey.equals(_currentSearchKey)) {
				//reset the iterator if it's finished
				if(!_searchIterator.hasNext()) {
					_searchIterator = _searchPositions.iterator();
				}
			} else {
				_currentSearchKey = _searchKey;
				String searchKeyAdjusted = _regCheckBox.isSelected() ? _searchKey : Pattern.quote(_searchKey);
				Pattern patt = Pattern.compile(searchKeyAdjusted);
				Matcher matt = patt.matcher(_searchText);
				_searchPositions.clear();
				_hilit.removeAllHighlights();
				while(matt.find()) {
					try {
						_searchPositions.add(matt.start());
						_hilit.addHighlight(matt.start(), matt.end(), _painter);
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
				_searchIterator = _searchPositions.iterator();
			}
			toggleSearchControls(false);
			if(_searchIterator.hasNext()) {
				int position = _searchIterator.next();
				int end = position + _currentSearchKey.length();
				int x = end - 1;
				int row = position;
				try {
					if(!_logArea.getDocument().getText(position + _currentSearchKey.length(), 1).matches("[\n\r]")) {
						x = Utilities.getNextWord(_logArea, position);
					}
					row = Utilities.getRowStart(_logArea, position);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				_logArea.setCaretPosition(x);
				_logArea.select(row, x);
				_regCheckBox.setEnabled(false);
				_logArea.requestFocusInWindow();
			} else {
				JOptionPane.showMessageDialog(null, "No search results found");
			}
		}
    	
    }

}
