package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;

public class DataPanel extends JPanel {
	private SNMPSessionFrame _frame;
	private MibPanel _mibPane;
	private DevicePanel _networkPane;
	
	private JLabel _loadingDataImg;
	
	public DataPanel(SNMPSessionFrame frame) {
		super(new BorderLayout());
		_frame = frame;
		java.net.URL imgURL = getClass().getResource("/img/loader2.gif");
		if(imgURL != null) {
			_loadingDataImg = new JLabel(new ImageIcon(imgURL));
			_loadingDataImg.setVisible(false);
		} else {
			System.out.println("image not found");
		}
		init();
	}
	
	private void init() {
		JLabel dataLabel = new JLabel("Drag and drop to the left panel");
		Font labelFont = new Font("Serif", Font.ITALIC, 12);
		dataLabel.setFont(labelFont);
		this.add(dataLabel, BorderLayout.NORTH);
		
		_mibPane = new MibPanel(_loadingDataImg, (SNMPOutputPanel) _frame.getOutputPane());
		_networkPane = new DevicePanel(_frame, _loadingDataImg);
		
		JSplitPane dataSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _mibPane, _networkPane);
		dataSplitPane.setBorder(null);
		dataSplitPane.setOneTouchExpandable(true);
		dataSplitPane.setDividerLocation(SNMPSessionFrame.HEIGHT/3);
		Dimension dataMinSize = new Dimension(200, 200);
		_mibPane.setMinimumSize(dataMinSize);
		_networkPane.setMinimumSize(dataMinSize);
		add(dataSplitPane, BorderLayout.CENTER);
		
	}
	
	public JPanel getMibPanel() {
		return _mibPane;
	}
	
	public JPanel getNetworkPanel() {
		return _networkPane;
	}
		
}
