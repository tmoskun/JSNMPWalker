package com.ezcode.jsnmpwalker.search;


import java.util.Map;
import java.util.TreeMap;

import com.ezcode.jsnmpwalker.dialog.MibTextSearchDialog;
import com.ezcode.jsnmpwalker.panel.MibViewPanel;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public interface SearchableText extends Searchable {
	
	public boolean isRegex();
	
	public void setRegex(boolean isRegex);
	
	public boolean isCaseSensitive();
	
	public void setCaseSensitivity(boolean isCaseSensitive);
	
	public String getSearchText();
	
	public TreeMap<Integer, Integer> getSearchPositions();
	
	public int getCurrentSearchPosition();

		
}
