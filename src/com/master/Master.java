package com.master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.client.ClientFS.FSReturnVals;
import com.client.FileHandle;

public class Master {
	private TinyFsDir root;
	private Map<String, TinyFsDir> directories;
	private Map<String, TinyFsFile> files;
	private Map<String, FileHandle> fileHandles; //maps from abs path to file handle
	
	public Master() {
		files = new HashMap<String, TinyFsFile>();
		fileHandles = new HashMap<String, FileHandle>();
		this.root = new TinyFsDir(null, "/", null);
		directories = new HashMap<String, TinyFsDir>();
		directories.put("",root);
		
	}
	
	public FSReturnVals createFile(String tgt, String filename) {
		System.out.println("hell from createfile");
		TinyFsDir targetDir = directories.get(tgt.substring(0, tgt.length() - 1));
		if (targetDir == null) {
			return FSReturnVals.SrcDirNotExistent;
		}

		TinyFsFile toAdd = null;
		FileHandle toAdd2 = null; //for the file handle data structure
		for (int i=0; i<targetDir.getFiles().size(); i++) {
			if (targetDir.getFiles().get(i).getName().equals(filename)) {
				return FSReturnVals.FileExists;
			}
		}
		String absPath = tgt + filename;
		System.out.println("hell0 from createfile");
		toAdd = new TinyFsFile(filename, absPath);
		toAdd2 = new FileHandle();
		System.out.println("toadd" + toAdd.name);
		targetDir.files.add(toAdd);
		System.out.println("hell00 from createfile");
		//Adding file to files map in master
		
		System.out.println("abs path: " + absPath);
		
		toAdd2.setFP(absPath); //adding abspath to Filehandle
		System.out.println("hell000 from createfile");
		files.put(absPath, toAdd);
		fileHandles.put(absPath, toAdd2); //adding filehandle into filehandle map
		return FSReturnVals.Success;	
	}
	
	public FSReturnVals deleteFile(String tgt, String filename) {
		TinyFsDir targetDir = directories.get(tgt.substring(0, tgt.length() - 1));
		if (targetDir == null) {
			return FSReturnVals.SrcDirNotExistent;
		}
		
		TinyFsFile toDelete = null;
		for (int i=0; i<targetDir.getFiles().size(); i++) {
			if (targetDir.getFiles().get(i).getName().equals(filename)) {
				toDelete = targetDir.getFiles().get(i);
			}
		}
		
		if (toDelete == null) {
			return FSReturnVals.FileDoesNotExist;
		}

		targetDir.files.remove(toDelete);
		
		//Removing file from files map in master
		String absPath = tgt + filename;
		files.remove(absPath);
		fileHandles.remove(absPath);
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
		
		TinyFsDir srcDir = directories.get(src);
		
		if(directories.get(newName) != null) {
			return FSReturnVals.DestDirExists;
		} else {
			directories.remove(src); 
			directories.put(newName, srcDir);
			srcDir.rename(getEndOfPath(newName));
		}
		return FSReturnVals.Success;
	}
	
	public List<String> openFile(String filePath, FileHandle ofh) {
		TinyFsFile file = files.get(filePath);
		if (file == null) {
			return null;
		}
		FileHandle fh = fileHandles.get(filePath);
		ofh.setFP(fh.getFP());
		ofh.setHandles(fh.getHandles());
		ofh.setOpen(true);
		fileHandles.remove(filePath);
		fileHandles.put(filePath, ofh);
		return file.getChunkHandles();
	}
	public FSReturnVals CloseFile(FileHandle fh) {
		FileHandle curr_fh;
		String pathOfFH = fh.getFP();
		//parse to get x and y in /x/y 
		if(files.containsKey(pathOfFH)) {
			curr_fh = fileHandles.get(pathOfFH);
			if(curr_fh!=null) {
				curr_fh.setOpen(false);
				return FSReturnVals.Success;
			}
			return FSReturnVals.BadHandle;
		}
		return FSReturnVals.BadHandle;
	}
	
	private String getEndOfPath(String srcDir) {  
		if (srcDir.equals("/")) {
			return "/";
		}
		
		String[] partsArr = srcDir.split("/");
		
		ArrayList<String> parts = new ArrayList<>(Arrays.asList(partsArr));
		
		return parts.get(parts.size()-1);
	}
	
}
