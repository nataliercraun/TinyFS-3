package com.master;

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
		directories.put(dirName, new tinyFsDir(parent, parent.getAbsPath() + "/" + dirName, dirName));
		return FSReturnVals.Success;
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
