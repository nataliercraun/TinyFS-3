package com.client;

import java.util.ArrayList;
import java.util.List;

public class FileHandle {

	//has chunks in the file
	
	String filepath;
	List<String> chunkHandles;
	boolean open;
	
	public FileHandle() {
		chunkHandles = new ArrayList<String>();
	}
	
	/*
	 * Setters/Getters
	 */
	
	public void setFP(String filePath) {
		this.filepath = filePath;
	}
	
	public String getFP() {
		return this.filepath;
	}
	
	public List<String> getHandles() {
		return chunkHandles;
	}

	public void setHandles(List<String> chunks) {
		this.chunkHandles = chunks;
		
	}
	
	public void setOpen(boolean open) {
		this.open = open;
	}
}
