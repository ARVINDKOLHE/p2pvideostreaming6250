package com.p2p;

import java.io.Serializable;
import java.util.Hashtable;

//Implements "Serializable" allows you to send packet as an object
//without the need to break it down into byte buffers
public class ServerPeerPing implements Serializable {

	private final String HEADER = "3142";
	private final int TYPE_SERVPEER_PING = 2;
	private Hashtable <String, Boolean> neighbours;
	
	public ServerPeerPing() {
		neighbours = new Hashtable <String, Boolean>();
	} // end default constructor
	
	// The message is made up of:
	// Unique Identifier
	// Message Type Field
	// Hashtable containing IPv4 address strings for each assigned neighbour as keys
	
	public String getHeader() {
		return this.HEADER;
	} // end getHeader();
	
	public int getType() {
		return this.TYPE_SERVPEER_PING;
	} // end getType
	
	// Adds a neighbour to the hashtable, by IP.
	// The boolean value is unimportant (has to be included by convention)
	public void addNeighbour(String ip) {
		neighbours.put(ip, true);
	} // end addNeighbour
	
	public Hashtable <String, Boolean> getNeighbours() {
		return this.neighbours;
	} // end getNeighbours
	
} // end ServerPeerPing
