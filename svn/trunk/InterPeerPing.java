/* ---------------------------------------------

InterPeerPing Class
Last updated: Thursday, 28th Nov 2013

Message class for transfer of video list from peer to
neighbours as heartbeat

------------------------------------------------ */

package com.p2p;

import java.io.Serializable;
import java.util.Hashtable;

public class InterPeerPing implements Serializable{

	private final String HEADER = GlobalVar.HEADER;
	
	// Required to remove warnings
	private static final long serialVersionUID = GlobalVar.TYPE_INTERPEER_PING;
	
	private String srcIP;
	private Hashtable <String, VideoInfo> srcVideoList;
	
	// The message is made up of:
	// Unique Identifier
	// Message Type Field
	// Peer IP Address (Own IP)
	public InterPeerPing(String ip, Hashtable <String, VideoInfo> videos) {
		
		srcIP = ip;
		srcVideoList = videos;
		
	} // end specific constructor
	
	public String getHeader() {
		return this.HEADER;
	} // end getHeader();
	
	public long getType() {
		return InterPeerPing.serialVersionUID;
	} // end getType
	
	public String getSrcIP() {
		return this.srcIP;
	} // end getIP
	
	public Hashtable <String, VideoInfo> getSrcVideoList() {
		return this.srcVideoList;
	} // end getSrcVideoList
	
} // end class InterPeerPing
