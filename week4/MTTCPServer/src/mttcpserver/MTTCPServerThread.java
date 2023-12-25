package mttcpserver;

import java.net.*;
import java.io.*;

public class MTTCPServerThread extends Thread {

    private Socket slaveSocket = null;

    public MTTCPServerThread(Socket socket) {
        super("MTTCPServerThread");
        this.slaveSocket = socket;
    }

    @Override
    // This method is called when we start a Thread object
    // Note that the MTTCPServerThread extends the Thread object
    public void run() {
        // The line that will be echoed back to the client.
        String line;
        
        // All Input/Output from/to the socket will take place through the writer/reader objects defined below.
        // We will not access sockets directly in this example.
        PrintWriter socketOutput;
        BufferedReader socketInput;
        try {
            // We get an OutputStream from the slave socket and use it to construct a PrintWriter object 
            // to write formatted representations of objects to the socket (to send messages to the client through TCP)
            socketOutput = new PrintWriter(slaveSocket.getOutputStream(), true);
            
            // We get an InputStream from the slave socket and use it to construct a BufferedReader object 
            // to read a character input stream from the socket (sent from the client through TCP)
            socketInput = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));
            
            // *******************************************************************************************
            // TODO:
            // Fill the while statement so that lines of characters are read from the socket until EOF is reached 
            // End-Of-File (i.e. returning null) when reading from sockets means that the the connection is closed
            // Use the BufferedReader object that was created above
            // Assign the read characters (the whole line) to the inputLine String object declared above.
            // *******************************************************************************************
            while ((line=socketInput.readLine()) != null) {
                System.out.println("Echoing: " + line + ", which is " + line.length() +  " characters long..");

                // *******************************************************************************************
                // TODO: 
                // Add a line below to send back (through the TCP Socket) the outputLine using the PrintWriter
                // BE CAREFUL to use a method that will add a carriage return at the end of the line so that the other side can know when to stop reading
                // (multiple methods can send data through the PrintWriter object) 
                // *******************************************************************************************
                socketOutput.println(line);
            }
            
            System.err.println("Closing Socket");
            slaveSocket.close();
            
        } catch (IOException e) {
            System.err.println(e);
        }
        // The run method will complete now.
        // The Thread object, which was used to serve this session with a single client
    }
}
