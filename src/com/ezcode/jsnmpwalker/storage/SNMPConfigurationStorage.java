package com.ezcode.jsnmpwalker.storage;
/**
 * Copyright(c) 2012-2013 
 * @author tmoskun, ezcode (Zlatco)
 * This Software is distributed under GPLv3 license
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SNMPConfigurationStorage {
	private DocumentBuilder _docBuilder;
	private String _path = null;
	
	public SNMPConfigurationStorage() {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			_docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Can't create a document builder");
			e.printStackTrace();
		}
	}
	
	public String getPath() {
		return _path;
	}
	
	private void setPath(String filename) {
		_path = filename;
	}
	
	public boolean saveConfiguration(TreeModel treeModel, Map<String, String> options) {
		return saveConfiguration(treeModel, options, _path);
	}
	
	public boolean saveConfiguration(TreeModel treeModel, Map<String, String> options, String filename) {
		if(_docBuilder != null && filename != null) {
						
			Document doc = _docBuilder.newDocument();
			
			//the tree			
			Element rootElement = doc.createElement("Root");
			doc.appendChild(rootElement);
				
			Element treeElement = doc.createElement("Tree");
			rootElement.appendChild(treeElement);
			
			TreeNode root = (TreeNode) treeModel.getRoot();
			Enumeration commands = root.children();
			while(commands.hasMoreElements()) {
				TreeNode command = (TreeNode) commands.nextElement();
				Element commandElement = doc.createElement("Command");
				commandElement.setAttribute("data", command.toString());
				treeElement.appendChild(commandElement);
				
				Enumeration ips = command.children();
				while(ips.hasMoreElements()) {
					TreeNode ip = (TreeNode) ips.nextElement();
					Element ipElement = doc.createElement("Ip");
					ipElement.setAttribute("data", ip.toString());
					commandElement.appendChild(ipElement);
					
					Enumeration oids = ip.children();
					while(oids.hasMoreElements()) {
						TreeNode oid = (TreeNode) oids.nextElement();
						Element oidElement = doc.createElement("Oid");
						oidElement.setAttribute("data", oid.toString());
						ipElement.appendChild(oidElement);
					}
				}
			}
			
			//options
			Element optionsElement = doc.createElement("Options");
			rootElement.appendChild(optionsElement);
			for(String opt: options.keySet()) {
				Element el = doc.createElement("Option");
				el.setAttribute("name", opt);
				el.setAttribute("value", options.get(opt));
				optionsElement.appendChild(el);
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			try {
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(new FileOutputStream(filename));
				transformer.transform(source, result);
				setPath(filename);
				return true;
			} catch (TransformerConfigurationException e) {
				System.out.println("Can't configure the document");
				e.printStackTrace();
			} catch (TransformerException e) {
				System.out.println("Can't save the document");
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				System.out.println("Can't find file");
				e.printStackTrace();
			}			
		}
		return false;
	}
		
	public boolean readConfiguration(TreeModel treeModel, Map<String, String> options, String filename) {
		if(_docBuilder != null) {
			try {
				File fXmlFile = new File(filename);
				Document doc = _docBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();
				
				//the tree
				TreeNode root = (TreeNode) treeModel.getRoot();
							
				NodeList commandList = doc.getElementsByTagName("Command");			
				for (int i = 0; i < commandList.getLength(); i++) {				 
					Node commandNode = commandList.item(i);
					if (commandNode.getNodeType() == Node.ELEMENT_NODE) {
						Element commandElement = (Element) commandNode;	
						String command = commandElement.getAttribute("data");	
						DefaultMutableTreeNode commTreeNode = new DefaultMutableTreeNode(command);
						((DefaultTreeModel) treeModel).insertNodeInto(commTreeNode, (MutableTreeNode) root, treeModel.getChildCount(root));
						
						NodeList ipList = commandElement.getElementsByTagName("Ip");
						for(int j = 0; j < ipList.getLength(); j++) {
							Node ipNode = ipList.item(j);
							if (ipNode.getNodeType() == Node.ELEMENT_NODE) {
								Element ipElement = (Element) ipNode;
								String ip = ipElement.getAttribute("data");
								DefaultMutableTreeNode ipTreeNode = new DefaultMutableTreeNode(ip);
								((DefaultTreeModel) treeModel).insertNodeInto(ipTreeNode, (MutableTreeNode) commTreeNode, treeModel.getChildCount(commTreeNode));								
								
								NodeList oidList = ipElement.getElementsByTagName("Oid");
								for(int k = 0; k < oidList.getLength(); k++) {
									Node oidNode = oidList.item(k);
									if(oidNode.getNodeType() == Node.ELEMENT_NODE) {
										Element oidElement = (Element) oidNode;
										String oid = oidElement.getAttribute("data");
										DefaultMutableTreeNode oidTreeNode = new DefaultMutableTreeNode(oid);
										((DefaultTreeModel) treeModel).insertNodeInto(oidTreeNode, (MutableTreeNode) ipTreeNode, treeModel.getChildCount(ipTreeNode));
									}
								}
							}
							
						}			 
					}
				}
				
				//options
				NodeList optionList = doc.getElementsByTagName("Option");
				for(int i = 0; i < optionList.getLength(); i++) {
					Node optionNode = optionList.item(i);
					if(optionNode.getNodeType() == Node.ELEMENT_NODE) {
						Element optionElement = (Element) optionNode;
						String optName = optionElement.getAttribute("name");
						String optValue = optionElement.getAttribute("value");					
						options.put(optName, optValue);
					}
				}
				setPath(filename);
				return true;
			} catch (Exception e) {
				System.out.println("Can't read file " + filename);
				e.printStackTrace();
		    }
		}
		return false;
	}

}
