/* ---------------------------------------------

PeerCommThread Class
Last updated: Thursday, 28th Nov 2013

Worker thread that handles all peer to peer
connections between source peer and targeted peer

1. Peer to Peer heartbeats
2. Video query requests

------------------------------------------------ */

package com.p2p;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class PeerWorkerThread extends Thread {

	private String nodeIP;
	private LoggerThread logThread;
	private boolean isActive;
	
	private Hashtable <String, VideoInfo> videoList;
	
	public PeerWorkerThread(String ip, LoggerThread logThread) {

		nodeIP = ip;
		isActive = true;

		videoList = new Hashtable <String, VideoInfo>();
		
	} // end default constructor

	// inner class (Peer-to-Peer Heartbeat Timer Task)
	// NeighbourPoll routine to be executed at fixed timing intervals
	private class NeighbourPoll extends TimerTask {
		
		private String neighbourIP;
		private LoggerThread logThread;
		
		private NeighbourPoll(String ip, LoggerThread thLog) {
			
			this.neighbourIP = ip;
			this.logThread = thLog;
		
		} // end specific constructor
		
		public void run() {
						
			try {

				// Write Peer to Peer heartbeat msg to stream
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				ObjectOutputStream oStream = new ObjectOutputStream(bStream);
				oStream.writeObject(new Object());
				oStream.close();
				
				// Convert stream to a byte array (required for UDP datagram)
				byte[] serialisedMsg = bStream.toByteArray();
				
				// Construct new datagram packet
				DatagramPacket msg = new DatagramPacket(serialisedMsg, serialisedMsg.length, 
						InetAddress.getByName(this.neighbourIP), GlobalVar.P2P_UDP_PORT);
				
				DatagramSocket dsocket = new DatagramSocket();
				
				// Send heartbeat to peer
				dsocket.send(msg);

				this.logThread.writeLog("Successfully sent heartbeat to neighbour at " + this.neighbourIP);
				
				// Close connection upon completion
				dsocket.close();
				
			}
			
			catch (IOException ioe) {

				// Write FAIL status to log file
				this.logThread.writeLog("ERROR: Could not send heartbeat to neighbour at " + this.neighbourIP);
			
			} 
			
			catch (Exception e) {

				// Write general exception type to log
				this.logThread.writeLog(this.getClass().getName() + ": " + e.toString());
				
			} // end try-catch
						
		} // end run
		
	} // end inner class NeighbourPoll
	
	// Signal thread to stop activity and perform cleanup
	public void stopActivity() {
		this.isActive = false;
	} // end stopActivity
	
	// Overloaded function to service InterPeerPings
	public void processMsg(InterPeerPing ippMsg) {
		this.videoList = ippMsg.getSrcVideoList();
	} // end processMsg
	
	public void run() {
		
		// Execute a new timer and timer task
		// Send poll from Peer to Peer at fixed durations
		Timer timer = new Timer();
		NeighbourPoll nPoll = new NeighbourPoll(this.nodeIP, this.logThread);
		
		// Sending of poll to neighbours starts after initial interval of one heartbeat duration
		timer.scheduleAtFixedRate(nPoll, GlobalVar.P2P_HEARTBEAT_DURATION, GlobalVar.P2P_HEARTBEAT_DURATION);

		this.logThread.writeLog("Neighbour poll TimerTask activated: " + this.nodeIP);
		
		while (this.isActive) {
			
			// Sleep forever until prompted to exit loop
			try {
				Thread.sleep(Long.MAX_VALUE);
			}
			catch (Exception e) {}
			
		} // endwhile
		
		// Stop peer to peer heartbeats when thread is to be stopped
		timer.cancel();

		this.logThread.writeLog("Neighbour poll TimerTask cancelled: " + this.nodeIP);
		
	} // end run
	
} // end class PeerWorkerThread
