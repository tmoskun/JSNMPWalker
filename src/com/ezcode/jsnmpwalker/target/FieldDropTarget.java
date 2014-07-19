package com.ezcode.jsnmpwalker.target;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDropEvent;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.ezcode.jsnmpwalker.utils.PanelUtils;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public class FieldDropTarget extends AbstractSNMPDropTarget {
	
	public JTextComponent _field;
	
	public FieldDropTarget(JTextComponent field) {
		_field = field;
	}

	@Override
	protected void insertTransferData(DropTargetDropEvent evt, Object data, int nodeType) {
        try {
			evt.acceptDrop(DnDConstants.ACTION_COPY);
        	List<Object> list = (List<Object>) data;
    		if(_field instanceof JTextArea) {
    			StringBuffer buff = new StringBuffer();
    			PanelUtils.appendWithLineBreak(buff, list);
    			if(buff.length() > 0) {
    				if(_field.getText().length() > 0)
    					((JTextArea) _field).append(System.getProperty("line.separator"));
    				((JTextArea) _field).append(buff.toString().trim());
    			}
    		} else if(_field instanceof JTextField){
    			if(!list.isEmpty()) {
    				_field.setText(PanelUtils.formatData(list.get(0)));
    			}
    		}
        } catch(Exception ex) {
        	ex.printStackTrace();
        	evt.rejectDrop();
            //System.err.println(ex.getMessage());
        } finally {
            evt.dropComplete(true);
        }
		
	}

}
