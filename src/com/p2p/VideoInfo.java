/* ---------------------------------------------
VideoInfo Class
Last updated: Wednesday, 27th Nov 2013

Definition of class which stores an Arraylist:
- Completeness status of each BLOCK

------------------------------------------------ */

package com.p2p;

import java.util.ArrayList;

public class VideoInfo {

	private ArrayList <Boolean> blockStatus;
	
	public VideoInfo(int numBlock) {
		
		blockStatus = new ArrayList <Boolean>();
		
		// Initialise boolean arraylist for blocks
		this.initBlockStatus(numBlock);
				
	} // end specific constructor
	
	private void initBlockStatus(int numBlock) {
	
		// Add N elements representing completeness of each block
		// to list. Set boolean flag for each block to false
		for (int i=0; i < numBlock; i++) {
			this.blockStatus.add(false);
		} // endfor
		
	} // end initBlockStatus
	
	// Return a byte array indicating completeness of each block
	public byte[] getBlockStatus() {
		
		byte[] bStatus = new byte[this.blockStatus.size()];
		
		for (int i = 0; i < bStatus.length; i++) {
			
			if (this.blockStatus.get(i))
				bStatus[i] = 1;
			else
				bStatus[i] = 0;
			
		} // endfor
		
		return bStatus;
		
	} // end getBlockStatus
	
	// Determine if a certain block is complete
	public boolean hasCompleteBlock(int blockNum) {
		return this.blockStatus.get(blockNum);
	} // end hasCompleteBlock
		
} // end VideoInfo class
