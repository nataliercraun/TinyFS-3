package com.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

import com.client.ClientFS.FSReturnVals;

public class ClientRec {

	static final int MAX_CHUNK_SIZE = 4096;
	
	/**
	 * Appends a record to the open file as specified by ofh Returns BadHandle
	 * if ofh is invalid Returns BadRecID if the specified RID is not null
	 * Returns RecordTooLong if the size of payload exceeds chunksize RID is
	 * null if AppendRecord fails
	 *
	 * Example usage: AppendRecord(FH1, obama, RecID1)
	 */
	public FSReturnVals AppendRecord(FileHandle ofh, byte[] payload, RID RecordID) {
		//Get last chunk in file
		List<String> chunkHandles = ofh.getHandles();
		
		if (chunkHandles.size() == 0) {
			return appendNewChunk(ofh, payload, RecordID);
		}
		String lastChunkHandle = chunkHandles.get(chunkHandles.size()-1);
		int numberRecords = getNumberRecords(lastChunkHandle);
		
		int offsetLastRecord = getOffsetOfRecord(lastChunkHandle, numberRecords);
		
		if (payload.length + offsetLastRecord + 4*(numberRecords+2) > MAX_CHUNK_SIZE) {
			return appendNewChunk(ofh, payload, RecordID);
		}
		
		byte[] newRecordNumber = ByteBuffer.allocate(4).putInt(numberRecords + 1).array();
		byte[] newRecordOffset = ByteBuffer.allocate(4).putInt(payload.length+offsetLastRecord).array();
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(lastChunkHandle, "rw");
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		try {
			raf.seek(0);
			raf.write(newRecordNumber, 0, 4);
			raf.seek(offsetLastRecord);
			raf.write(payload, 0, payload.length);
			raf.seek(MAX_CHUNK_SIZE-4*(numberRecords+1));
			raf.write(newRecordOffset, 0, 4);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return FSReturnVals.Success;
	}
	
	private FSReturnVals appendNewChunk(FileHandle ofh, byte[] payload, RID RecordID) {
		if (payload.length > (MAX_CHUNK_SIZE-8)) {
			return FSReturnVals.RecordTooLong;
		}
		
		String newChunkHandle = ofh.getFP() + ofh.getHandles().size();
		ofh.chunkHandles.add(newChunkHandle);
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(newChunkHandle,"rw");
			raf.setLength((long) MAX_CHUNK_SIZE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		byte[] numberRecords = ByteBuffer.allocate(4).putInt(1).array();
		byte[] recordOffset = ByteBuffer.allocate(4).putInt(4+payload.length).array();
		
		try {
			raf.seek(0);
			raf.write(numberRecords, 0, 4);
			raf.seek(4);
			raf.write(payload, 0, payload.length);
			raf.seek(MAX_CHUNK_SIZE-4);
			raf.write(recordOffset, 0, 4);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return FSReturnVals.Success;
	}
	
	private int getOffsetOfRecord(String chunkHandle, int recordNumber) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(chunkHandle,"rw");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] data = new byte[4];
		try {
			raf.seek(MAX_CHUNK_SIZE - 4*(recordNumber));
			raf.read(data, 0, 4);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		int offset = ByteBuffer.wrap(data).getInt();
		return offset;
	}
	
	//Helper function to get number of records in a chunk
	private int getNumberRecords(String chunkHandle) {
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(chunkHandle, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] data = new byte[4];
		try {
			raf.seek(0);
			raf.read(data, 0, 4);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int numberRecords = ByteBuffer.wrap(data).getInt();
		return numberRecords;
	}

	/**
	 * Deletes the specified record by RecordID from the open file specified by
	 * ofh Returns BadHandle if ofh is invalid Returns BadRecID if the specified
	 * RID is not valid Returns RecDoesNotExist if the record specified by
	 * RecordID does not exist.
	 *
	 * Example usage: DeleteRecord(FH1, RecID1)
	 */
	public FSReturnVals DeleteRecord(FileHandle ofh, RID RecordID) {
		String chunkHandle = RecordID.ChunkHandle;
		int index = RecordID.index;
		List<String> chunkHandles = ofh.chunkHandles;
		
		if (!chunkHandles.contains(chunkHandle)) {
			return FSReturnVals.BadHandle;
		}
		
		int numberRecords = getNumberRecords(chunkHandle);
		if (index > (numberRecords-1)) {
			return FSReturnVals.RecDoesNotExist;
		}
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(chunkHandle,"rw");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] recordOffset = new byte[4];
		try {
			raf.seek(MAX_CHUNK_SIZE - 4*(index+1));
			raf.read(recordOffset, 0, 4);
			int recOffsetInt = ByteBuffer.wrap(recordOffset).getInt();
			
			//If offset already negative, means record has already been deleted and does not exist
			if (recOffsetInt < 0) {
				raf.close();
				return FSReturnVals.RecDoesNotExist;
			}
			
			int newInvalidOffset = recOffsetInt*(-1);
			byte[] newInvalidRecordOffset = ByteBuffer.allocate(4).putInt(newInvalidOffset).array();
			raf.seek(MAX_CHUNK_SIZE - 4*(index+1));
			raf.write(newInvalidRecordOffset, 0, newInvalidRecordOffset.length);
			raf.close();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		
		return FSReturnVals.Success;
	}

	/**
	 * Reads the first record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadFirstRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadFirstRecord(FileHandle ofh, TinyRec rec){
		//TODO:check for badhandle
		
		String firstChunkHandle = "";
		// get the right chunkhandle
		if (ofh.getHandles().size() > 0) {
			firstChunkHandle = ofh.getHandles().get(0);
		} else {
			// file is empty 
			return FSReturnVals.RecDoesNotExist;
		}
				
		RandomAccessFile raf = null;
		byte[] payload = null;
		
		try {
			raf = new RandomAccessFile(firstChunkHandle, "rw");
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		try {
			
			// Calculate payload length 
			byte[] endOfP1 = new byte[4];
			raf.seek(MAX_CHUNK_SIZE-4);
			raf.read(endOfP1, 0, 4);
			
			int endofpayloadOneInt = ByteBuffer.wrap(endOfP1).getInt();
			
			// Check if record has been deleted 
			if (endofpayloadOneInt < 0) {
				// Call helper function with filehandle, chunkhandle, index (for RID), and record
				return ReadNextRecordHelper(ofh, firstChunkHandle, 0, rec);
			}
			
			// Subtract the 4 bytes that store the # of records 
			int payloadLength = endofpayloadOneInt - 4; 
			
			payload = new byte[payloadLength];
			
			// Seek to end of the 4 bytes that store the # of records 
			raf.seek(4);
			
			// Read payload 
			raf.read(payload, 0, payloadLength);
			
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Construct RID w/ index of 0 and assign it to the record 
		rec.setPayload(payload);
		RID rid = new RID(firstChunkHandle, 0);
		rec.setRID(rid);
				
		return FSReturnVals.Success;
	}
	
	public FSReturnVals ReadNextRecordHelper(FileHandle ofh, String chunkHandle, Integer index, TinyRec rec) {
		RID rid = new RID(chunkHandle, index);
		return ReadNextRecord(ofh, rid, rec);
	}

	/**
	 * Reads the last record of the file specified by ofh into payload Returns
	 * BadHandle if ofh is invalid Returns RecDoesNotExist if the file is empty
	 *
	 * Example usage: ReadLastRecord(FH1, tinyRec)
	 */
	public FSReturnVals ReadLastRecord(FileHandle ofh, TinyRec rec){
		
		//check badhandle
		//check recdoesnotexist
		
		
		List<String> chunkHandles = ofh.getHandles(); 
		String lastChunkHandle = chunkHandles.get(chunkHandles.size()-1);
		int numberRecords = getNumberRecords(lastChunkHandle);
		int offsetLastRecord = getOffsetOfRecord(lastChunkHandle, numberRecords);
		int offsetPrevRecord = getOffsetOfRecord(lastChunkHandle,numberRecords-1);
		
		if(offsetPrevRecord < 0) {
			offsetPrevRecord = -offsetPrevRecord;
		}
		int length_payload = offsetLastRecord - offsetPrevRecord;
		byte[] data = new byte[length_payload];
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(lastChunkHandle, "r");
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		try {
			
			raf.seek(MAX_CHUNK_SIZE - numberRecords*4); //
			//if that's negative then need to call prevrecord
			byte[] endof = new byte[4];
			raf.read(endof, 0, 4);
			int endofint = ByteBuffer.wrap(endof).getInt();
			if(endofint < 0) {
				RID pivot_rid = new RID(lastChunkHandle, numberRecords-1);
				return ReadPrevRecord(ofh, pivot_rid, rec); 
			}
			
			raf.seek(offsetPrevRecord);
			raf.read(data, 0, length_payload);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		rec.setPayload(data);
		RID rid = new RID();
		rid.ChunkHandle = lastChunkHandle;
		rid.index = numberRecords;
		rec.setRID(rid);
		
		return FSReturnVals.Success;
	}

	/**
	 * Reads the next record after the specified pivot of the file specified by
	 * ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadFirstRecord(FH1, tinyRec1) 2. ReadNextRecord(FH1,
	 * rec1, tinyRec2) 3. ReadNextRecord(FH1, rec2, tinyRec3)
	 */
	public FSReturnVals ReadNextRecord(FileHandle ofh, RID pivot, TinyRec rec){
		int currentIndex = pivot.index + 1;
		String chunkHandle = pivot.ChunkHandle;
		int numberRecords = getNumberRecords(chunkHandle);
		
		//If already at last index of last chunk for pivot, return rec does not exist since at end of file
		if ( (currentIndex+1) > numberRecords) {
			List<String> chunkHandles = ofh.getHandles();
			for (int i = 0; i < chunkHandles.size(); i++) {
				if (chunkHandles.get(i).equals(chunkHandle)) {
					if (i == (chunkHandles.size()-1)) {
						return FSReturnVals.RecDoesNotExist;
					}
					else {
						chunkHandle = chunkHandles.get(i+1);
					}
				}
			}
		}
		
		int currentOffset = getOffsetOfRecord(chunkHandle, currentIndex+1);
		//If current record is invalid (has been deleted) call get next record again
		if (currentOffset < 0) {
			pivot.ChunkHandle = chunkHandle;
			pivot.index = currentIndex;
			return ReadNextRecord(ofh, pivot, rec);
		}
		
		rec.setPayload(getRecordPayload(chunkHandle, currentIndex));
		RID r = new RID();
		r.index = currentIndex;
		r.ChunkHandle = chunkHandle;
		rec.setRID(r);
		return FSReturnVals.Success;
	}
	
	private byte[] getRecordPayload(String chunkHandle, int index) {
		
		int payloadSize;
		int startOfPayload;
		if (index == 0) {
			startOfPayload = 4;
			payloadSize = getOffsetOfRecord(chunkHandle, index+1) - 4;
		}
		else {
			int	prevRecordOffset = getOffsetOfRecord(chunkHandle, index);
			int currRecordOffset = getOffsetOfRecord(chunkHandle, index+1);
			payloadSize = currRecordOffset - prevRecordOffset;
			startOfPayload = prevRecordOffset;
		}
		
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(chunkHandle, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] payload = new byte[payloadSize];
		try {
			raf.seek(startOfPayload);
			raf.read(payload, 0, payloadSize);
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return payload;
		
	}

	/**
	 * Reads the previous record after the specified pivot of the file specified
	 * by ofh into payload Returns BadHandle if ofh is invalid Returns
	 * RecDoesNotExist if the file is empty or pivot is invalid
	 *
	 * Example usage: 1. ReadLastRecord(FH1, tinyRec1) 2. ReadPrevRecord(FH1,
	 * recn-1, tinyRec2) 3. ReadPrevRecord(FH1, recn-2, tinyRec3)
	 */
	public FSReturnVals ReadPrevRecord(FileHandle ofh, RID pivot, TinyRec rec){
		int currentIndex = pivot.index - 1;
		String chunkHandle = pivot.ChunkHandle;
		
		//If already at last index of last chunk for pivot, return rec does not exist since at end of file
		if ( (currentIndex-1) < 0) {
			List<String> chunkHandles = ofh.getHandles();
			for (int i = 0; i < chunkHandles.size(); i++) {
				if (chunkHandles.get(i).equals(chunkHandle)) {
					if (i == 0) {
						return FSReturnVals.RecDoesNotExist;
					}
					else {
						chunkHandle = chunkHandles.get(i-1);
						int numberRecords = getNumberRecords(chunkHandle);
						currentIndex = numberRecords - 1;
					}
				}
			}
		}
		
		
		
		int currentOffset = getOffsetOfRecord(chunkHandle, currentIndex+1);
		//If current record is invalid (has been deleted) call get next record again
		if (currentOffset < 0) {
			pivot.ChunkHandle = chunkHandle;
			pivot.index = currentIndex;
			return ReadPrevRecord(ofh, pivot, rec);
		}
		
		rec.setPayload(getRecordPayload(chunkHandle, currentIndex));
		RID r = new RID();
		r.index = currentIndex;
		r.ChunkHandle = chunkHandle;
		rec.setRID(r);
		return FSReturnVals.Success;
	}

}
