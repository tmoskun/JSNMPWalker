package com.ezcode.jsnmpwalker.panel;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.ezcode.jsnmpwalker.SNMPSessionFrame;
import com.ezcode.jsnmpwalker.listener.NetworkDeviceDragGestureListener;
import com.ezcode.jsnmpwalker.worker.NetworkScanner;

public class DevicePanel extends JPanel {
	private static final DecimalFormat _prefixFormatter = new DecimalFormat("#,###");
	public static final int DEFAULT_TEST_REACHABLE = 3000;
	
	private SNMPSessionFrame _frame;
	private JLabel _loadingDataImg;
	private JTable _deviceList;
	private DefaultTableModel _networkListModel;
	private JButton _scanNetworkButton;
	private JButton _cancelScanButton;
	private JButton _clearNetworkList;
	
	public DevicePanel(SNMPSessionFrame frame, JLabel loadingDataImg) {
		super(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Discover Network"));
		_frame = frame;
		_loadingDataImg = loadingDataImg;
		init();
	} 
	
	public void init() {
		
		JPanel deviceListPane = new JPanel(new BorderLayout());
		_networkListModel = new NetworkDeviceTableModel();
		_networkListModel.addColumn("Devices");
		_deviceList = new JTable(_networkListModel);
		TableCellRenderer renderer = new NetworkDeviceRenderer();
		_deviceList.getColumn("Devices").setCellRenderer(renderer);
		deviceListPane.add(_deviceList, BorderLayout.CENTER);
		JPanel ipPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ipPane.add(new JLabel("IP:"));
		final JTextField showIp = new JTextField();
		showIp.setPreferredSize(new Dimension(220, 20));
		showIp.setEditable(false);
		showIp.setBackground(Color.decode("#F9F9F9"));
		ipPane.add(showIp);
		deviceListPane.add(ipPane, BorderLayout.SOUTH);
        _deviceList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int row = _deviceList.getSelectedRow();	
				int col = _deviceList.getSelectedColumn();
				if(row < 0 || col < 0) {
					showIp.setText("");
				} else {
					InetAddress address = (InetAddress) _deviceList.getValueAt(row, col);
					showIp.setText(address.getHostAddress());
				}
			}    	
        });
		DragSource deviceDragSource = new DragSource();
		deviceDragSource.createDefaultDragGestureRecognizer(_deviceList, DnDConstants.ACTION_COPY, new NetworkDeviceDragGestureListener(_deviceList));	
		JScrollPane sp = new JScrollPane(deviceListPane);
		
		add(sp, BorderLayout.CENTER);
		JPanel formPane = new JPanel(new BorderLayout());
		
		JPanel paramsPane = new JPanel(new GridLayout(0, 2, 10, 4));
		paramsPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel typeLabel = new JLabel("Network type:", JLabel.TRAILING);
		String[] types = {NetworkScanner.IPv4, NetworkScanner.IPv6};
		final JComboBox netTypes = new JComboBox(types);
		paramsPane.add(typeLabel);
		paramsPane.add(netTypes);
		typeLabel.setLabelFor(netTypes);
		JLabel ipLabel = new JLabel("Ip:", JLabel.TRAILING);
		final JTextField ip = new JTextField("localhost");
		paramsPane.add(ipLabel);
		paramsPane.add(ip);
		ipLabel.setLabelFor(ip);
		JLabel maskLabel = new JLabel("Prefix length:", JLabel.TRAILING);
		final JComboBox mask = new JComboBox();
		paramsPane.add(maskLabel);
		paramsPane.add(mask);
		maskLabel.setLabelFor(mask);
		JLabel timeoutLabel = new JLabel("Scanning timeout", JLabel.TRAILING);
		final JTextField timeoutField = new JTextField(String.valueOf(DEFAULT_TEST_REACHABLE));
		paramsPane.add(timeoutLabel);
		paramsPane.add(timeoutField);
		timeoutLabel.setLabelFor(timeoutField);
		
		formPane.add(paramsPane, BorderLayout.CENTER);
		
		JPanel scanButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
		_scanNetworkButton = new JButton("Scan for devices");
		_scanNetworkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int timeout = DEFAULT_TEST_REACHABLE;
				try {
					timeout = Integer.parseInt(timeoutField.getText());
				} catch (Exception ex) {
					//best effort
				}
				_frame.scanNetwork(ip.getText(), (Integer) mask.getSelectedItem(), (String) netTypes.getSelectedItem(), timeout);	
			}
		});
		scanButtons.add(_scanNetworkButton);
		_cancelScanButton = new JButton("Cancel");
		_cancelScanButton.setEnabled(false);
		_cancelScanButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_frame.stopScanning();
			}
			
		});
		scanButtons.add(_cancelScanButton);
		_clearNetworkList = new JButton("Clear list");
		_clearNetworkList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the list?");
				if(result == JOptionPane.YES_OPTION) {
					int rowCount = _networkListModel.getRowCount();
					for(int i = rowCount-1; i >= 0; i--) {
						_networkListModel.removeRow(i);
					}
				}
			}	
		});
		scanButtons.add(_clearNetworkList);

		if(_loadingDataImg != null) {
			scanButtons.add(_loadingDataImg);
		}
		
		formPane.add(scanButtons, BorderLayout.SOUTH);
		add(formPane, BorderLayout.SOUTH);
		
		netTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nt = (String) netTypes.getSelectedItem();
				mask.removeAllItems();
				Integer numOfBits = NetworkScanner.PREFIX_LENGTHS.get(nt);
				PrefixLengthModel model = new PrefixLengthModel(numOfBits);
				PrefixLengthRenderer renderer = new PrefixLengthRenderer(numOfBits);
				Vector<Integer> items = new Vector<Integer>();
				if(numOfBits != null) {
					for(int i = 1; i <= numOfBits.intValue(); i++) {
						model.addElement(i);
					}
				}
				mask.setModel(model);
				mask.setRenderer(renderer);
				mask.setSelectedItem(NetworkScanner.PREFIX_DEFAULTS.get(nt));
			}		
		});
		netTypes.setSelectedIndex(0);
	}
	
	public void toggleNetScan(boolean isrun) {
		if(_loadingDataImg != null)
			_loadingDataImg.setVisible(isrun);
		_scanNetworkButton.setEnabled(!isrun);
		_cancelScanButton.setEnabled(isrun);
		_clearNetworkList.setEnabled(!isrun);
	}
	
	public void addAddress(InetAddress address) {
		int rowcount = _deviceList.getRowCount();
		for(int i=0; i < rowcount; i++) {
			if(_networkListModel.getValueAt(i, 0).equals(address))
				return;
		}
		_networkListModel.addRow(new Object[] {address});
	}
	
	//Network device Table Model
	private class NetworkDeviceTableModel extends DefaultTableModel {
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	//Network device Table renderer
	private class NetworkDeviceRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
			if(value instanceof InetAddress) {
				InetAddress address = (InetAddress) value;
				String name = address.getHostName();
				if(name != null && name.length() > 0)
					value = name;
				else
					value = address.getHostAddress();
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
	
	//Prefix Length ComboBox model
	private class PrefixLengthModel extends DefaultComboBoxModel {
		private int _numOfBits;
		
		protected PrefixLengthModel(int numOfBits) {
			_numOfBits = numOfBits;
		}
		
		  @Override
		    public void setSelectedItem(Object obj) {
		        if (obj != null) {
		        	Long numOfHosts = NetworkScanner.getNumberOfHosts((Integer) obj, _numOfBits);
		        	if(numOfHosts <= NetworkScanner.NUM_OF_DEVICES_LIMIT) {
		        		super.setSelectedItem(obj);
		        	} 
		        } else {
		            super.setSelectedItem(obj);
		        }
		    }
	}
	
	//Prefix Length ComboBox renderer
	private class PrefixLengthRenderer extends DefaultListCellRenderer  {
		private int _numOfBits;
				
		protected PrefixLengthRenderer(int numOfBits) {
			_numOfBits = numOfBits;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			Long numOfHosts = NetworkScanner.getNumberOfHosts((Integer) value, _numOfBits);
			value = value + "(" + _prefixFormatter.format(numOfHosts) + " hosts)";
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(numOfHosts > NetworkScanner.NUM_OF_DEVICES_LIMIT) {
				comp.setEnabled(false);
			}
			return comp;
		}		
	}

}
