package com.p2p;

import java.io.Serializable;
import java.util.HashSet;

public class PeerInformation implements Serializable{
	/**
	 * Record what neighbours.
	 */
	private static final long serialVersionUID = 1L;
	public String ipAddr;
	public int port;
	public HashSet <String> neighbour = new HashSet <String> ();
	
	public PeerInformation(String ipAddr, int port) {
		this.ipAddr = ipAddr;
		this.port = port;
	}
	
	public void addPeer(String ip) throws Exception{
		if (neighbour.contains(ip)) throw new Exception("Duplicated Peer");
		else neighbour.add(ip);
	}
	
	public void removePeer(String ip) throws Exception{
		if (neighbour.contains(ip)) neighbour.remove(ip);
		else throw new Exception("Peer not exist");
	}
}
