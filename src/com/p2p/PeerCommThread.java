/* ---------------------------------------------

PeerCommThread Class
Last updated: Thursday, 28th Nov 2013

Thread class which handles all communications
between a Peer and another Peer

------------------------------------------------ */

package com.p2p;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class PeerCommThread extends Thread{

	private LoggerThread logThread;
	private P2PPeer p2ppeer;
	private boolean isActive;
	
	public PeerCommThread(LoggerThread loggerThread, P2PPeer p2p) {
		
		logThread = loggerThread;
		p2ppeer = p2p;
		isActive = true;
		
	} // end specific constructor
	
	public void stopActivity() {
		this.isActive = false;
	} // end stopActivity
	
	public void run() {
		
		DatagramSocket dsocket = null;
		
		// Attempt to create new datagram socket to listen on
		while (this.isActive) {	
			
			try {

				dsocket = new DatagramSocket(GlobalVar.P2P_UDP_PORT);				
				logThread.writeLog("Listening port (UDP) established at " + GlobalVar.P2P_UDP_PORT);
				// Break out of loop once listening port is ready
				break;
				
			}
			catch (SocketException se) {
					logThread.writeLog("ERROR [Datagram Socket OPEN]: Cannot establish listening port");				
			}
			
			catch (Exception e) {
				
				// Write general exception type to log
				this.logThread.writeLog("ERROR [Datagram Socket OPEN]: " + this.getClass().getName() + ": " + e.toString());
				
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
						PeerWorkerThread worker = this.p2ppeer.getNeighbours().get(msg.getSrcIP());
						
						// Let worker handle Peer to Peer heartbeat packet
						if (worker != null)
							worker.processMsg(msg);
						
					}
					
					// Ignore message if it does not belong to any required class
					else {} // endif
					
				} // endif

				
			}
			catch (IOException ioe) {
				
				this.logThread.writeLog("ERROR [Datagram Socket RCV]: Error receiving datagram.");
				
			} 
			
			catch (Exception e){
				
				this.logThread.writeLog("ERROR [Datagram Socket RCV]: " + this.getClass().getName() + ": " + e.toString());
				
			} // end try-catch			
			
		} // endwhile
		
	} // end run
	
} // end PeerCommThread
