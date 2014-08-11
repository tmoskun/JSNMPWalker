package com.ezcode.jsnmpwalker.search;

import javax.swing.tree.TreePath;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public interface SearchableTree extends Searchable {
	
	public boolean isCurrentInludeText();
	
	public void setCurrentIncludeText(boolean isIncludeText);
	
	public TreePath getCurrentSearchPosition();
	
}
