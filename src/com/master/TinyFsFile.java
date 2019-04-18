package com.master;

import java.util.ArrayList;

public class TinyFsFile {

	public String name;
	public String absPath;
	public ArrayList<String> chunkHandles;
	
	public TinyFsFile(String name, String absPath) {
		this.name = name;
		this.absPath = absPath;
		chunkHandles = new ArrayList<String>();
	}
	public TinyFsFile(String name) {
		this.name = name;
		chunkHandles = new ArrayList<String>();
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
	
	public ArrayList<String> getChunkHandles() {
		return chunkHandles;
	}

	public void setChunkHandles(ArrayList<String> handles) {
		this.chunkHandles = handles;
	}
	
}
