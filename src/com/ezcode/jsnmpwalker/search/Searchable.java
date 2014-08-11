package com.ezcode.jsnmpwalker.search;


/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public interface Searchable {
	
	public void resetSearch(boolean clearPrevious);
	
	public void disableSearchControls(boolean isrun);
		
	public String getCurrentSearchKey();
	
	public void setCurrentSearchKey(String searchKey);
	
	public boolean hasSearchResults();
	
	public void findSearchResult() throws SearchResultNotFoundException;
	
	public void moveToNext() throws SearchResultNotFoundException;
	
	public void moveToPrevious() throws SearchResultNotFoundException;
	
	public void showResult();
	
}
