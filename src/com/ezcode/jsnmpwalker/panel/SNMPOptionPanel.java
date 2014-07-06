package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import com.ezcode.jsnmpwalker.data.SNMPOptionModel;
import com.ezcode.jsnmpwalker.layout.SpringUtilities;
import com.ezcode.jsnmpwalker.listener.OptionFieldListener;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class SNMPOptionPanel extends JPanel {
	private SNMPOptionModel _optionModel;
	
	public SNMPOptionPanel(SNMPOptionModel optionModel) {
		_optionModel = optionModel;
		init();
	}
	
	public void init() {
		this.setLayout(new SpringLayout());
		setBorder(PanelUtils.DIALOG_BORDER);
		
		final JLabel portLabel = new JLabel("Port Number: ", JLabel.TRAILING);
		add(portLabel);
		final JTextField port = new JTextField(_optionModel.get(SNMPOptionModel.PORT_KEY));
		port.setPreferredSize(PanelUtils.FIELD_DIM);
		add(port);
		portLabel.setLabelFor(port);
		port.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, port, SNMPOptionModel.PORT_KEY));
		
		
		final JLabel timeoutLabel = new JLabel("Timeout: ", JLabel.TRAILING);
		add(timeoutLabel);
		final JTextField timeout = new JTextField(_optionModel.get(SNMPOptionModel.TIMEOUT_KEY));
		timeout.setPreferredSize(PanelUtils.FIELD_DIM);
		add(timeout);
		timeoutLabel.setLabelFor(timeout);
		timeout.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, timeout, SNMPOptionModel.TIMEOUT_KEY));
		
		final JLabel retriesLabel = new JLabel("Retries: ", JLabel.TRAILING);
		add(retriesLabel);
		final JTextField retries = new JTextField(_optionModel.get(SNMPOptionModel.RETRIES_KEY));
		retries.setPreferredSize(PanelUtils.FIELD_DIM);
		add(retries);
		retriesLabel.setLabelFor(retries);
		retries.getDocument().addDocumentListener(new OptionFieldListener(_optionModel, retries, SNMPOptionModel.RETRIES_KEY));
		
	    SpringUtilities.makeCompactGrid(this, //parent
                3, 2,
                5, 5,  //initX, initY
                10, 10); //xPad, yPad
	}
	

}
