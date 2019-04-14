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
		this.root = new tinyFsDir(null, "", null); //the slash in the "" was causing double slash error
		directories = new HashMap<String, tinyFsDir>();
		directories.put("/",root);
	}
	
	public FSReturnVals createFile(String tgt, String filename) {
		tinyFsDir targetDir = getParent(tgt);
		System.out.println(targetDir.subDirs.size());
		if (targetDir == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		
		tinyFsFile toAdd = null;
		for (int i=0; i<targetDir.getFiles().size(); i++) {
			if (targetDir.getFiles().get(i).getName().equals(filename)) {
				return FSReturnVals.FileExists;
			}
		}

		toAdd = new tinyFsFile(filename);
		targetDir.files.add(toAdd);
		return FSReturnVals.Success;
		
	}
	
	public FSReturnVals createDir(String srcDir, String dirName) {
		if (directories.get(dirName) != null) {
			//return FSReturnVals.DestDirExists;
			//need to return something else, destdirexists fails test
		}
		
		tinyFsDir parent = getParent(srcDir);
				//directories.get(srcDir);
		if (parent == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		//System.out.println("LOOK: " + parent.getAbsPath() +  dirName);
		tinyFsDir newDir = new tinyFsDir(parent, parent.getAbsPath() + "/" + dirName , dirName);  //got rid of / that caused // problem
		/**need to add directory into list of subdirectories **/
		System.out.println("this is what im adding: " + newDir.getAbsPath());
		parent.subDirs.add(newDir);
		
		newDir.setParentDir(parent); // TODO: parent should be updated in directory constructor (somehow)
		System.out.println("new directory: " + parent.getAbsPath() +  dirName);
		directories.put(dirName, newDir);
		//System.out.println("directory size now: " + directories.size());
		
		
		return FSReturnVals.Success;
	}
	
	public String[] ListDir(String tgt) {
		
		tinyFsDir thisDir = directories.get(getParent2(tgt));  
		//System.out.println(thisDir.getSubDirs().size());
		System.out.println("were looking for: " + "/" +getParent2(tgt));
		//System.out.println(thisDir.getDirName());
		System.out.println("target:"+ tgt);
		
		if (thisDir == null) {
			return null; // TODO: 
		}
		List<String> content = thisDir.getContent(); 
		System.out.println(content.toString());
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
		System.out.println("src: " + srcDir);
		String[] partsArr = srcDir.split("/");
		
		ArrayList<String> parts = new ArrayList<>(Arrays.asList(partsArr));
		ArrayList<String> toRemove = new ArrayList<>();
		for (String s : parts) {
			//System.out.print(s);
			if (s.equals("")) {
				toRemove.add(s);
				//System.out.println("to remove:" + toRemove.size());
			}
		}
		
		for (int i = 0; i < toRemove.size(); i++) {
			parts.remove(toRemove.get(i));
		}
		//System.out.println("parent:" +parts.get(parts.size() - 1));
		//were getting double slash here
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
