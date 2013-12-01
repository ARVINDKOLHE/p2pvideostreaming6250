/* ---------------------------------------------

PeerWorkerThread Class
Last updated: Saturday, 30th Nov 2013

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
import java.net.Socket;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class PeerWorkerThread extends Thread {

	private String nodeIP;
	private LoggerThread logThread;
	private boolean isActive;
	
	private Hashtable <String, VideoInfo> videoList;
	
	// default constructor; used only as placeholder
	public PeerWorkerThread() {
		nodeIP = null;
		logThread = null;
		isActive = false;
		videoList = new Hashtable <String, VideoInfo>();
	}
	
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

				this.logThread.writeLog("[" + this.getClass().getName() + "] Successfully sent heartbeat to neighbour at " + this.neighbourIP);
				
				// Close connection upon completion
				dsocket.close();
				
			}
			
			catch (IOException ioe) {

				// Write FAIL status to log file
				this.logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Could not send heartbeat to neighbour at " + this.neighbourIP);
			
			} 
			
			catch (Exception e) {

				// Write general exception type to log
				this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());
				
			} // end try-catch
						
		} // end run
		
	} // end inner class NeighbourPoll
	
	// Signal thread to stop activity and perform cleanup
	public void stopActivity() {
		
		this.isActive = false;
		
		// Interrupt thread to initiate shutdown of thread
		this.interrupt();
		
	} // end stopActivity
	
	// retrieves video list for this node
	public Hashtable <String, VideoInfo> getVideoList() {
		return this.videoList;
	} // end getVideoList
	
	public String getNodeIP() {
		return this.nodeIP;
	} // end getNodeIP
	
	// General method that takes in an object type to be sent out through TCP
	public void sendTCPObject(Object o) {

		String type = null;
		
		try {

			if (o instanceof VideoQuery)
				type = "Video Query";
			else if (o instanceof VideoQueryResponse)
				type = "Video Query Response";
			else {

				this.logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Unknown TCP Object type!");
				return;
			
			} // endif
			
			// Attempt to establish TCP connection with this next hop node
			Socket serverSocket = new Socket(this.nodeIP, GlobalVar.P2P_TCP_PORT);
			
			// Write the video query response message as serialised object to socket connection
			ObjectOutputStream oStream = new ObjectOutputStream(serverSocket.getOutputStream()); 
			oStream.writeObject(o);
			oStream.close();
			
			// Close connection once video query response has been sent
			serverSocket.close();
			
			// Write SUCCESS status to log file
			this.logThread.writeLog("[" + this.getClass().getName() + "] " + type + " sent to: " + this.nodeIP + ":" + GlobalVar.P2P_TCP_PORT);
			
		}
		
		catch (IOException ioe) {

			// Write FAIL status to log file
			this.logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Could not send " + type + " to " + this.nodeIP + ":" + GlobalVar.P2P_TCP_PORT);
		
		} // end try-catch

		catch (Exception e) {

			// Write general exception type to log
			this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());
			
		} // end try-catch
	
	} // end sendTCPObject
	
	// Send VideoQuery message
	public void sendVideoQuery(VideoQuery vidQuery) {
		this.sendTCPObject(vidQuery);		
	} // end sendVideoQuery
	
	// Send VideoQueryResponse message
	public void sendVideoQueryResponse(VideoQueryResponse vQueryResponse) {
		this.sendTCPObject(vQueryResponse);
	} // end sendVideoQueryResponse
	
	// Overwrite current video list for that neighbour with the new copy
	public void processMsg(InterPeerPing ippMsg) {
		this.videoList = ippMsg.getSrcVideoList();
	} // end processMsg (InterPeerPing)

	public void run() {
		
		// Execute a new timer and timer task
		// Send poll from Peer to Peer at fixed durations
		Timer timer = new Timer();
		NeighbourPoll nPoll = new NeighbourPoll(this.nodeIP, this.logThread);
		
		// Sending of poll to neighbours starts after initial interval of one heartbeat duration
		timer.scheduleAtFixedRate(nPoll, GlobalVar.P2P_HEARTBEAT_DURATION, GlobalVar.P2P_HEARTBEAT_DURATION);

		this.logThread.writeLog("[" + this.getClass().getName() + "] Neighbour poll TimerTask activated: " + this.nodeIP);
		
		while (this.isActive) {
			
			// Sleep forever until prompted to exit loop
			try {
				Thread.sleep(Long.MAX_VALUE);
			}
			catch (Exception e) {}
			
		} // endwhile
		
		// Stop peer to peer heartbeats when thread is to be stopped
		timer.cancel();

		this.logThread.writeLog("[" + this.getClass().getName() + "] Neighbour poll TimerTask cancelled: " + this.nodeIP);
		
	} // end run
	
} // end class PeerWorkerThread
