package com.ezcode.jsnmpwalker.panel;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class JTextWrapPane extends JTextPane {
    private boolean _wrapState = true;
    
    /*
     * Constructor
     */
      
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
