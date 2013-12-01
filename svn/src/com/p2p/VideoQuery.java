/* ---------------------------------------------

VideoQuery Class
Last updated: Friday, 29th Nov 2013

Class defining format for Video Queries
1. Video query to find peers able to service request
2. Video query to get actual data from a known peer

------------------------------------------------ */

package com.p2p;

import java.io.Serializable;
import java.util.ArrayList;

//Implements "Serializable" allows you to send packet as an object
//without the need to break it down into byte buffers
public class VideoQuery implements Serializable {

	private final String HEADER = GlobalVar.HEADER;

	// Required to remove warnings
	private static final long serialVersionUID = GlobalVar.TYPE_VIDQUERY;
	
	private String videoName;
	private int reqBlock;
	private String srcIP;
	private String dstIP;
	private ArrayList <String> ipPath;
	
	// Constructor 1: To be used for seeking (i.e. no clue about who has a file)
	public VideoQuery(String ip, String name, int block) {
		
		srcIP = ip;
		dstIP = null;
		
		videoName = name;
		reqBlock = block;
		ipPath = new ArrayList <String>();
		
	} // end specific constructor 1

	// Constructor 2: To be used for an actual video request for a known source
	public VideoQuery(String srcip, String dstip, String name, int block, ArrayList <String> path) {

		srcIP = srcip;
		dstIP = dstip;

		// Set the arraylist of nodes to be traversed in path to destination
		// This should be taken from the initial VideoQuery where the destination is unknown
		ipPath = path;
		
		videoName = name;
		reqBlock = block;
		
	} // end specific constructor 2
	
	public String getVideoName() {
		return videoName;
	} // end getVideoName
	
	public String getSrcIP() {
		return srcIP;
	} // end getSrcIP
	
	public int getReqBlock() {
		return reqBlock;
	} // end getReqBlock
	
	public ArrayList <String> getIPPath() {
		return this.ipPath;
	} // end getIPPath
	
	// Checks if the targeted IP is an IP of a peer already visited on path
	public boolean isVisitedIP(String ip) {
		
		if (this.ipPath.indexOf(ip) == -1)
			return false;
		
		return true;
					
	} // end isVisitedIP
	
	// Adds IP of current node to end of arraylist of visited nodes
	public void insertIP(String ip) {
		this.ipPath.add(ip);
	} // end insertIP
	
	// Tests if path list is empty
	public boolean isPathEmpty() {
		return this.ipPath.isEmpty();
	} // end isPathEmpty
	
	// Check if this is an actual video GET query (destination known)
	public boolean isGetQuery() {
		
		if (this.dstIP == null)
			return false;
		
		return true;
		
	} // end isGetQuery

} // end class VideoQuery
