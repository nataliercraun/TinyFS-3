package com.master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.client.ClientFS.FSReturnVals;

public class Master {
	private TinyFsDir root;
	private Map<String, TinyFsDir> directories;
	
	public Master() {
		this.root = new TinyFsDir(null, "/", null);
		directories = new HashMap<String, TinyFsDir>();
		directories.put("",root);
	}
	
	public FSReturnVals createFile(String tgt, String filename) {
		TinyFsDir targetDir = directories.get(tgt);
		if (targetDir == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		
		TinyFsFile toAdd = null;
		for (int i=0; i<targetDir.getFiles().size(); i++) {
			if (targetDir.getFiles().get(i).getName().equals(filename)) {
				return FSReturnVals.FileExists;
			}
		}

		toAdd = new TinyFsFile(filename);
		targetDir.files.add(toAdd);
		return FSReturnVals.Success;
		
	}
	
	public FSReturnVals createDir(String srcDir, String dirName) {
		String absPath = srcDir + dirName;
		
		if (directories.get(absPath) != null) {
			return FSReturnVals.Success;
		}
		
		TinyFsDir parent = directories.get(srcDir.substring(0, srcDir.length() - 1));

		if (parent == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		TinyFsDir newDir = new TinyFsDir(parent, absPath , dirName);  //got rid of / that caused // problem
		/**need to add directory into list of subdirectories **/
		parent.subDirs.add(newDir);
		
		newDir.setParentDir(parent); // TODO: parent should be updated in directory constructor (somehow)
		directories.put(absPath, newDir);
		
		return FSReturnVals.Success;
	}
	
	public List<String> ListDirHelper(String tgt) {
		List<String> content = new ArrayList<String>();
		
		TinyFsDir thisDir = directories.get(tgt);
		if (thisDir == null) {
			return content;
		}
		for (TinyFsFile file: thisDir.getFiles()) {
			content.add(file.getAbsPath());
		}
		for (TinyFsDir dir: thisDir.getSubDirs()) {
			content.add(dir.getAbsPath());
			content.addAll(ListDirHelper(dir.getAbsPath()));
		}
		return content;
	}
	
	public String[] ListDir(String tgt) {
		List<String> content = ListDirHelper(tgt);
		return content.toArray(new String[content.size()]);
	}
	
	public FSReturnVals deleteDir(String srcDir, String dirName) {
		String absPath = srcDir + dirName;

		TinyFsDir toDelete = directories.get(absPath);
		if ( toDelete == null) {
			return FSReturnVals.Success;
		}
		
		TinyFsDir parent = directories.get(srcDir.substring(0, srcDir.length() - 1));
		if (parent == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		
		if (toDelete.getSubDirs().size() != 0) {
			return FSReturnVals.DirNotEmpty;
		}
		
		//Delete this directory from parent's list of dirs
		List<TinyFsDir> parentSubDirs = parent.getSubDirs();
		for (int i = 0; i < parentSubDirs.size(); i++) {
			if (parentSubDirs.get(i).equals(toDelete) ) {
				parentSubDirs.remove(i);
			}
		}
		
		//Delete this directory's subdirs
		toDelete.deleteSubDirs();
		
		//Delete this directory
		//directories.remove(dirName);
		directories.remove(absPath);
		
		return FSReturnVals.Success;
	}
	
	public FSReturnVals RenameDir(String src, String newName) {
	
		TinyFsDir newDir = directories.get(newName);
		
		if(newDir != null) {
			return FSReturnVals.DestDirExists;
		} else {
			directories.put(newName, directories.get(newName));
			directories.remove(src); 
			
		}
		return FSReturnVals.Success;
	}
	
}
