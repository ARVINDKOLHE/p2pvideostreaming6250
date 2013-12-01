/* ---------------------------------------------
VideoInfo Class
Last updated: Saturday, 30th Nov 2013

Definition of class which stores an boolean array
indicating completeness status of each block

------------------------------------------------ */

package com.p2p;

public class VideoInfo {

	private boolean blockStatus[];
	
	public VideoInfo(int numBlock) {
		
		blockStatus = new boolean[numBlock];
		
		// Initialise boolean arraylist for blocks
		this.initBlockStatus(numBlock);
				
	} // end specific constructor
	
	private void initBlockStatus(int numBlock) {
	
		// Add N elements representing completeness of each block
		// to list. Set boolean flag for each block to false
		for (int i=0; i < numBlock; i++) {
			this.blockStatus[i] = false;
		} // endfor
		
	} // end initBlockStatus
	
	// Return a boolean array indicating completeness of each block
	public boolean[] getBlockStatus() {
		return this.blockStatus;
	} // end getBlockStatus
	
	// returns total number of blocks for the complete video
	public int getNumBlocks() {
		return this.blockStatus.length;
	} // end getNumBlocks
	
	// Count the number of complete blocks for this video
	public int countComplete() {
		
		int count = 0;
		
		for (int i = 0; i < this.blockStatus.length; i++) {
			
			// If status for block true; increment counter
			if (this.blockStatus[i])
				++count;
			
		} // endfor
		
		return count;
		
	} // end countComplete
	
	// Determine if a certain block is complete
	public boolean hasCompleteBlock(int blockNum) {
		return this.blockStatus[blockNum];
	} // end hasCompleteBlock
	
	// Set flag determining completeness of a particular block
	public void setBlock(boolean b, int index) {
		
		if (index < this.blockStatus.length)
			this.blockStatus[index] = b;
		
	} // end setBlock
		
} // end VideoInfo class