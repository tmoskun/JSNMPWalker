package com.ezcode.jsnmpwalker.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;


/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class MibBrowserPanel extends JPanel {
	
	private static final Color HILIT_COLOR = Color.YELLOW;
	private static final int FIELD_WIDTH = Math.min(SNMPSessionFrame.WIDTH/6, 300);
	
	private AttributeSet _docAttributes;
	private JTextWrapPane _mibArea;
	private final Highlighter _hilit;
	private final Highlighter.HighlightPainter _painter;

	public MibBrowserPanel() {
		super(new BorderLayout());
		_hilit = new DefaultHighlighter();
		_painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
		init();
	}
	
	public void init() {
		JPanel viewPane = new JPanel(new BorderLayout());
		viewPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "MIB Browser")));
		StyleContext sc = StyleContext.getDefaultStyleContext();
		_docAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
		JScrollPane mibScrollPane = new JScrollPane();
		StyledDocument logDoc = new DefaultStyledDocument();
		_mibArea = new JTextWrapPane(logDoc, false);
		_mibArea.setEditable(false);
		_mibArea.setHighlighter(_hilit);
		_mibArea.setSelectedTextColor(logDoc.getForeground(_docAttributes));
		_mibArea.setText("");
		_mibArea.setPreferredSize(new Dimension(SNMPSessionFrame.WIDTH/2, SNMPSessionFrame.HEIGHT/3));;
		mibScrollPane.getViewport().add(_mibArea, BorderLayout.NORTH);
		viewPane.add(mibScrollPane, BorderLayout.CENTER);
		
		add(viewPane, BorderLayout.CENTER);
	}
		
	public void setMibDescription(String descr) {
		_mibArea.setText(descr);
	}
	
	

}
