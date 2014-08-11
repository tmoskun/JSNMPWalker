package com.ezcode.jsnmpwalker.search;
/**
 * Copyright(c) 2014 
 * @author tmoskun
 * This Software is distributed under GPLv3 license
 */


public class SearchResultNotFoundException extends Exception { 
	
	public SearchResultNotFoundException(boolean more, String keyword) {
		super("No " + (more ? "more ":"") + "result found for '" + keyword + "'");
	}

}
