/* ---------------------------------------------

GlobalVar Class
Last updated: Wednesday, 27th Nov 2013

Class containing constants used for packet configurations

------------------------------------------------ */

package com.p2p;

public final class GlobalVar {

	// Header identifier for our P2P packets
	public final static String HEADER = "3142";
	// MSG_TYPE - Peer to Server Heartbeat
	public final static int TYPE_PEERSERV_PING = 1;
	// MSG_TYPE - Server to Peer Heartbeat
	public final static int TYPE_SERVPEER_PING = 2;
	// MSG_TYPE - Peer to Peer Heartbeat
	public final static int TYPE_INTERPEER_PING = 3;
	
	// Peer to Server Heartbeat Duration
	public final static long P2S_HEARTBEAT_DURATION = 3000;
	// Listening Port on Server for Peer to Server Heartbeat
	public final static int P2S_LISTEN_PORT = 9898;
	// Peer to Peer Heartbeat Duration
	public final static long P2P_HEARTBEAT_DURATION = 30000;
	// Listening Port on Server for Peer to Peer Heartbeat
	public final static int P2P_UDP_PORT = 9899;	
	
} // end class GlobalVar
