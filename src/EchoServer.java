/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.HashMap;

public class EchoServer implements Runnable {
	
	///////////////////////////////////////
	// Attributes
	///////////////////////////////////////	
	private Socket clientSocket;		// socket of the client of a given thread
	private String username;			// user name of the client on thread
	private static HashMap <String,Socket> user_base; 	// what is this???
	private static List <String> user_list;				// sorted list for display
	
	///////////////////////////////////////
	// Constructor
	///////////////////////////////////////
	// Constructor with the socket
	public EchoServer(Socket clientSocket) {
		// a thread now needs a clientSocket
		this.clientSocket = clientSocket; 
	}
	
	///////////////////////////////////////
	// Functions
	///////////////////////////////////////
	// Server log, threads will be logging their state in the console
	static void threadLog(String message) {
		String threadName = Thread.currentThread().getName();
		System.out.printf("%s: %s\n", threadName, message);
	}
	// Join the list of users
	synchronized void join_list() {
		user_base.put(this.username, clientSocket);
		user_list.add(this.username);
		Collections.sort(user_list);
	}
	// Leave the list of users
	synchronized void leave_list() {
		user_base.remove(this.username);
		user_list.remove(user_list.indexOf(this.username));
	}
	///////////////////////////////////////
	// The thread body
	///////////////////////////////////////
	public void run() {
		threadLog(String.format("connected on port# %d", clientSocket.getPort()));
		try {
			// output
			PrintWriter out =
				new PrintWriter(clientSocket.getOutputStream(), true);
			// input
			BufferedReader in =
				new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			// buffer string
			String inputLine;
			
			// Welcome message
			out.println("=======================================");
			out.println("|| Welcome to Ass1 Server!           ||");
			out.println("|| Type HELP for available commands. ||");
			out.println("=======================================");
			
			// Operation loop
			while ((inputLine = in.readLine()) != null) {
				// HELLO
				if (inputLine.compareToIgnoreCase("HELLO") == 0) {
					out.println(" Hello again! Welcome to Ass1 Server!");
				}
				// HELP
				else if (inputLine.compareToIgnoreCase("HELP") == 0) {
					out.println("Available commands:");
					out.println("[ HELP - prints this list ]");
					out.println("[ HELLO - simple greeting ]");
					out.println("[ JOIN - prints this list ]");
					out.println("[ LEAVE - prints this list ]");
					out.println("[ LIST - prints this list ]");
					out.println("[ QUIT - quit, closing the socket ]");
				}
				// JOIN
				else if (inputLine.compareToIgnoreCase("JOIN") == 0) {
					// check if socket already has a username mapped
					if (user_base.containsValue(this.clientSocket) ) {
						out.println(" You are '" + this.username + "'.");
						out.println(" To change your name LEAVE and JOIN again.");
					}
					else {
						out.println(" Enter Username:");
						inputLine = in.readLine();
						// make sure usernames are unique
						while (user_base.containsKey(inputLine)) {
							out.println(" The username '" + inputLine + "' is taken.");
							out.println(" Enter Username:");
							inputLine = in.readLine();
						}
						this.username = inputLine;
						this.join_list();
						out.println(" Success! You joined as '"+ username + "'.");
					}
				}
				// LEAVE
				else if (inputLine.compareToIgnoreCase("LEAVE") == 0) {
					// make sure the user joined
					if (user_base.containsValue(this.clientSocket)) {
						out.println(" No problem. You can join again.");
						this.leave_list();
					}
					else {
						out.println(" Looks like you are not on the list...");
					}
				}
				// LIST
				else if (inputLine.compareToIgnoreCase("LIST") == 0) {
					// attempted to keep the list sorted with String's compareTo
					out.println("You are:");
					out.println(this.clientSocket.toString());
					out.println("People online:");
					
					//out.println(":debug:");
					for (Map.Entry<String, Socket> entry : user_base.entrySet()) {
						out.println(entry.getKey() + "," + entry.getValue().getInetAddress().getHostAddress());
					}
					out.println("end_of_list");
					out.println(user_list.toString());
					
					/*
					Map<Integer, Integer> map = new HashMap<Integer, Integer>();
					for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
						System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
					}
					*/
					
					//out.println(user_list.toString());
					//out.println(user_base.toString());
				}
				// CHAT
				else if (inputLine.compareToIgnoreCase("CHAT") == 0) {
					out.println("---");
					out.println("here's an address");
				}
				// QUIT
				else if (inputLine.compareToIgnoreCase("quit") == 0) {
					threadLog("user disconnected");
					// clean the user_base (force LEAVE the user)
					if (user_base.containsValue(this.clientSocket)) {
						out.println("< assuming you want to LEAVE >");
						this.leave_list();
						threadLog("without LEAVE command");
					}
					// close the streams, socket, and return from the thread
					out.println("Closing connection...");
					out.close();
					in.close();
					clientSocket.close();
					return;
				}
				else {
					out.println(" Command not recognized!");
				}
				Thread.sleep(50); // can be interrupted while asleep
			}
		} catch (InterruptedException e) {
			return; // interrupted == abort
		} catch (IOException e) { // from example code
			System.out.println("Exception caught when trying to listen on port "
					+ clientSocket + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
	///////////////////////////////////////
	// Main thread body
	///////////////////////////////////////
	public static void main(String[] args) throws IOException {
		
		// Basic usage
		if (args.length != 1) {
			System.err.println("Usage: java EchoServer <port number>");
			System.exit(1);
		}
		else {
			threadLog("Starting server...");
		}
		
		// Interrupt handler flag
		boolean flag = false;
		// Read port number from console
		int portNumber = Integer.parseInt(args[0]);
		
		
		// Assign bot sockets ?
		
		// forget about bots for now ...
		
		// Create and populate the user base with test bots
		//user_base = new HashMap<String, Integer>();
		user_base = new HashMap<String, Socket>();
		//user_base.put("MasterBot", 0);
		//user_base.put("ExpertBot", 1);
		//user_base.put("MediumBot", 2);
		//user_base.put("EasyBot", 3);
		
		// Keep a separate list of users (sorted) for display 
		user_list = new ArrayList<String>(user_base.keySet());
		Collections.sort(user_list);
		
		///////////////////////////////////////
		// MAIN BODY
		///////////////////////////////////////
		try {
			ServerSocket serverSocket =	new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			while (true) {
				// There is a client, create a thread 
				if (clientSocket != null) {
					Thread t = new Thread(new EchoServer(clientSocket));
					t.start();
					clientSocket = null;
				}
				// Exception handler
				else if (flag) {
					threadLog("Server stopping...");
					stdIn.close();
					serverSocket.close();
					System.exit(0);
				}
				// Look for another client
				else {
					clientSocket = serverSocket.accept();
				}
				// Sleep to be interrupt-able
				Thread.sleep(50);
			}
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port "
				+ portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		} catch (InterruptedException e) {
			flag = true;
			//return;
		}
	}
}
