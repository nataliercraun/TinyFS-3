package com.client;

import java.util.ArrayList;
import java.util.List;

public class FileHandle {

	//has chunks in the file
	
	String filepath;
	List<String> chunkHandles;
	
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
	
	
	public void setHandles(List<String> fileChunks) {
		this.chunkHandles = fileChunks;
	}
	
	public List<String> getHandles() {
		return chunkHandles;
	}
}
