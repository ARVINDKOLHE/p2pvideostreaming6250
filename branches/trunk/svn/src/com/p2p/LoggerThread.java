/* ---------------------------------------------
LoggerThread Class
Last updated: Sunday, 17th Nov 2013

This thread is responsible for logging useful
debug information. Most other threads will take
this in as an argument in their constructors, and
then invoke the writeLog function after an action
has been executed.

------------------------------------------------ */

package com.p2p;

import java.io.*;
import java.text.*;
import java.util.Date;

public class LoggerThread extends Thread {

	private final Object writeMutex = new Object();
	
	private File logFile;
	private boolean isActive;
	
	public LoggerThread() {
		isActive = true;
	} // end default constructor
	
	private String getLogFileName() {
		
		StringBuffer sb = new StringBuffer();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		
		sb.append(dateFormat.format(new Date()).toString()).append(".txt");

		return sb.toString();
		
	} // end createLogName
	
	public void stopActivity() {

		this.isActive = false;
		this.interrupt();

	} // end stopActivity
	
	public void writeLog(String s) {
		
		// Ensure proper writing to file by conflicting threads
		synchronized(writeMutex) {
			
			try {

				FileOutputStream fos = new FileOutputStream(logFile, true);	

				StringBuffer sb = new StringBuffer();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				sb.append("[");
				sb.append(dateFormat.format(new Date()).toString()).append("]");
				sb.append(" - ").append(s).append("\n" + "");
				
				fos.write(sb.toString().getBytes());
				
				System.out.println(sb.toString());
				
				fos.close();
				
			}
			catch (IOException ioe) {

				StringBuffer sb = new StringBuffer();
				sb.append("ERROR: Could not write string \"");
				sb.append(s).append("\" to file!");
				System.out.println(sb.toString());
				
			}
			
			catch (Exception e) {
				System.out.println("EXCEPTION: General");
			} // end try-catch
			
		} // end synchronized
		
	} // end writeLog
	
	public void run() {
		
		logFile = new File(this.getLogFileName());		
		System.out.println(logFile.getName());
		
		// Create this logfile if not existing
		try {
			logFile.createNewFile();
		}
		catch (IOException e) {
			System.out.println("ERROR: CreateNewFile");
			return;
		} // end try-catch
		
		// Thread goes to sleep; performs write only when invoked
		while (this.isActive) {
			
			// Sleep until called
			try {
				Thread.sleep(Long.MAX_VALUE);
			}
			catch (InterruptedException ie) {

			} // end try-catch
			
		} // endwhile
		
	} // end run
	
} // end LoggerThread
