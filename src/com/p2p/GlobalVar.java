/* ---------------------------------------------

GlobalVar Class
Last updated: Friday, 29th Nov 2013

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
	// MSG_TYPE - Video Query
	public final static int TYPE_VIDQUERY = 4;
	// MSG_TYPE - Video Query Response
	public final static int TYPE_VIDQUERY_RESPONSE = 5;
	
	// Peer to Server Heartbeat Duration
	public final static long P2S_HEARTBEAT_DURATION = 3000;
	// Listening Port on Server for Peer to Server Heartbeat
	public final static int P2S_LISTEN_PORT = 9898;
	// Peer to Peer Heartbeat Duration
	public final static long P2P_HEARTBEAT_DURATION = 3000;
	// Listening Port on Server for Peer to Peer Heartbeat
	public final static int P2P_UDP_PORT = 9899;
	// Peer to Peer TCP Retry Duration
	public final static long TCP_RETRY_DURATION = 3000;
	// Listening Port on Server for Peer to Peer Heartbeat
	public final static int P2P_TCP_PORT = 9900;
	
	// Top level directory for videos
	public final static String VIDEO_TOP_DIR = "Videos/";
	// File extension for videos
	public final static String VIDEO_SUFFIX = ".ts";
	
} // end class GlobalVar
