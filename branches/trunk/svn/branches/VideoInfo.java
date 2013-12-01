/* ---------------------------------------------
VideoInfo Class
Last updated: Saturday, 16th Nov 2013

Definition of class which stores 2 Arraylists:
1. Completeness status of each BLOCK
2. Completeness status of all PIECES per individual block

------------------------------------------------ */

package com.p2p;

import java.util.ArrayList;

public class VideoInfo {

	private ArrayList <Boolean> blockStatus;
	private ArrayList <ArrayList <Boolean>> pieceStatus;
	
	public VideoInfo(int numBlock, int numPiece) {
		
		blockStatus = new ArrayList <Boolean>();
		pieceStatus = new ArrayList <ArrayList <Boolean>>();
		
		// Initialise boolean arraylist for blocks
		this.initBlockStatus(numBlock);
		
		// Initialise boolean piecelist for pieces by block
		this.initPieceStatus(numBlock, numPiece);
		
	} // end specific constructor
	
	private void initBlockStatus(int numBlock) {
	
		// Add N elements representing completeness of each block
		// to list. Set boolean flag for each block to false
		for (int i=0; i < numBlock; i++) {
			this.blockStatus.add(false);
		} // endfor
		
	} // end initBlockStatus

	private void initPieceStatus(int numBlock, int numPiece) {
		
		// Add N elements representing completeness of each block
		// to list. Set boolean flag for each block to false
		for (int i=0; i < numBlock; i++) {
			
			ArrayList <Boolean> pieceList = new ArrayList <Boolean>();
			
			for (int j=0; j < numPiece; j++) {
				pieceList.add(false);
			} // endfor
			
			this.pieceStatus.add(pieceList);
			
		} // endfor
		
	} // end initPieceStatus
	
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
	
	// Determine if you have a certain piece for chosen block
	public boolean hasPiece(int blockNum, int pieceNum) {
		return this.pieceStatus.get(blockNum).get(pieceNum);
	} // end hasPiece
	
}
