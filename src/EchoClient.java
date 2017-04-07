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

public class EchoClient {
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
        //int listenPort = 3333;

        try (
    		Socket serverSocket = new Socket(hostName, portNumber);
        	ServerSocket listenSocket = new ServerSocket();
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
        	
        	String userInput;
        	String serverOutput;

        	// Give the server 100ms to print the welcome message
        	Thread.sleep(100);
        	
            while (true) {
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
            	// Send the command to the server
            	else {
	                out.println(userInput);
            	}
            	// Listen for a P2P connection
            	Socket chatSocket  = listenSocket.accept();
            	if (chatSocket != null) {
            		out.println("chatSocket");
            	}
            	// Give the server time to respond
            	Thread.sleep(100); // makes client thread interrupt-able
            }
        } catch (InterruptedException e) {
			return; // interrupted == abort    
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } 
    }
}
