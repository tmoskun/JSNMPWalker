package com.ezcode.jsnmpwalker.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.menu.AbstractMibViewPopupMenu;
import com.ezcode.jsnmpwalker.menu.MibViewWindowPopupMenu;
import com.ezcode.jsnmpwalker.panel.MibBrowserPanel;
import com.ezcode.jsnmpwalker.panel.MibViewPanel;
import com.ezcode.jsnmpwalker.search.Searchable;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public class MibBrowserWindow extends JFrame {
	private static int WIDTH = SNMPSessionFrame.WIDTH/3;
	private static int HEIGHT = SNMPSessionFrame.HEIGHT/2;
	
	//private MibBrowserPanel _mibBrowser;
	private MibViewPanel _mibViewPanel;
	
	public MibBrowserWindow(MibBrowserPanel mibBrowser, String title, String descr) {
		super(title);
		//_mibBrowser = mibBrowser;
		_mibViewPanel = new MibViewPanel(this, mibBrowser, WIDTH, HEIGHT, descr);
		init(_mibViewPanel);
	}
	
	
	public MibBrowserWindow(MibBrowserPanel mibBrowser, MibViewPanel mibViewPanel, String title) {
		super(title);
		//_mibBrowser = mibBrowser;
		_mibViewPanel = mibViewPanel;
		_mibViewPanel.setFrame(this);
		init(mibViewPanel);
	}
	
	public void init(MibViewPanel mibViewPanel) {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//height of the task bar
		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		int taskBarSize = scnMax.bottom;
		int maxWidth = screenSize.width;
		int maxHeight = screenSize.height - taskBarSize;
		
		if(WIDTH > maxWidth) {
			WIDTH = maxWidth;
		}
		if(HEIGHT > maxHeight) {
			HEIGHT = maxHeight;
		}
			
		setSize(WIDTH, HEIGHT);
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.put("TextPane.selectionForeground", Color.GREEN.darker());
		} catch (Exception e) {
			e.printStackTrace();
		}
        //mibViewPanel.setWindowPopup(this);
        
        //JScrollPane mibViewPanel = new MibViewPanel(_mibBrowser, WIDTH, HEIGHT, descr, new MibViewWindowPopupMenu());
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mibViewPanel, BorderLayout.CENTER);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		//setLocationByPlatform(true);
		setAlwaysOnTop(true);

/*
		addWindowListener(new WindowAdapter() {			
			@Override
			public void windowClosing(WindowEvent e) {			
				super.windowClosing(e);
				MibBrowserWindow.this.setVisible(false);
				MibBrowserWindow.this.dispose();
			}

		});
*/
	}
	
	public MibViewPanel getMibViewPanel() {
		return _mibViewPanel;
	}
	
	public void editTitle() {
		String title = JOptionPane.showInputDialog(this, "Tab title:", getTitle());
		if(title != null && title.length() > 0) {
			setTitle(title);
		}
	}
	
}
