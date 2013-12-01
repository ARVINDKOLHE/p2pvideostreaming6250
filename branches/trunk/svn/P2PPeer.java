/* ---------------------------------------------

P2PPeer Node Class
Last updated: Monday, 18th Nov 2013

Main class for process

------------------------------------------------ */

package com.p2p;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

import java.io.*;

public class P2PPeer {
	
	private LoggerThread logThread;
	private ServerCommThread serverCommThread;
	private PeerCommThread peerCommThread;
	private boolean isActive = true;
	
	private Hashtable <String, PeerWorkerThread> neighbourWorkers;

	private void displayAppHeader(String ip) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("----------------------------------------------------------------\n");
		sb.append("P2P VIDEO STREAMING NODE (").append("IP: ").append(ip).append(")\n");
		sb.append("----------------------------------------------------------------\n");
		
		System.out.println(sb.toString());
		
	} // end displayAppHeader
	
	private void displayMenu() {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("1. Get video.").append("\n");
		sb.append("2. Show neighbours.").append("\n");
		sb.append("3. Show my videolist.").append("\n");
		sb.append("4. Show videolist of neighbours.").append("\n");
		sb.append("5. Exit program.").append("\n");		
		
		System.out.println(sb.toString());
		
	} // end displayAppHeader
	
	// Given a hashtable of neighbour IPs:
	// 1. Remove nodes which are no longer designated neighbours
	// 2. Insert new neighbour nodes
	public void updateNeighbours(Hashtable <String, PeerWorkerThread> n) {
		
		Enumeration <String> ipKeys = this.neighbourWorkers.keys();
		
		// Iterate through this peer's neighbour list
		while (ipKeys.hasMoreElements()) {
			
			String ip = ipKeys.nextElement();
			
			// If neighbour already exists, remove this entry from the new hashtable of entries
			if (n.containsKey(ip))
				n.remove(ip);

			// otherwise, the current IP is no longer a neighbour of this peer.
			else {
				
				// Cease all activity between node and removed neighbour
				// This is achieved by setting a flag on the corresponding PeerWorkerThread
				PeerWorkerThread currThread = neighbourWorkers.get(ip);
				currThread.stopActivity();
				
				// Thus, we remove this IP from the current peer's neighbour hashtable
				neighbourWorkers.remove(ip);
				
				// Log down removal of neighbouring peer node
				this.logThread.writeLog("Peer node " + ip + " removed as neighbour.");
			
			} // endif
			
		} // endwhile
		
		ipKeys = n.keys();
		
		// Iterate through the filtered list of new neighbours; add new neighbour nodes to hashtable
		while (ipKeys.hasMoreElements()) {
			
			String ip = ipKeys.nextElement();
			
			// Create a new worker thread for the new neighbour
			PeerWorkerThread peerThread = new PeerWorkerThread(ip, this.logThread);
			// Start the new peer worker thread
			peerThread.start();
			// Add new hashtable entry with worker thead
			neighbourWorkers.put(ip, peerThread);
			
			// Log down assignment of new peer node
			this.logThread.writeLog("Peer node " + ip + " added as neighbour.");
			
		} // endwhile
	
	} // end updateNeighbours
	
	public Hashtable <String, PeerWorkerThread> getNeighbours() {
		return this.neighbourWorkers;
	} // end getNeighbours
	
	public static void main(String[] args) {

		P2PPeer p2ppeer = new P2PPeer();
		
		String serverIP = null;
		int serverPort = 0;
		
		// Get the peer's IP address
		try {
			serverIP = InetAddress.getLocalHost().getHostAddress();
		}
		catch (Exception e) {}

		p2ppeer.logThread = new LoggerThread();		
		p2ppeer.logThread.start();
		
		while (p2ppeer.isActive) {

			p2ppeer.displayAppHeader(serverIP);
			
			// Create new Scanner object to read in input
			Scanner s = new Scanner(System.in);
			
			System.out.print("Enter IP address for P2P Server: ");
			String hostName = s.next();
			
			// Get port number
			while (p2ppeer.isActive) {
				
				System.out.print("Enter P2P Server Listening Port: ");
				
				try {
					
					serverPort = s.nextInt();
					
					if ((serverPort >=0) && (serverPort <=65535))
						break;
				}
				catch (Exception e) {}
				
				System.out.println("ERROR: Invalid port. Please retry!");
			
			} // endwhile
			
			// Launch Server communications thread
			p2ppeer.serverCommThread = new ServerCommThread(hostName, serverPort, p2ppeer.logThread, p2ppeer);
			p2ppeer.serverCommThread.start();
			
			// Launch Peer communications controller thread
			p2ppeer.peerCommThread = new PeerCommThread(p2ppeer.logThread, p2ppeer);
			p2ppeer.peerCommThread.start();
			
			// Loop display of menu until exit
			while (p2ppeer.isActive) {
				
				p2ppeer.displayMenu();
				
				int choice = 0;

				try {
					
					choice = s.nextInt();
					
					switch (choice) {
					
						case 1:
							break;
						case 2:
							break;
						case 3:
							break;
						case 4:
							break;
						case 5:
							p2ppeer.isActive = false;
							break;
							
						default: break;
					
					} // endswitch
					
					if ((serverPort >=0) && (serverPort <=65535))
						break;
				}
				catch (Exception e) {
					System.out.println("Invalid option. Please retry!");
				} // end try-catch
				
			} // endwhile
			
			System.out.println("Exiting program...");

			// Cleanup upon exit
			s.close();
			
			// Gets all threads to cease
			p2ppeer.serverCommThread.stopActivity();
			p2ppeer.peerCommThread.stopActivity();
			p2ppeer.logThread.stopActivity();
			
		}  // endwhile
		
	} // end main
	
}
