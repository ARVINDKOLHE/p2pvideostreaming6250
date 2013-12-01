package com.p2p;

public class TestDrive {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestDrive test = new TestDrive();
		//PeerWorkerThread pwtd = new PeerWorkerThread();
		PeerWorkerThread pwtd = new PeerWorkerThread("127.0.0.1", new LoggerThread());
	}

}
