/* ---------------------------------------------

ServerCommThread Class
Last updated: Monday, 18th Nov 2013

Thread class which handles all communications
between a Peer and the Server

------------------------------------------------ */

package com.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ServerCommThread extends Thread {
	
	// Used during inspection of incoming packets
	private final String HEADER = "3142";
	private final int TYPE_SERVPEER_PING = 2;

	private final long HEARTBEAT_DURATION = 30000;
	private final int PEER_LISTEN_PORT = 9898;
	
	private String serverIPAddr;
	private int serverPort;
	private LoggerThread logThread;
	private P2PPeer p2pPeer;
	
	private boolean isActive;
	
	// inner class (Peer-to-Server Heartbeat Timer Task)
	// Heartbeat routine to be executed at fixed timing intervals
	private class Heartbeat extends TimerTask {
		
		private String serverIP;
		private int serverPort;
		private LoggerThread logThread;
		
		private Heartbeat(String ip, int port, LoggerThread thLog) {
			this.serverIP = ip;
			this.serverPort = port;
			this.logThread = thLog;
		} // end specific constructor
		
		public void run() {			
			
			try {

				// Attempt to establish TCP connection with P2P Server
				Socket serverSocket = new Socket(this.serverIP, this.serverPort);
				
				// Write the heartbeat message as serialised object to socket connection
				// Receiver to cast it as PeerServerPing class object
				ObjectOutputStream oStream = new ObjectOutputStream(serverSocket.getOutputStream()); 
				oStream.writeObject(new PeerServerPing(this.serverIP));
				oStream.close();
				
				// Close connection once heartbeat has been sent
				serverSocket.close();
				
				// Write SUCCESS status to log file
				this.logThread.writeLog("Successfully sent heartbeat to server at " + this.serverIP + ":" + this.serverPort);
				
			}
			catch (IOException ioe) {

				// Write FAIL status to log file
				this.logThread.writeLog("ERROR: Could not send heartbeat to server.");
			
			} // end try-catch
			
		} // end run
		
	} // end Heartbeat
	
	public ServerCommThread(String ip, int port, LoggerThread thLog, P2PPeer peer) {
		
		serverIPAddr = ip;
		serverPort = port;
		logThread = thLog;
		p2pPeer = peer;
		
		isActive = true;
		
	} // end specific constructor
	
	public void stopActivity() {
		this.isActive = false;
	} // end stopActivity
	
	public void run() {
		
		ServerSocket peerSocket = null;
		
		// Try to open up a listening port
		while (this.isActive) {

			try {
				
				peerSocket = new ServerSocket(this.PEER_LISTEN_PORT);
				logThread.writeLog("Listening port established at " + this.PEER_LISTEN_PORT);
				
				// Break out of loop once listening port is ready
				break;
				
			}
			catch (IOException ioe) {
				logThread.writeLog("ERROR: Cannot establish listening port");
			} // end try-catch
			
			// Sleep for a while before retrying if unable to open listening port
			try {
				Thread.sleep(this.HEARTBEAT_DURATION/2);
			}
			catch (InterruptedException ie) {}
				
		} // endwhile

		// Execute a new timer and timer task
		// Send heartbeat from Peer to Server at fixed durations
		Timer timer = new Timer();
		Heartbeat heartbeat = new Heartbeat(this.serverIPAddr, this.serverPort, logThread);
		
		// Sending of heartbeat starts after initial interval of one heartbeat duration
		timer.scheduleAtFixedRate(heartbeat, this.HEARTBEAT_DURATION, this.HEARTBEAT_DURATION);

		System.out.println("Timer started!");
		
		// Thread starts listening for heartbeats coming from server end
		while (this.isActive) {
			
			// Listens for a TCP connection on this port and accepts it
			try {

				Socket dataSocket = peerSocket.accept();
				
				// Read the object and attempt to cast object as ServerPeerPing type
				ObjectInputStream iStream = new ObjectInputStream(dataSocket.getInputStream()); 
				
				try {
					
					ServerPeerPing inMsg = (ServerPeerPing) iStream.readObject();					
					
					// Process contents of object and update neighbour list of process
					this.p2pPeer.updateNeighbours(inMsg.getNeighbours());
				
				}
				catch (Exception e) {}
				
			}
			catch (IOException ioe) {

				logThread.writeLog("ERROR: Cannot read input stream coming from server");
				
			} // end try-catch			

		} // endwhile
			
		timer.cancel();

		if (peerSocket != null) {
				
			// Close socket upon completion
			try {
				
				peerSocket.close();
				logThread.writeLog("Listening connection to P2P Server closed.");
				
			}
			catch (IOException ioe) {
				System.out.println("ERROR: Cannot close Peer Socket.");				
			} // end try-catch
				
		} // endif

	} // end run

} // end ServerCommThread
