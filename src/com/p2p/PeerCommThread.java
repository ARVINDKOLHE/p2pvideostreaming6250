/* ---------------------------------------------

PeerCommThread Class
Last updated: Friday, 29th Nov 2013

Thread class which handles incoming packets for
peers, deserialises them to the correct message
class, and passes them to the relevant peer
worker threads for processing

------------------------------------------------ */

package com.p2p;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class PeerCommThread extends Thread{

	private LoggerThread logThread;
	private PeerCommTCPThread pTCPThread;
	private P2PPeer p2ppeer;
	private boolean isActive;
	private DatagramSocket dsocket;
	
	public PeerCommThread(LoggerThread loggerThread, P2PPeer p2p) {
		
		logThread = loggerThread;
		pTCPThread = new PeerCommTCPThread(p2ppeer, logThread);
		p2ppeer = p2p;
		isActive = true;
		dsocket = null;
		
	} // end specific constructor
	
	// inner class
	// Separate thread which listens on TCP port
	private class PeerCommTCPThread extends Thread {

		// Heartbeat duration and listening port for Peer to Server comms
		private final int PEER_LISTEN_PORT = GlobalVar.P2P_TCP_PORT;
		private ServerSocket peerSocket;
		private P2PPeer p2ppeer;
		private LoggerThread logThread;
		
		private boolean isActive;
		
		private PeerCommTCPThread(P2PPeer p2ppeer, LoggerThread loggerThread) {
			
			logThread = loggerThread;
			this.isActive = true;
			
		} // end specific constructor

		public void stopActivity() {
			
			this.isActive = false;

			if (peerSocket != null) {
				
				// Close socket upon completion
				try {
					
					peerSocket.close();
					logThread.writeLog("[" + this.getClass().getName() + "] TCP Listening port closed.");
					
				}
				catch (IOException ioe) {
					logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Could not close TCP listening connection.");
				} // end try-catch
				
				catch (Exception e) {

					// Write general exception type to log
					this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());
					
				} // end try-catch
					
			} // endif
			
		} // end stopActivity
		
		// forward video query to all neighbour workers 
		public void forwardToNextHopVideoQuery(VideoQuery vidQuery) {
			
			if (p2ppeer.getNeighbourWorkers() != null) {

				Enumeration <String> ipKeys = p2ppeer.getNeighbourWorkers().keys();
				
				// Iterate through this peer's neighbour list
				while (ipKeys.hasMoreElements()) {

					String ip = ipKeys.nextElement();
					PeerWorkerThread wThread = p2ppeer.getNeighbourWorkers().get(ip);
					
					wThread.sendVideoQuery(vidQuery);
					
				} // endwhile
			
			} // endwhile
			
		} // end forwardToNextHopVideoQuery
		
		// Delegate sending of query response to targeted neighbour worker
		public void sendVideoQueryResponse(VideoQueryResponse vQueryRes) {
		
			if (p2ppeer.getNeighbourWorkers() != null) {

				// Decrement index to point to IP of next hop on reverse path
				vQueryRes.decrementPeerIndex();
				
				PeerWorkerThread wThread = p2ppeer.getNeighbourWorkers().get(vQueryRes.getCurrPeer());
				
				if (wThread == null)
					logThread.writeLog("[" + this.getClass().getName() + "] ERROR: " + vQueryRes.getCurrPeer() + "[" + vQueryRes.getPeerIndex() + "]" + " is not a neighbour.");
					
				else {

					logThread.writeLog("[" + this.getClass().getName() + "] Preparing query response to " + vQueryRes.getCurrPeer() + "[" + vQueryRes.getPeerIndex() + "]");
					wThread.sendVideoQueryResponse(vQueryRes);
	
				} // endif
							
			} // endwhile
			
		} // end sendVideoQueryResponse
		
		public void run() {

			// Try to open up a listening port
			while (this.isActive) {

				try {
					
					peerSocket = new ServerSocket(this.PEER_LISTEN_PORT);
					peerSocket.setReuseAddress(true);
					
					logThread.writeLog("[" + this.getClass().getName() + "] TCP Listening port for Peer established at " + this.PEER_LISTEN_PORT);
					
					// Break out of loop once listening port is ready
					break;
					
				}
				catch (IOException ioe) {
					logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Cannot establish TCP listening port");
				}
				
				catch (Exception e) {

					// Write general exception type to log
					this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());
					
				} // end try-catch
				
				// Sleep for a while before retrying if unable to open listening port
				try {
					Thread.sleep(GlobalVar.TCP_RETRY_DURATION/2);
				}
				catch (Exception e) {}
					
			} // endwhile
			
			// Thread starts listening for heartbeats coming from server end
			while (this.isActive) {
				
				// Listens for a TCP connection on this port and accepts it
				try {

					Socket dataSocket = peerSocket.accept();
					
					// Read the object and attempt to cast object as ServerPeerPing type
					ObjectInputStream iStream = new ObjectInputStream(dataSocket.getInputStream()); 
					
					try {
						
						// Read received byte array as an object
						Object serialisedMsg = iStream.readObject();
						
						// Close the input stream when completed
						iStream.close();
						
						// Given a serialised message, cast to appropriate class
						// Process the message
						// Cast message to corresponding type and perform processing
						if (serialisedMsg != null) {
							
							// Peer to Peer Heartbeat Message
							if (serialisedMsg instanceof VideoQuery) {
								
								// Cast serialised object to instance of InterPeerPing
								VideoQuery vidQuery = (VideoQuery) serialisedMsg;

								// Obtain local IP address
								String myIP = dataSocket.getInetAddress().getHostAddress();

								// Ensure that this IP has not been already traversed in path
								if (!vidQuery.isVisitedIP(myIP)) {

									logThread.writeLog("[" + this.getClass().getName() + "] VideoQuery received.");
									
									if (vidQuery.isGetQuery()) {
										
										// Get Worker to send message with video contents if this is a GET message
										// This is done provided that we have the file and the relevant block
										if ((p2ppeer.getMyVideos().containsKey(vidQuery.getVideoName())) &&
												(p2ppeer.getMyVideos().get(vidQuery.getVideoName()).hasCompleteBlock(vidQuery.getReqBlock()))) {
											
											logThread.writeLog("[" + this.getClass().getName() + "] Verified node has " + vidQuery.getVideoName() + ", Block " + vidQuery.getReqBlock());
											
											// Read the file representing the block into a byte array
											Path path = Paths.get(GlobalVar.VIDEO_TOP_DIR + vidQuery.getVideoName() + "/" + vidQuery.getVideoName() + GlobalVar.VIDEO_SUFFIX);
											byte[] data = Files.readAllBytes(path);

											// Format new video query response message with the video block to be propagated back to source
											VideoQueryResponse vQueryRes = new VideoQueryResponse(vidQuery.getSrcIP(),
													vidQuery.getVideoName(), vidQuery.getReqBlock(), vidQuery.getIPPath(), data);
											
											// Send video query response
											this.sendVideoQueryResponse(vQueryRes);											
											
										} // endif
										
									}
									
									// packet is still in search of someone with the required video and block
									else {

										// Add your own IP to path list
										vidQuery.insertIP(myIP);

										logThread.writeLog("[" + this.getClass().getName() + "] Inserted " + myIP + "into path list.");
										
										if (p2ppeer.getMyVideos().containsKey(vidQuery.getVideoName())) {
											
											// Format new video query response message to be propagated back to source
											VideoQueryResponse vQueryRes = new VideoQueryResponse(vidQuery.getSrcIP(),
													vidQuery.getVideoName(), vidQuery.getReqBlock(), vidQuery.getIPPath());
											
											// Send video query response
											this.sendVideoQueryResponse(vQueryRes);
											
										}
										
										else {
											
											// Forward video query to all neighbours
											this.forwardToNextHopVideoQuery(vidQuery);
											
										} // endif

										
									} // endif
									
								}
								
								else if (serialisedMsg instanceof VideoQueryResponse) {
									
								}
								
								// IP already traversed; log as duplicate
								else
									logThread.writeLog("[" + this.getClass().getName() + "] Duplicate VideoQuery dropped.");
																
							}
							
							// Ignore message if it does not belong to any required class
							else {} // endif
							
						} // endif	
					
					}
					
					catch (Exception e) {} // end try-catch
					
				}
				
				catch (IOException ioe) {

					if (this.isActive)
						this.logThread.writeLog("[" + this.getClass().getName() + "] IOException on TCP input stream from peer.");
						
					// Triggered by main P2PPeer class to close thread
					else {
						
						this.logThread.writeLog("[" + this.getClass().getName() + "] PeerCommTCPThread closing.");
						break;
						
					} // endif
					
					
				} // end try-catch
							
				catch (Exception e) {

					// Write general exception type to log
					this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());
					
				} // end try-catch

			} // endwhile
		
		} // end run
		
	} // end inner class TCPListenerThread
	
	// Invoked by main thread; tells controller to get workers to check neighbours for video
	public void searchVideo(String videoName, int block) {

		this.logThread.writeLog("[" + this.getClass().getName() + "] Searching for block " + block + " of video: " + videoName);
		
		Hashtable <String, PeerWorkerThread> workers = this.p2ppeer.getNeighbourWorkers();
		
		Enumeration <String> workerKeys = workers.keys();
		
		// Iterate through this peer's neighbour list
		while (workerKeys.hasMoreElements()) {
			
			String ip = workerKeys.nextElement();
			PeerWorkerThread wThread = workers.get(ip);
			
			// Get video list from neighbour
			Hashtable <String, VideoInfo> videoList = wThread.getVideoList();
			
			if (videoList != null) {

				// If neighbour has the video and block, we issue a GET query request straightaway
				if ((videoList.containsKey(videoName)) &&
						(videoList.get(videoName).hasCompleteBlock(block))) {

					this.logThread.writeLog("[" + this.getClass().getName() + "] " + ip + " has block " + block + " of video: " + videoName);
					
					// Issue a GET Video Query to retrieve block from neighbour
					wThread.sendVideoQuery(new VideoQuery(p2ppeer.getMyIP(), ip, videoName, block, new ArrayList <String>()));
				
				}
				
				else {

					this.logThread.writeLog("[" + this.getClass().getName() + "] Querying " + ip + " for block " + block + " of video: " + videoName);
					
					// Issue a standard Video Query to neighbour
					wThread.sendVideoQuery(new VideoQuery(p2ppeer.getMyIP(), videoName, block));					
					
				} // endif
			
			} // endif			
			
		} // endwhile
		
	} // end searchVideo
	
	public void stopActivity() {
		
		this.isActive = false;
		
		// Signal TCP Listener thread to cease activity
		this.pTCPThread.stopActivity();
		
		// Signal datagram socket to close
		if (dsocket != null)
			dsocket.close();
		else
			this.interrupt();
	
	} // end stopActivity
	
	public void run() {

		// Start thread to listen on TCP port
		this.pTCPThread.start();
		
		// Attempt to create new datagram socket to listen on
		while (this.isActive) {	
			
			try {
				
				dsocket = new DatagramSocket(GlobalVar.P2P_UDP_PORT);

				this.logThread.writeLog("[" + this.getClass().getName() + "] Listening port (UDP) estabished at " + GlobalVar.P2P_UDP_PORT);
				
				// Break out of loop once listening port is ready
				break;
				
			}
			
			catch (SocketException se) {
				
				System.out.println(se.getMessage() + " , " + se.getClass().getName() + se.getLocalizedMessage());
				
				this.logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Could not establish listening port.");
			}
			
			catch (Exception e) {
				
				// Write general exception type to log
				this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());
				
			} // end try-catch
			
			// Wait for fixed duration and retry
			// Sleep for a while before retrying if unable to open listening port
			try {
				Thread.sleep(GlobalVar.P2P_HEARTBEAT_DURATION/2);
			}
			catch (Exception e) {}
			
		} // endwhile
				
		// Listen for incoming datagrams
		while (this.isActive) {

			byte[] recvBuf = new byte[65535];			
			DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
	        
			// Read incoming datagram
			try {
				
				dsocket.receive(packet);	

				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(packet.getData()));
				
				// Read received byte array as an object
				Object serialisedMsg = iStream.readObject();

				// Close the input stream when completed
				iStream.close();
				
				// Given a serialised message, cast to appropriate class
				// Process the message
				// Cast message to corresponding type and perform processing
				if (serialisedMsg != null) {
					
					// Peer to Peer Heartbeat Message
					if (serialisedMsg instanceof InterPeerPing) {
												
						// Cast serialised object to instance of InterPeerPing
						InterPeerPing msg = (InterPeerPing) serialisedMsg;

						// Obtain pointer to worker thread for this neighbour
						PeerWorkerThread worker = this.p2ppeer.getNeighbourWorkers().get(msg.getSrcIP());
						
						// Let worker handle Peer to Peer heartbeat packet
						if (worker != null)
							worker.processMsg(msg);
						
					}
					
					// Ignore message if it does not belong to any required class
					else {} // endif
					
				} // endif
				
			}
			
			catch (IOException ioe) {
				
				if (this.isActive)
					this.logThread.writeLog("[" + this.getClass().getName() + "] ERROR: Could not receive datagram.");

				// Triggered by main P2PPeer class to close thread
				else {

					this.logThread.writeLog("[" + this.getClass().getName() + "] PeerCommThread signalled to close.");
					break;
					
				} // endif
				
			} 
			
			catch (Exception e){
				this.logThread.writeLog("[" + this.getClass().getName() + "] EXCEPTION:" + e.getClass().getName());				
			} // end try-catch			
			
		} // endwhile

		this.logThread.writeLog("[" + this.getClass().getName() + "] PeerCommThread closing.");
		
	} // end run
	
} // end PeerCommThread
