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

public class TestClient implements Runnable {

	///////////////////////////////////////
	// Attributes
	///////////////////////////////////////
	// TestClient will have a listenSocket and accept chatSocket
	private ServerSocket listenSocket;
	private static Socket chatSocket;
	
	///////////////////////////////////////
	// Constructor
	///////////////////////////////////////
	// Constructor with the socket
	public TestClient(ServerSocket listenSocket) {
		// a thread now needs a listenSocket
		this.listenSocket = listenSocket; 
	}
	
	///////////////////////////////////////
	// The thread body
	///////////////////////////////////////
	public void run() {
		
		System.out.println("INFO: Listener thread started.");
		
		try {
			Thread.sleep(100);
			chatSocket = this.listenSocket.accept();
			if (chatSocket != null) {
				System.out.println();
				System.out.println("Got a connection on " + chatSocket.getLocalPort());
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
	public static void chat(BufferedReader stdIn) throws IOException {
		try (
            PrintWriter out = new PrintWriter(chatSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
            //BufferedReader chatBuf = new BufferedReader(new InputStreamReader(System.in));
        ) {
        	
        	String chatInput;
        	String chatOutput;

        	// Initiator goes first (wait)
        	Thread.sleep(100);
        	
        	while (true) {
        		
        	// Listen
    		chatOutput = in.readLine();
    		System.out.print("unonimous: ");
    		System.out.println(chatOutput);	
	        	
        	// Talk
    		System.out.print("you: ");
        	chatInput = stdIn.readLine();

        		// Exit condition
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
            		if (serverOutput.compareToIgnoreCase("---") == 0) {
            			serverOutput = in.readLine();
            			System.out.println("*connect*");
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
            	// Accepting chat will indicate a start of conversation and break out of loop 
            	else if (((userInput.compareToIgnoreCase("yes") == 0)
            			|| (userInput.compareToIgnoreCase("y") == 0))
            			&& (chatSocket != null)) {
            		// Start the chat session
            		System.out.println("*     Chat session sterted    *");
            		System.out.println("* Type \"--end\" to end session *");
            		chat(stdIn);
            		t = new Thread(new TestClient(listenSocket));
        			t.start();
            	}
            	//
            	else if (((userInput.compareToIgnoreCase("no") == 0)
            			|| (userInput.compareToIgnoreCase("n") == 0))
            			&& (chatSocket != null)) {
            		// Shake the unwanted socket off
            		chatSocket.close();
            		chatSocket = null; // ensure we stay in the loop
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
