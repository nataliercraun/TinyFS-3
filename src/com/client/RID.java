package com.client;

public class RID {
	public String ChunkHandle;
	public int index;
	
	public RID() {
	}
	
	public RID(String ChunkHandle, int index) {
		this.ChunkHandle = ChunkHandle;
		this.index = index;
	}
	
	public void setIndex(Integer index) {
		this.index = index; 
	}
	
	public void setChunkHandle(String chunkHandle) {
		this.ChunkHandle = chunkHandle; 
	}
	
}
