package transport;

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
     * Stop-and-Wait Protocol
     * Description:
     * 
     * This protocol is used for sending single packet and blocking until receiving the acknowledgement for it from the receiver.
     * The block is implemented with the msg value. If it is null, then the system is not blocked. The payload is given from the 
     * application layer. After confirming there are no packets being transmitted at the given time, the system becomes blocked 
     * until the packet is sent successfully. It is then wrapped into a package, with a sequence number, acknowledgement number and 
     * checksum attached. When ready, packet is sent to the receiver through the transport layer and a timer starts to count down. On 
     * the receiver side, the checksum value is extracted from the packet and recalculated by the same checksum generation algorithm 
     * to confirm the packet was sent without corruption. Then the packet is sent back with the corresponding acknowledgement. It is 
     * then checked if it matches the initial acknowledgement. In that case, the lock is released, timer is stopped, sequence number 
     * and acknowledgement number is increased in order to make the system ready for the next incoming message from the application,
     * if there is one. If a packet is lost or corrupted while travelling to the receiver, since no packets will be sent back, a timeout 
     * will occur and the packet will be sent again. If a packet is lost or corrupted on its way back from the receiever to sender, the 
     * acknowledgement will be checked for confirmation, if it fails, a timeout will occur since the timer will not be stopped. Thus, 
     * the system only sends one packet and retries until it is certainly known to be transmitted correctly.
     *
     */
    
    int seq;    // Variable to monitor the sequence number
    int ack;    // Variable to monitor the acknowledgement number
    int checksum;   // Variable to store the calculated checksum value
    Packet p;   // Variable to store the packet created
    Message msg;    // Variable to store the payload of the current packet created
    
    // checksum is created in this method by adding up sequence number, acknowledgement
    // number and every character's ascii number in the payload.
    public int generateChecksum(int seqNo, int ackNo, String msg){
        char[] chars = msg.toCharArray();   // Create and array with each char in the payload string.
        int payload = 0;
        for(char c:chars){
            payload += c;   // Add each ascii number together.
        }
        return seqNo + ackNo + payload;     // Return the checksum value as a total of three variables.
    }
    
    
    // This is the constructor.  Don't touch!
    public Sender(int entityName) {
        super(entityName);
    }

    // This method will be called once, before any of your other sender-side methods are called. 
    // It can be used to do any required initialisation (e.g. of member variables you add to control the state of the sender).
    @Override
    public void init() {
        seq = 0;    // Sequence number starts from 0.
        ack = 0;    // Base of the window starts from 0.
        msg = null; // Initial message is empty
    }
    
    // This method will be called whenever the app layer at the sender has a message to send.  
    // The job of your protocol is to ensure that the data in such a message is delivered in-order, and correctly, to the receiving application layer.
    @Override
    public void output(Message message) {
        if(msg==null){      // Check if message is empty to determine whether there are any messages being sent at the moment...
            msg = message;      // Store the message that has to be sent
            deliverData(message.getData());     // Run deliverData with the payload
            checksum = generateChecksum(seq, ack, message.getData());   // Calculate and store the checksum value for the packet
            p = new Packet(seq, ack, checksum, message.getData());  // Create and store the packet
            udtSend(p);     // Send the packet to the receiver
            startTimer(40);     // Start the timer for the packet. 40 seconds.
        }
    }
    
    
    // This method will be called whenever a packet sent from the receiver (i.e. as a result of a udtSend() being done by a receiver procedure) arrives at the sender.  
    // "packet" is the (possibly corrupted) packet sent from the receiver.
    @Override
    public void input(Packet packet) {
        if(ack == packet.getAcknum()){  // Check if the acknowledgement was sent correctly from the receiver...
            stopTimer();    // Stop the timer
            seq++;      // Increase the sequence number
            ack++;      // Increase the acknowledgement number
            msg = null;     // release the message lock
        }
    }
    
    
    // This method will be called when the senders's timer expires (thus generating a timer interrupt). 
    // You'll probably want to use this method to control the retransmission of packets. 
    // See startTimer() and stopTimer(), above, for how the timer is started and stopped. 
    @Override
    public void timerInterrupt() {
        udtSend(p);    // Send the packet that is still stored in the system.
    }
}
