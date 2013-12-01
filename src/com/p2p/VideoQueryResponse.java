/* ---------------------------------------------

VideoQueryResponse Class
Last updated: Friday, 29th Nov 2013

Class defining format for Response to Video Queries

------------------------------------------------ */

package com.p2p;

import java.io.Serializable;
import java.util.ArrayList;

//Implements "Serializable" allows you to send packet as an object
//without the need to break it down into byte buffers

public class VideoQueryResponse implements Serializable {

	private final String HEADER = GlobalVar.HEADER;

	// Required to remove warnings
	private static final long serialVersionUID = GlobalVar.TYPE_VIDQUERY_RESPONSE;
	
	private String videoName;
	private int reqBlock;
	private String srcIP;
	private ArrayList <String> ipPath;
	private int currPeerIndex;
	
	private byte[] buffer;
	
	// Constructor 1: Response after locating node which can service streaming request
	// The constructor should take in the fields from the received VideoQuery message
	public VideoQueryResponse(String ip, String name, int block, ArrayList <String> path) {
		
		srcIP = ip;
		videoName = name;
		reqBlock = block;
		ipPath = path;
		currPeerIndex = ipPath.size() - 1;
		
		// For this constructor, there is no file to be sent back
		buffer = null;
		
	} // end specific constructor 1

	// Constructor 2: Response with actual file chunk being sent back
	// The constructor should take in the fields from the received VideoQuery message
	public VideoQueryResponse(String ip, String name, int block, ArrayList <String> path, byte[] b) {
		
		srcIP = ip;
		videoName = name;
		reqBlock = block;
		ipPath = path;
		currPeerIndex = ipPath.size() - 1;
		
		// Buffer to contain contents of input byte array
		buffer = b;
		
	} // end specific constructor 1
	
	public String getVideoName() {
		return videoName;
	} // end getVideoName
	
	public String getSrcIP() {
		return srcIP;
	} // end getSrcIP
	
	public int getReqBlock() {
		return reqBlock;
	} // end getReqBlock
	
	// Get index of node on path list that current node is referring to
	public int getPeerIndex() {
		return currPeerIndex;
	} // end getPeerIndex
	
	// Get node on path list pointed by index
	public String getCurrPeer() {
		return ipPath.get(currPeerIndex);
	} // end getCurrPeer
	
	// Return byte buffer with file contents
	public byte[] getBuffer() {
		return buffer;
	} // end getBuffer
	
	// Decrement peer index by 1
	public void decrementPeerIndex() {
		
		if (this.currPeerIndex >= 0)
			this.currPeerIndex -= 1;
		
	} // end incPeerIndex

	// Check if this is an actual video GET response (to send file back)
	public boolean isGetResponse() {
		
		if (this.buffer == null)
			return false;
		
		return true;
		
	} // end isGetQuery
	
} // end class VideoQueryResponse
