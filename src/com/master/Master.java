package com.master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.client.ClientFS.FSReturnVals;

public class Master {
	private tinyFsDir root;
	private Map<String, tinyFsDir> directories;
	
	public Master() {
		this.root = new tinyFsDir(null, "/", null);
		directories = new HashMap<String, tinyFsDir>();
		directories.put("/",root);
	}
	
	public FSReturnVals createDir(String srcDir, String dirName) {
		if (directories.get(dirName) != null) {
			return FSReturnVals.DestDirExists;
		}
		
		tinyFsDir parent = getParent(srcDir);
				//directories.get(srcDir);
		if (parent == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		tinyFsDir newDir = new tinyFsDir(parent, parent.getAbsPath() + "/" + dirName, dirName); 
		directories.put(dirName, newDir);
		newDir.setParentDir(parent); // TODO: parent should be updated in directory constructor (somehow)
		
		return FSReturnVals.Success;
	}
	
	public String[] ListDir(String tgt) {
		
		tinyFsDir thisDir = directories.get(getParent2(tgt));
		
		if (thisDir == null) {
			return null; // TODO: 
		}
		List<String> content = thisDir.getContent(); 
		if (content.size() == 0) {
			return null; 
		}
		String[] contentArray = new String[content.size()]; 
		for (int i = 0; i < content.size(); i++) {
			contentArray[i] = content.get(i); 
		}
		
		return contentArray; 
	}
	
	public FSReturnVals deleteDir(String srcDir, String dirName) {
		tinyFsDir toDelete = directories.get(dirName);
		if ( toDelete == null) {
			return FSReturnVals.Success;
		}
		tinyFsDir parent = directories.get(srcDir);
		if (parent == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		
		//Delete this directory from parent's list of dirs
		List<tinyFsDir> parentSubDirs = parent.getSubDirs();
		for (int i = 0; i < parentSubDirs.size(); i++) {
			if (parentSubDirs.get(i).equals(toDelete) ) {
				parentSubDirs.remove(i);
			}
		}
		
		//Delete this directory's subdirs
		toDelete.deleteSubDirs();
		
		//Delete this directory
		directories.remove(dirName);
		
		return FSReturnVals.Success;
	}
	
	private tinyFsDir getParent(String srcDir) { //TODO: not ours
		if (srcDir.equals("/")) {
			return root;
		}
		
		String[] partsArr = srcDir.split("/");
		
		ArrayList<String> parts = new ArrayList<>(Arrays.asList(partsArr));
		ArrayList<String> toRemove = new ArrayList<>();
		for (String s : parts) {
			if (s.equals("")) {
				toRemove.add(s);
			}
		}
		
		for (int i = 0; i < toRemove.size(); i++) {
			parts.remove(toRemove.get(i));
		}
		
		return directories.get(parts.get(parts.size() - 1));
	}
	
	private String getParent2(String srcDir) { //TODO: 
		if (srcDir.equals("/")) {
			return "/";
		}
		
		String[] partsArr = srcDir.split("/");
		
		ArrayList<String> parts = new ArrayList<>(Arrays.asList(partsArr));
	
		
		return parts.get(parts.size()-1);
	}
}
