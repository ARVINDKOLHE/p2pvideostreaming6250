package com.p2p;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

public class P2PServer {
	//P2PServer contains peer information 
	private static final int PORT = 13136;
	private final long HEARTBEAT_DURATION = 3000;
	private Hashtable <String, PeerInformation> peerSet = new Hashtable <String, PeerInformation> ();
	private LoggerThread loggerThread;
	
	public P2PServer()
	{
		this.loggerThread = new LoggerThread();
	}

	//Handler is a new thread to handle every in come socket connection
	private class Handler extends Thread {
		private Socket socket;

		public Handler(Socket socket) {
			this.socket = socket;
		}
	//Every connection accepted, Handler will 
	//1st read the in coming information to see whether a new peer
	//2nd if a new peer the help it to generate neighbour
		public void run() {
			Thread.currentThread().setName("Handler");
			try {
				String ip = socket.getInetAddress().getHostAddress();
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				PeerServerPing ping = (PeerServerPing) inStream.readObject();
				int port = 9898;
				loggerThread.writeLog("New socket connection from "+ip+",\t"+port);
				synchronized(peerSet) {
					if (!peerSet.containsKey(ip)) {
						loggerThread.writeLog("New Peer connected");
						PeerInformation peerInformation = new PeerInformation (ip,port);
						generateNeighbour(peerInformation);
						peerSet.put(ip, peerInformation);
					}
					else {
						loggerThread.writeLog("Regular Peer HeartBeat");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	//Heartbeat is to send peers connection information
	private final class Heartbeat extends TimerTask{
		private final int PORT = 9898;
		public void run()
		{
			Thread.currentThread().setName("ServerHeartBeat");
			try{
				synchronized (peerSet) {
					loggerThread.writeLog("Start sending heartbeat for " + peerSet.size());
	//Enumerate every peer information. 
					for (PeerInformation peer: peerSet.values()) {
						loggerThread.writeLog("Start sending heartbeat to" + peer.ipAddr + " , " + this.PORT);
						Socket socket = new Socket(peer.ipAddr, this.PORT);
						ObjectOutputStream oStream = new ObjectOutputStream(socket.getOutputStream());
						oStream.writeObject(new ServerPeerPing(peer));
						//oStream.writeObject(peer);
						oStream.close();
						socket.close();
					}
					loggerThread.writeLog("Heartbeat Sent");
				}
			} catch (IOException ioe){
				loggerThread.writeLog("{Error}" + ioe.getLocalizedMessage());
			}
		}
	}
	


	public static void main(String[] args) throws IOException {
		P2PServer p2pServer = new P2PServer();
		p2pServer.loggerThread.start();

		ServerSocket listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),PORT));
		p2pServer.loggerThread.writeLog("Successfully listening on port "+PORT);

		Timer timer = new Timer();
		Heartbeat heartbeat = p2pServer.new Heartbeat();
		timer.scheduleAtFixedRate(heartbeat, p2pServer.HEARTBEAT_DURATION, p2pServer.HEARTBEAT_DURATION);
		p2pServer.loggerThread.writeLog("Timer of Heartbeat set up sucessfully");

		try {
			while (true) {
				Socket sock = listener.accept();
				Handler handle = p2pServer.new Handler(sock);
				handle.run();
			}
		} finally {
			listener.close();
			timer.cancel();
		}
	}
	
	public void generateNeighbour (PeerInformation newpeer){
		ArrayList <PeerInformation> candidateList = new ArrayList <PeerInformation> ();
		for (PeerInformation peerInformation : peerSet.values() ) {
			if (peerInformation.neighbour.size() < 6) {
				candidateList.add(peerInformation);
			}
		}
		Random rand = new Random();
		int neighbourCount = Math.min(peerSet.size(), rand.nextInt(3)+2);
		PeerInformation peer;
//		System.out.println("Set size"+peerSet.size() + " neighbourCount " + neighbourCount);
		for (int i=0;i<neighbourCount;i++) {
			peer = candidateList.get(rand.nextInt(candidateList.size()));
			try {
				newpeer.addPeer(peer.ipAddr);
				peer.addPeer(newpeer.ipAddr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			candidateList.remove(peer);
		}
	}
	

}
