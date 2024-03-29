/* ---------------------------------------------

PeerServerPing Class
Last updated: Sunday, 17th Nov 2013

Message class for object transfers from peer to
server as heartbeat

------------------------------------------------ */

package com.p2p;

import java.io.Serializable;

// Implements "Serializable" allows you to send packet as an object
// without the need to break it down into byte buffers
public class PeerServerPing implements Serializable {

	private final String HEADER = GlobalVar.HEADER;
	
	// Required to remove warnings
	private static final long serialVersionUID = GlobalVar.TYPE_PEERSERV_PING;
	
	private String ipAddr;

	// The message is made up of:
	// Unique Identifier
	// Message Type Field
	// Peer IP Address (Own IP)
	public PeerServerPing(String ip) {
		ipAddr = ip;
	} // end specific constructor
	
	public String getHeader() {
		return this.HEADER;
	} // end getHeader();
	
	public long getType() {
		return PeerServerPing.serialVersionUID;
	} // end getType
	
	public String getIP() {
		return this.ipAddr;
	} // end getIP

} // end PeerServerPing
