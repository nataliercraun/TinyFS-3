package com.master;

import com.client.ClientFS.FSReturnVals;
import java.util.ArrayList;
import java.util.List;

public class TinyFsDir {
	public TinyFsDir parentDir;
	public String absPath;
	public String dirName;
	public List<TinyFsDir> subDirs;
	public List<TinyFsFile> files;
	
	public TinyFsDir(TinyFsDir parentDir, String absPath, String dirName) {
		this.parentDir = parentDir;
		
		//Add this to parents subDirs list
//		parentDir.subDirs.add(this); TODO: parent should be updated in directory constructor (somehow)
		this.absPath = absPath;
		this.dirName = dirName;
		
		subDirs = new ArrayList<TinyFsDir>();
		files = new ArrayList<TinyFsFile>();
	}
	
	public FSReturnVals deleteFile(String fName) {
		for(int i = 0;  i < files.size(); i++) {
			if (files.get(i).name.equals(fName)) {
				files.remove(i);
				return FSReturnVals.Success; 
			}
		}
		
		return FSReturnVals.FileDoesNotExist;
	}
	
	public void deleteSubDirs() {
		for (int i = 0; i < subDirs.size(); i++) {
			subDirs.remove(i);
		}
	}
	
	public boolean deleteSubDir(String dirName) {
		for (int i = 0; i < subDirs.size(); i++) {
			if (subDirs.get(i).dirName.equals(dirName)) {
				subDirs.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public void rename(String name) {
		dirName = name;
		absPath = parentDir.getAbsPath() + "/" + name;
	}
	
	/**
	 * Returns List<String> of absolute paths of all files and subdirs contained within this directory
	 * @return
	 */
	public List<String> getContent() {
		List<String> content = new ArrayList<String>();
		
		for (int i = 0; i < files.size(); i++) {
			content.add(files.get(i).getAbsPath());
		}
		for (int i = 0; i < subDirs.size(); i++) {
			content.add(subDirs.get(i).getAbsPath());
			System.out.println("added this: "+ subDirs.get(i).getAbsPath());
		}
		System.out.println("current directory file size: "+ subDirs.size());
		return content;
	}
	
	/***Getters and Setters***/
	
	public TinyFsDir getParentDir() {
		return parentDir;
	}

	public void setParentDir(TinyFsDir parentDir) {
		this.parentDir = parentDir;
	}

	public String getAbsPath() {
		return absPath;
	}

	public void setAbsPath(String absPath) {
		this.absPath = absPath;
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	public List<TinyFsDir> getSubDirs() {
		return subDirs;
	}

	public void setSubDirs(List<TinyFsDir> subDirs) {
		this.subDirs = subDirs;
	}

	public List<TinyFsFile> getFiles() {
		return files;
	}

	public void setFiles(List<TinyFsFile> files) {
		this.files = files;
	}
	

}
