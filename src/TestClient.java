/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;
import java.util.*;

public class TestClient implements Runnable {

	///////////////////////////////////////
	// Attributes
	///////////////////////////////////////
	// TestClient will have a listenSocket and accept chatSocket
	private ServerSocket listenSocket;
	private static Socket chatSocket;
	private static HashMap <String,String> users_online;
	private static Boolean first_turn;
	
	///////////////////////////////////////
	// Constructor
	///////////////////////////////////////
	// Constructor with the socket
	public TestClient(ServerSocket listenSocket) {
		// a thread now needs a listenSocket
		this.listenSocket = listenSocket; 
	}
	
	public static String getKeyByValue(Map<String, String> map, String value) {
	    for (Map.Entry<String, String> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return "unknown user";
	}
	
	///////////////////////////////////////
	// The thread body
	///////////////////////////////////////
	public void run() {
		
		try {
			// listen for a peer connection
			Thread.sleep(100);
			chatSocket = this.listenSocket.accept();
			
			if (chatSocket != null) {
				String invIP = chatSocket.getInetAddress().getHostAddress();
				System.out.println();
				System.out.print("Chat invitation from ");
				System.out.print(invIP);
				System.out.println(" [" + getKeyByValue(users_online, invIP) + "]");
				System.out.print("Start the chat? (yes/no) : ");
				return;
			}
			else {
				System.out.println("Problem...");
			}
		} catch (InterruptedException e) {
			// interrupted == abort
			return;
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on "
					+ listenSocket.toString() + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}
	
	///////////////////////////////////////
	// The CHAT function
	///////////////////////////////////////
	public static void chat(BufferedReader stdIn, String peerName) throws IOException {
		try (
            PrintWriter out = new PrintWriter(chatSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
            //BufferedReader chatBuf = new BufferedReader(new InputStreamReader(System.in));
        ) {
        	
        	String chatInput;
        	String chatOutput;

        	if (first_turn) {
        		// Would be nice to send your name ?
        		// --- here ---
        		// First message
        		System.out.print("you: ");
            	chatInput = stdIn.readLine();
            	out.println(chatInput);
        	}
        	else {
        		// Initiator goes first (wait)
        		Thread.sleep(100);
        	}
        	
        	while (true) {
        		
	        	// Listen
	    		chatOutput = in.readLine();
	    		
		        // Exit condition (received)
	    		if (chatOutput.compareToIgnoreCase("--end") == 0) {
	    			// Close all streams and the socket
            		System.out.println(peerName + " ended conversation, disconnecting...");
            		chatSocket.close();
            		chatSocket = null;
                    out.close();
                    in.close();
                    //chatBuf.close();
                    return;
	    		}
	    		else {
	    			System.out.print(peerName + ": ");
		    		System.out.println(chatOutput);	
	    		}
	    		
	    		
	    		
	        	// Talk
	    		System.out.print("you: ");
	        	chatInput = stdIn.readLine();
	        	
        		// Exit condition (send)
	        	if (chatInput.compareToIgnoreCase("--end") == 0) {
            		// Send the quit command
            		out.println(chatInput);
            		// Wait for a response
            		Thread.sleep(50);
            		while (in.ready()) {
                		System.out.println(in.readLine());
                	}
            		// Close all streams and the socket
            		System.out.println("Conversation is over, disconnecting...");
            		chatSocket.close();
            		chatSocket = null;
                    out.close();
                    in.close();
                    //chatBuf.close();
                    return;
            	}
	        	else {
	        		out.println(chatInput);
	        	}
        	}
        
        } catch (InterruptedException e) {
			return; // interrupted == abort    
        }
	}
	
	///////////////////////////////////////
	// The decline CHAT function
	///////////////////////////////////////
	public static void declineChat(BufferedReader stdIn) throws IOException {
		try (
	            PrintWriter out = new PrintWriter(chatSocket.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
	            //BufferedReader chatBuf = new BufferedReader(new InputStreamReader(System.in));
	        ) {
	        	// send the --end command
				out.println("--end");
				// close everything
				chatSocket.close();
        		chatSocket = null;
                out.close();
                in.close();
	        	
		} catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);    
		}
	        	
	}
	///////////////////////////////////////
	// The main body
	///////////////////////////////////////
    public static void main(String[] args) throws IOException {
        
    	// Basic usage
        if (args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
        // Read host name and port number from console
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        int listenPort = portNumber + 10000;
        // init the first turn flag for chat
        first_turn = false;
        // Create the user_online list
        users_online = new HashMap<String,String>();

        try (
    		Socket serverSocket = new Socket(hostName, portNumber);
        	ServerSocket listenSocket = new ServerSocket(listenPort);
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
        	
        	// Listen for a connection in a separate thread
        	Thread t = new Thread(new TestClient(listenSocket));
			t.start();
        	
        	// go?
        	String userInput;
        	String serverOutput;

        	// Give the server 100ms to print the welcome message
        	Thread.sleep(100);
        	
            while (chatSocket == null) {
            	// See if server has something to say 
            	while (in.ready()) {
            		serverOutput = in.readLine();
            		///////////////////////////////////////////
            		/* check for special signal from server */
            		// populate the online users map
            		if (serverOutput.compareToIgnoreCase("People online:") == 0) {
            			System.out.println(serverOutput);
            			serverOutput = in.readLine();
            			while (serverOutput.compareToIgnoreCase("end_of_list") != 0) {
            				String[] id = serverOutput.split("/"); // split into uname + inet
            				users_online.put(id[0], id[1]);
            				serverOutput = in.readLine();
            			}
            		}
            		else {
            			System.out.println(serverOutput);
            		}
            	}
            	// Prompt
            	System.out.print(">");
            	// Get a command
            	userInput = stdIn.readLine();
            	
            	// QUIT is a special command
            	if (userInput.compareToIgnoreCase("QUIT") == 0) {
            		// Send the quit command
            		out.println(userInput);
            		// Wait for a response
            		Thread.sleep(50);
            		while (in.ready()) {
                		System.out.println(in.readLine());
                	}
            		// Close all streams and the socket
            		System.out.println("Quitting...");
            		serverSocket.close();
                    out.close();
                    in.close();
                    stdIn.close();
                    System.exit(0);
            	}
            	// CHAT command, handled by peers, no server involved
            	else if (userInput.compareToIgnoreCase("CHAT") == 0) {
            		System.out.print(" Enter the name of the user to start the chat: ");
            		userInput = stdIn.readLine();
            		serverSocket.getInetAddress();
					System.out.println("You are ->" + InetAddress.getLocalHost().getHostAddress());
            				//.getInetAddress().getHostAddress());
            		String peerName = new String(userInput);
            		String address = users_online.get(userInput);
            		System.out.println(address);
            		if (address == null) {
            			System.out.printf(" User %s not found!\n", userInput);
            			userInput = "list";
            		}
            		else if (address.compareTo
            				(listenSocket.getInetAddress().getHostAddress()) == 0) {
            			System.out.printf(" Chat with yourself is not supported.\n", userInput);
            			userInput = "list";
            		}
            		else {
            			try {
            				// connect
            				chatSocket = new Socket(address, listenPort);
            				// Start the chat session
                    		System.out.println("*     Chat session started    *");
                    		System.out.println("* Type \"--end\" to end session *");
                    		first_turn = true;
                    		chat(stdIn, peerName);
                    		first_turn = false;
            			} catch (IOException e) {
            				System.err.println("User unreachable.");
            				System.err.println(e.getMessage());
            				return;
            			}
            		}
            	}
            	// debug command, shows the contents of users_online
            	else if (userInput.compareToIgnoreCase("LOCAL") == 0) {
            		for (Map.Entry<String, String> entry : users_online.entrySet()) {
						System.out.println(entry.getKey() + " / " + entry.getValue());
					}
            	}
            	// Accepting chat will indicate a start of conversation and break out of loop 
            	else if (((userInput.compareToIgnoreCase("yes") == 0)
            			|| (userInput.compareToIgnoreCase("y") == 0))
            			&& (chatSocket != null)) {
            		// Start the chat session
            		System.out.println("*     Chat session sterted    *");
            		System.out.println("* Type \"--end\" to end session *");
            		String peerName = 
            			getKeyByValue(users_online, chatSocket.getInetAddress().getHostAddress());
            		chat(stdIn, peerName);
            		t = new Thread(new TestClient(listenSocket));
        			t.start();
            	}
            	//
            	else if (((userInput.compareToIgnoreCase("no") == 0)
            			|| (userInput.compareToIgnoreCase("n") == 0))
            			&& (chatSocket != null)) {
            		// explicitly decline chat
            		declineChat(stdIn);
            		// Listen for a connection in a separate thread (again)
                	t = new Thread(new TestClient(listenSocket));
        			t.start();
            	}
            	// Send the command to the server
            	else {
	                out.println(userInput);
            	}
            	// Give the server time to respond
            	Thread.sleep(100); // makes client thread interrupt-able
            }
            
            System.out.print("][ This is the END ][ ... Chat on port ");
    		System.out.println(chatSocket.getLocalPort());
        } catch (InterruptedException e) {
			return; // interrupted == abort    
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.err.println(e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
		}

        System.out.println("HELLOOOOO");
    }
}
