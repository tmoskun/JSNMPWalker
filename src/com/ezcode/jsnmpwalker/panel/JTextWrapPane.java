package com.ezcode.jsnmpwalker.panel;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;


public class JTextWrapPane extends JTextPane {
    
    boolean _wrapState = true;
    JTextArea j = new JTextArea();
    
    /*
     * Constructor
     */
    JTextWrapPane(boolean wrapState) {
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

