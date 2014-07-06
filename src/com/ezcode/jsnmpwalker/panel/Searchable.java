package com.ezcode.jsnmpwalker.panel;


import java.util.TreeMap;

import com.ezcode.jsnmpwalker.dialog.SearchDialog;

/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */

public interface Searchable {
	
	public void resetSearch(boolean clearPrevious);
	
	public void enableSearchControls(boolean isrun);
		
	public String getCurrentSearchKey();
	
	public void setCurrentSearchKey(String searchKey);
	
	public String getSearchText();
	
	public TreeMap<Integer, Integer> getSearchPositions();
	
	public boolean hasSearchResults();
	
	public int getCurrentSearchPosition();
	
	public void showResults(TreeMap<Integer, Integer> searchPositions, int currentPosition);
	
	public void moveToNext();
	
	public void moveToPrevious();
	
}
