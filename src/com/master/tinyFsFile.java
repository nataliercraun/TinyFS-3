package com.master;

import java.util.ArrayList;

public class tinyFsFile {

	public String name;
	public String absPath;
	public ArrayList<String> handles;
	
	public tinyFsFile(String name, String absPath) {
		this.name = name;
		this.absPath = absPath;
		handles = new ArrayList<String>();
	}
	public tinyFsFile(String name) {
		this.name = name;
		handles = new ArrayList<String>();
	}
	

	/*** Setters and getters ***/
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbsPath() {
		return absPath;
	}

	public void setAbsPath(String absPath) {
		this.absPath = absPath;
	}
	
	public ArrayList<String> getHandles() {
		return handles;
	}

	public void setHandles(ArrayList<String> handles) {
		this.handles = handles;
	}
	
}
