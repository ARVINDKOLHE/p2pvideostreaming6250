/* ---------------------------------------------

P2PPeer Node Class
Last updated: Friday, 29th Nov 2013

Main class for process

------------------------------------------------ */

package com.p2p;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

public class P2PPeer {
	
	private LoggerThread logThread;
	private ServerCommThread serverCommThread;
	private PeerCommThread peerCommThread;
	private boolean isActive = true;
	
	private Hashtable <String, PeerWorkerThread> neighbourWorkers;
	private Hashtable <String, VideoInfo> myVideos;

	private P2PPeer() {
		
		// Create empty hashtables for worker and video lists
		neighbourWorkers = new Hashtable <String, PeerWorkerThread>();
		myVideos = new Hashtable <String, VideoInfo>();
		
	} // end default constructor
	
	private void displayAppHeader(String ip) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("----------------------------------------------------------------\n");
		sb.append("P2P VIDEO STREAMING NODE (").append("IP: ").append(ip).append(")\n");
		sb.append("----------------------------------------------------------------\n");
		
		System.out.println(sb.toString());
		
	} // end displayAppHeader
	
	private void displayMenu(String ip) {
		
		this.displayAppHeader(ip);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("1. Get video.").append("\n");
		sb.append("2. Show neighbours.").append("\n");
		sb.append("3. Show my videolist.").append("\n");
		sb.append("4. Show videolist of neighbours.").append("\n");
		sb.append("5. Exit program.").append("\n");		
		
		System.out.println(sb.toString());
		
	} // end displayAppHeader

	public void listNeighbours() {
		
		int count = 0;
		StringBuffer sb = new StringBuffer();
		
		if (this.neighbourWorkers != null) {

			Enumeration <String> ipKeys = this.neighbourWorkers.keys();
			
			// Iterate through this peer's neighbour list
			while (ipKeys.hasMoreElements()) {
				
				sb.append("ENTRY ").append(++count).append("\n");
				sb.append(ipKeys.nextElement()).append("\n");
				
			} // endwhile
		
		} // endwhile
		
		if (count == 0)
			sb.append("I have no neighbours!\n");
		
		System.out.println(sb.toString());
			
	} // end listNeighbours
	
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
				this.logThread.writeLog("[" + this.getClass().getName() + "] Peer node " + ip + " removed as neighbour.");
			
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
			this.logThread.writeLog("[" + this.getClass().getName() + "] Peer node " + ip + " added as neighbour.");
			
		} // endwhile
	
	} // end updateNeighbours
	
	public Hashtable <String, PeerWorkerThread> getNeighbourWorkers() {
		return this.neighbourWorkers;
	} // end getNeighbours
	
	private void listVideos() {

		int count = 0;
		StringBuffer sb = new StringBuffer();
		
		if (this.myVideos != null) {

			Enumeration <String> videoKeys = this.myVideos.keys();
			
			// Iterate through this peer's neighbour list
			while (videoKeys.hasMoreElements()) {
				
				sb.append("ENTRY ").append(++count).append("\n");
				
				String videoName= videoKeys.nextElement();
				
				// Append name of video
				sb.append(videoName).append(" - ");
				
				// Append number of blocks completed and total blocks
				VideoInfo vInfo = this.myVideos.get(videoName);
				
				sb.append(vInfo.countComplete()).append("/");
				sb.append(vInfo.getNumBlocks()).append("\n");
				
			} // endwhile
		
		} // endwhile
		
		if (count == 0)
			sb.append("I have no videos!\n");
		
		System.out.println(sb.toString());
		
	} // end listVideos
	
	// Retrieve hashtable of videos
	public Hashtable <String, VideoInfo> getMyVideos() {
		return this.myVideos;
	} // end getMyVideos
	
	public static void main(String[] args) {

		P2PPeer p2ppeer = new P2PPeer();
		
		String hostName = null;
		int serverPort = 0;
		
		// Get the peer's IP address
		try {
			hostName = InetAddress.getLocalHost().getHostAddress();
		}
		catch (Exception e) {}

		p2ppeer.logThread = new LoggerThread();		
		p2ppeer.logThread.start();
		
		while (p2ppeer.isActive) {

			p2ppeer.displayAppHeader(hostName);
			
			// Create new Scanner object to read in input
			Scanner s = new Scanner(System.in);
			
			System.out.print("Enter IP address for P2P Server: ");
			String serverIP = s.next();
			
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
			p2ppeer.serverCommThread = new ServerCommThread(serverIP, serverPort, p2ppeer.logThread, p2ppeer);
			p2ppeer.serverCommThread.start();
			
			// Launch Peer communications controller thread
			p2ppeer.peerCommThread = new PeerCommThread(p2ppeer.logThread, p2ppeer);
			p2ppeer.peerCommThread.start();
			
			// Loop display of menu until exit
			while (p2ppeer.isActive) {
				
				p2ppeer.displayMenu(hostName);
				
				int choice = 0;

				try {
					
					choice = s.nextInt();
					
					switch (choice) {
					
						case 1: // Video search
							
							System.out.print("Enter desired video file name (case sensitive): ");
							String videoName = s.next();
							
							System.out.println("Starting search for video: " + videoName);
							
							// Start search for video
							p2ppeer.peerCommThread.searchVideo(videoName, 0);
							
							break;
							
						case 2: // Show neighbours
							
							p2ppeer.listNeighbours();
							break;
							
						case 3: // Show my videos
							
							p2ppeer.listVideos();
							break;
							
						case 4: // Show video list of neighbours
							
							break;
							
						case 5:
							p2ppeer.isActive = false;
							break;
							
						default: 
							
							System.out.println("Invalid option! Please retry.");
							continue;
					
					} // endswitch

				}
				catch (Exception e) {
					System.out.println(e.getClass().getName());
					System.out.println("Invalid option. Please retry!");
				} // end try-catch
				
			} // endwhile
			
			System.out.println("Performing cleanup...");

			// Cleanup upon exit
			s.close();
			
			// Gets all threads to cease
			p2ppeer.serverCommThread.stopActivity();
			p2ppeer.peerCommThread.stopActivity();
			p2ppeer.logThread.stopActivity();
			
		}  // endwhile
		
		System.out.println("Goodbye!");
		
	} // end main
	
}