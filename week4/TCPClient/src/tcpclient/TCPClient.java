package tcpclient;

import java.io.*;
import java.net.*;

public class TCPClient {

    // The client takes two user arguments as input: 1) Server IP address in dotted-decimal format, 2) Server TCP port
    // Change the default running configuration in Netbeans to pass the arguments.
    // Given that both processes will run on the same machine for this lab session,
    // the IP address can be the loopback address 127.0.0.1
    public static void main(String[] args) throws IOException {
        Socket echoSocket;
        InetAddress adress;
        int portNumber;
        String userInput;
        String serverResponse;
        
        // All Input/Output from/to the socket will take place through the writer/reader objects defined below.
        // We will not access sockets directly in this example.
        PrintWriter out;
        BufferedReader in;
        // A buffered reader to read characters typed by the user (i.e. from the standard input)
        BufferedReader stdIn;
        

        // Check that both required input arguments are passed.
        if (args.length != 1) {
            System.err.println("Usage: java EchoClient <address> <port>");
            System.exit(1);
        }

        // Assign the first argument to the hostName String object
        adress = InetAddress.getByName("localhost");
        // Assign the second argument to the portNumber variable
        portNumber = Integer.parseInt(args[0]);

        try {
            // *******************************************************************************************
            // TODO:
            // Add a line of code that will instantiate the client Socket (echoSocket) and connect it to the server (must be running)
            // Alternatively, you can use the default constructor to first instantiate the Socket object
            // and then explicitly connect it to the server.
            // *******************************************************************************************
            echoSocket = new Socket(adress, portNumber);
            
            
            // We get an OutputStream from the client socket and use it to construct a PrintWriter object 
            // to write formatted representations of objects to the socket (to send messages to the server through TCP)
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            
            // We get an InputStream from the client socket and use it to construct a BufferedReader object 
            // to read a character input stream from the socket (sent from the server through TCP) 
            in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            
            // We create an InputStreamReader and wrap it with a BufferedReader object to read characters coming through the standard input (typed by the user).
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            
            // Keep reading from the standard input until the user types 'exit'
            // When the client is running, you will type in characters in the consle below
            // A line terminates with a carriage return (i.e. after you pressed enter)
            while (((userInput = stdIn.readLine()).compareTo("exit")) != 0) {
                System.out.println("Requesting to echo: " + userInput + ", which is " + userInput.length() +  " characters long..");
                
                // *******************************************************************************************
                // TODO:
                // Add a line that sends the line of characters to the server
                // This is a bloking call; i.e. the process will block until the whole line (terminated by a carriage return) is written to the stream (i.e. sent through the socket)
                // Don't use the socket directly - we have a wrapped a PrintWriter object around it (check above)
                // *******************************************************************************************
                out.println(userInput);
                                
                // *******************************************************************************************
                // TODO:
                // Add a line that reads a line of characters sent from the server (the server will echo back what we sent above)
                // This is a bloking call; i.e. the process will block until a whole line (terminated by a carriage return) is read
                // Don't use the socket directly - we have a wrapped a PrintWriter object around it (check above)
                // *******************************************************************************************
                serverResponse = in.readLine();
                
                // Print the echoed line
                System.out.println("echo: " + serverResponse);
            }
            
            // User typed 'exit'. Close the socket (effectively tearing down the TCP connection)
            echoSocket.close();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + adress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + adress + ". Is the server running?");
            System.exit(1);
        }
    }
}
