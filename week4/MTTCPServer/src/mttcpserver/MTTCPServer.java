package mttcpserver;

import java.net.*;
import java.io.*;

public class MTTCPServer {

    public static void main(String[] args) throws IOException {

        // the port number that the server socket will be bound to
        int portNumber = 10000;

        // The TCP ServerSocket object (master socket)
        ServerSocket masterSocket;
        Socket slaveSocket;

        // *******************************************************************************************
        // TODO:
        // Instantiate the server socket, binding it to the port defined above
        // The easiest way of doing that is to pass the portNumber in the constructor (check the Javadoc links)
        // Otherwise you need to instantiate the serverSocket object without a port and then bind it manually to a SocketAddress
        // *******************************************************************************************
        masterSocket = new ServerSocket(portNumber);

        System.out.println("Server Started...");
        
        // the following will run forever (until interrupted by stopping the application through Netbeans)
        while (true) {
            // *******************************************************************************************
            // TODO: 
            // Add source code below below to accept socket connections. This will be a bloking call; i.e. the process will be blocked until a TCP connection is attempted from a remote host
            // The method should return a Socket object, named slaveSocket.
            // the slaveSocket object will be then passed to a new thread that will handle the connection
            // *******************************************************************************************
            slaveSocket = masterSocket.accept();
            
            System.out.println("Accepted TCP connection from: " + slaveSocket.getInetAddress() + ", " + slaveSocket.getPort() + "...");
            System.out.println("Instantiating and starting new MTTCPServerThread object to handle the connection...");
            
            // *******************************************************************************************
            // instantiate and start a new MTTCPServerThread object with the client socket as an argument
            // check the constructor in the MTTCPServerThread class which extends Thread
            // *******************************************************************************************
            new MTTCPServerThread(slaveSocket).start();
        }
    }
}
