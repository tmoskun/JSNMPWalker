package com.ezcode.jsnmpwalker.panel;

/**
 * Copyright(c) 2014
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.plaf.basic.BasicButtonUI;

import com.ezcode.jsnmpwalker.menu.AbstractMibViewPopupMenu;
import com.ezcode.jsnmpwalker.menu.MibViewTabPopupMenu;
import com.ezcode.jsnmpwalker.utils.PanelUtils;

public class TabPanel extends JPanel {
	final private static MouseListener ML = new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
	};
	
	private MibBrowserPanel _mibPanel;
	private JLabel _tabName;
	
	public TabPanel(MibBrowserPanel mibPanel) {
		this(mibPanel, null);
	}
	
	
	public TabPanel(MibBrowserPanel mibPanel, String title) {
		super(new FlowLayout(FlowLayout.LEFT));
		_mibPanel = mibPanel;
		init(title);
	}
	
	private void init(String title) {
		final JTabbedPane tp = _mibPanel.getTabbedPane();
		String t = (title == null || title.length() == 0) ? "Untitled " + tp.getTabCount() : title;
		_tabName = new JLabel(t);
	    _tabName.setOpaque(false);
		_tabName.setBorder(null);
		_tabName.setFont(new Font(PanelUtils.UI_DEFAULTS.getFont("Button.font").getFamily(), Font.BOLD, 12));
		add(_tabName);
		final RemoveTabButton removeButt = new RemoveTabButton(this, ML);
		add(removeButt);
		_tabName.addMouseListener(new MouseAdapter() {
			private void TabPopupEvent(MouseEvent event) {
				int x = event.getX();
				int y = event.getY();
					
		        Component comp = (Component) event.getSource();
		            
				MibViewTabPopupMenu popup = new MibViewTabPopupMenu(_mibPanel, TabPanel.this);
				popup.buildMenu();
				popup.show(comp, x, y);
			}
				
				
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TabPopupEvent(e);
				} 
			}
				
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TabPopupEvent(e);
				} 
			}
				
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 1) {
					int index = tp.indexOfTabComponent(TabPanel.this);
					tp.setSelectedIndex(index);
				} else if(e.getClickCount() == 2) {
					editTitle();
				}
			}
				
		});
		setOpaque(false);
	}
	
	
	public void editTitle() {
		String title = JOptionPane.showInputDialog(_mibPanel, "Tab title:", _tabName.getText());
		if(title != null && title.length() > 0) {
			_tabName.setText(title);
		}
	}
	
	public void setTitle(String title) {
		_tabName.setText(title);
	}
	
	public String getTitle() {
		return _tabName.getText();
	}
	
	public JLabel getLabel() {
		return _tabName;
	}
	
/*
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof TabPanel) {
			return ((TabPanel) obj).getTitle().equals(getTitle());
		}
		return false;
	}
*/
	
	private class RemoveTabButton extends JButton implements ActionListener {
		private Component _tabComponent;
		
	    public RemoveTabButton(Component comp, MouseListener buttListener) {
	       _tabComponent = comp;
	       int size = 17;
	       setPreferredSize(new Dimension(size, size));
	       setToolTipText("Close tab");
	       setUI(new BasicButtonUI());
	       //Make it transparent
	       setContentAreaFilled(false);
	       //No need to be focusable
	       setFocusable(false);
	       setBorder(BorderFactory.createRaisedBevelBorder());
	       setBorderPainted(false);
	       addMouseListener(buttListener);
	       addActionListener(this);
	       setRolloverEnabled(true);
	    }
	    
        //we don't want to update UI for this button
        public void updateUI() {
        }

        //paint the cross
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            //shift the image for pressed buttons
            if (getModel().isPressed()) {
                g2.translate(1, 1);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            int delta = 6;
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }

		@Override
		public void actionPerformed(ActionEvent e) {
			_mibPanel.removeMibPanel(_tabComponent);
			
		}
	}
	    

}
