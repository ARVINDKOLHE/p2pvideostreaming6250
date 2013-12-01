/* ---------------------------------------------

ServerPeerPing Class
Last updated: Thursday, 28th Nov 2013

Class defining format for Server to Peer heartbeats

------------------------------------------------ */

package com.p2p;

import java.io.Serializable;
import java.util.Hashtable;

//Implements "Serializable" allows you to send packet as an object
//without the need to break it down into byte buffers
public class ServerPeerPing implements Serializable {

	private final String HEADER = GlobalVar.HEADER;

	// Required to remove warnings
	private static final long serialVersionUID = GlobalVar.TYPE_SERVPEER_PING;
	
	private Hashtable <String, PeerWorkerThread> neighbours;
	
	public ServerPeerPing() {
		neighbours = new Hashtable <String, PeerWorkerThread>();
	} // end default constructor
	
	// The message is made up of:
	// Unique Identifier
	// Message Type Field
	// Hashtable containing IPv4 address strings for each assigned neighbour as keys
	
	public String getHeader() {
		return this.HEADER;
	} // end getHeader();
	
	public long getType() {
		return ServerPeerPing.serialVersionUID;
	} // end getType
	
	// Adds a neighbour to the hashtable, by IP.
	// PeerWorkerThread value is null at addition; to be filled later
	public void addNeighbour(String ip) {
		neighbours.put(ip, null);
	} // end addNeighbour
	
	public Hashtable <String, PeerWorkerThread> getNeighbours() {
		return this.neighbours;
	} // end getNeighbours
	
} // end ServerPeerPing
