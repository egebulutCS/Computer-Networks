package transport;

import java.util.ArrayList;

public class Sender extends NetworkHost {

    /*
     * Predefined Constant (static member variables):
     *
     *   int MAXDATASIZE : the maximum size of the Message data and Packet payload
     *
     *
     * Predefined Member Methods:
     *
     *  void startTimer(double increment):
     *       Starts a timer, which will expire in "increment" time units, causing the interrupt handler to be called.  You should only call this in the Sender class.
     *  void stopTimer():
     *       Stops the timer. You should only call this in the Sender class.
     *  void udtSend(Packet p)
     *       Sends the packet "p" into the network to arrive at other host
     *  void deliverData(String dataSent)
     *       Passes "dataSent" up to app layer. You should only call this in the Receiver class.
     *
     *  Predefined Classes:
     *
     *  NetworkSimulator: Implements the core functionality of the simulator
     *
     *  double getTime()
     *       Returns the current time in the simulator. Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().getTime()
     *  void printEventList()
     *       Prints the current event list to stdout.  Might be useful for debugging. Call it as follows: NetworkSimulator.getInstance().printEventList()
     *
     *  Message: Used to encapsulate a message coming from the application layer
     *    Constructor:
     *      Message(String inputData): 
     *          creates a new Message containing "inputData"
     *    Methods:
     *      void setData(String inputData):
     *          sets an existing Message's data to "inputData"
     *      String getData():
     *          returns the data contained in the message
     *
     *  Packet: Used to encapsulate a packet
     *    Constructors:
     *      Packet (Packet p):
     *          creates a new Packet, which is a copy of "p"
     *      Packet (int seq, int ack, int check, String newPayload):
     *          creates a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and a payload of "newPayload"
     *      Packet (int seq, int ack, int check)
     *          chreate a new Packet with a sequence field of "seq", an ack field of "ack", a checksum field of "check", and an empty payload
     *    Methods:
     *      void setSeqnum(int seqnum)
     *          sets the Packet's sequence field to seqnum
     *      void setAcknum(int acknum)
     *          sets the Packet's ack field to acknum
     *      void setChecksum(int checksum)
     *          sets the Packet's checksum to checksum
     *      void setPayload(String payload) 
     *          sets the Packet's payload to payload
     *      int getSeqnum()
     *          returns the contents of the Packet's sequence field
     *      int getAcknum()
     *          returns the contents of the Packet's ack field
     *      int getChecksum()
     *          returns the checksum of the Packet
     *      String getPayload()
     *          returns the Packet's payload
     *
     */
    
    // Add any necessary class variables here. They can hold state information for the sender. 
    // Also add any necessary methods (e.g. checksum of a String)
    
    /*
     * Go-Back-N Protocol
     * Description:
     * 
     * This protocol is used for sending N number of packets from a buffer. In this assignment N is 8. While 
     * sending the N size of window from the buffer, the system still accepts new payloads given from the
     * application layer by wraping them into new packets and adding them to the buffer. The buffer is an array
     * of fixed sized to 50, instead of a cyclic buffer as in real-life secnarios. Once a message is received
     * from the application layer while the base of the window is empty, it is directly sent through the network
     * layer. If the packet is sent from the output method, the timer is set for one packet. If the packet is
     * sent from the input method, the timer runs for the whole window whether the window is full or not,
     * therefore, single packet time is multiplied with N is the interval for a timeout. In this case, the time
     * for a single packet is set to 40, resulting to 40 * 8 = 320 for the whole window. On the receiver side,
     * there is a single information, expected sequence number. This is to check if the packet received is the
     * packet in order. If it is not, no further actions will be taken by the receiver. In the case it is, packet
     * will be inspected to calculate and to compare checksum values in order to detect corruption. If the data is not
     * corrupted, then a new packet is sent to the sender including the expected sequence number as acknowledgement
     * value. Otherwise, no further action is taken by the receiver. Once the packet reaches the sender, the
     * acknowledgement value is compared to base value whether if it is the latest unacknowledged packet in the window.
     * If it is, then the base value is incremented. Then a final check exists to compare base value with
     * sequence number to determine whether the end of window is reached. If it is the end of window, then
     * the timer is stopped. If it isn't the end of buffer and if there are more packets in the window, then they are
     * sent from the input method. In case a packet is lost or corrupted while travelling from the sender to receiver,
     * then the receiver will not send anything which will cause for a timeout. If the packet is lost or corrupted
     * on the way from the receiver to the sender, then it will fail the check on the sender side, leading to a
     * timeout since the timer will not be stopped. The timeout is handled by resending all the packets in the
     * window starting from the latest unacknowledged packet. Thus, as the system is fed payloads on a rate that
     * the network layer can send, then the packets are sent instantly, otherwise they are added to the buffer to
     * be sent in N sized windows when previous packets are acknowledged and control the failure of transmission by
     * resending the window starting with the latest unacknowledged packet.
     *
     */

    int windowSize;     // Holds the size of the window for the buffer.
    Packet[] buffer;    // Data structure to hold packets and implement buffer.
    
    int lastSeqSent;    // Variable to monitor the next sequence number.
    int index;    // Variable to hold the acknowledgement.
    int base;   // Variable to monitor latest unacknowledged packet (base of the window).
    
    // checksum is created in this method by adding up sequence number, acknowledgement
    // number and every character's ascii number in the payload.
    public int generateChecksum(int seqNo, int ackNo, String msg){
        char[] chars = msg.toCharArray();   // Create and array with each char in the payload string.
        int payload = 0;
        for(char c:chars){
            payload += c;   // Add each ascii number together.
        }
        return seqNo + ackNo + payload; // Return the checksum value as a total of three variables.
    }    
    
    // This is the constructor.  Don't touch!
    public Sender(int entityName) {
        super(entityName);
    }

    // This method will be called once, before any of your other sender-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the sender).
    @Override
    public void init() {
        index = 0;
        lastSeqSent = 0;    // Last sent packet's sequence number starts from 0.
        base = 0;   // Base of the window starts from 0.
        windowSize = 7;     // including 0, setting this variable to 7, gives a window of 8 elements.
        buffer = new Packet[50];    // As given in the description of the task, buffer has a limit of 50 packets.
    }
    
    // This method will be called whenever the app layer at the sender has a message to send.  
    // The job of your protocol is to ensure that the data in such a message is delivered in-order, and correctly, to the receiving application layer.
    @Override
    public void output(Message message) {
        if(index < buffer.length){    // To prevent excessive input from the application layer based on the size of the buffer.
            System.out.println("Adding data to buffer: " + message);    // Printing the payload of the packet that is added to the buffer.
            int checksum = generateChecksum(index, base, message.getData());  // Checksum is generated to be added in the new package.
            buffer[index] = new Packet(index, base, checksum, message.getData());   // Create the new package and add to the buffer, indexed by its sequence number.
            if(base == index){  // If the packet is the base packet in the window...
                startTimer(40);   // Start timer for the first packet sent in the window.
                deliverData(buffer[index].getPayload());    // Call deliverData method with the payload of sent packet.
                udtSend(buffer[index]);   // Send the packet that is created.
                lastSeqSent = buffer[index].getSeqnum();    // Last sent sequence number is set to the last sent packet's sequence number.
            }
            index++;  // Increase the index (next sequence number) for each packet created and added into the buffer.
        }
    }
    
    
    // This method will be called whenever a packet sent from the receiver (i.e. as a result of a udtSend() being done by a receiver procedure) arrives at the sender.  
    // "packet" is the (possibly corrupted) packet sent from the receiver.
    @Override
    public void input(Packet packet) {
        int checksum = packet.getChecksum();    // Extraction of checksum value that is sent with the package into variable.
        if(checksum == generateChecksum(packet.getSeqnum(), packet.getAcknum(), packet.getPayload())){      // Recalculating checksum from the values extracted within the package and comparing it with the checksum transmitted.
            if(base == packet.getAcknum()){     // if the acknowledgement number of received packet is equal to the base value of the window...
                base = packet.getAcknum()+1;    // Increase the latest unacknowledged packet number.
                if(base-1 == lastSeqSent){      // If it is the end of the window...
                    stopTimer();    // Stop the timer.
                    if(base<buffer.length){     // If the window hasn't gone out of the buffer...
                        if(buffer[base]!=null){     // If the buffer's next value isn't null (contains packet)...
                            startTimer(40*windowSize);      // Start timer for the first packet sent in the window.
                            for(int i = base; i<=base+windowSize; i++){     // From the latest unacknowledged packet to end of the window...
                                if((i<buffer.length) && (buffer[i]!=null)){     // If the index isn't out of array size and contain's packet...
                                    deliverData(buffer[i].getPayload());    // Call deliverData method with the payload of sent packet.
                                    udtSend(buffer[i]);     // Send the data found in the index.
                                    lastSeqSent = buffer[i].getSeqnum();    // Last sent sequence number is set to the last sent packet's sequence number.
                                }
                            }
                        }
                    }
                }
            }
        } 
    }
    
    
    // This method will be called when the senders's timer expires (thus generating a timer interrupt). 
    // You'll probably want to use this method to control the retransmission of packets. 
    // See startTimer() and stopTimer(), above, for how the timer is started and stopped. 
    @Override
    public void timerInterrupt() {
        startTimer(40*windowSize);  // Start the timer. 40 seconds per packet in the sent window
        for(int i = base; i <= base+windowSize; i++){    // From the latest unacknowledged packet to end of the window...
            if((i<buffer.length) && (buffer[i]!=null)){     // If the index isn't out of array size and contain's packet...
                deliverData(buffer[i].getPayload());    // Call deliverData method with the payload of sent packet.
                udtSend(buffer[i]);     // Send the data found in the index.
                lastSeqSent = buffer[i].getSeqnum();    // Last sent sequence number is set to the last sent packet's sequence number.
            }           
        }
    }
}
