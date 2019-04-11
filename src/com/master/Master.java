package com.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.client.ClientFS.FSReturnVals;

public class Master {
	private tinyFsDir root;
	private Map<String, tinyFsDir> directories;
	
	public Master(String rootPath) {
		this.root = new tinyFsDir(null, "/", null);
		directories = new HashMap<String, tinyFsDir>();
	}
	
	public FSReturnVals createDir(String srcDir, String dirName) {
		if (directories.get(dirName) != null) {
			return FSReturnVals.DestDirExists;
		}
		tinyFsDir parent = directories.get(srcDir);
		if (parent == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		tinyFsDir newDir = new tinyFsDir(parent, parent.getAbsPath() + "/" + dirName, dirName); 
		directories.put(dirName, newDir);
		newDir.setParentDir(parent); // TODO: parent should be updated in directory constructor (somehow)
		
		return FSReturnVals.Success;
	}
	
	public String[] ListDir(String tgt) {
		tinyFsDir thisDir = directories.get(tgt); 
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
	
	
}
