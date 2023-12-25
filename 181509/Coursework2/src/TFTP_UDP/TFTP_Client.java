/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFTP_UDP;

import java.net.*;
import java.io.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TFTP_Client {
    
    static InetAddress hostname;
    static boolean Connection = false;
    static int packetLength = 512;
    static int destTID = 69;
    static byte[] mode = "octet".getBytes();
//    int sourceTID;
//    int opcode = 0;
//    int seqNum = 0;
    
    public static void main(String[] args) throws IOException {
        
        DatagramSocket socket;
        DatagramPacket sendPacket;
        DatagramPacket receivePacket;
        
        byte[] buf = new byte[packetLength];
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
        
//        Random r = new Random();
//        int TID = r.nextInt((49151 - 1024) + 1) + 1024;
//        socket = new DatagramSocket(TID);
        
        
        while(true){
            String input = bufReader.readLine().toLowerCase();
            if(input != "" || input != null){
                if(Connection){
                    System.out.println("A connection has been already established with " + hostname.getHostName());
                }
                socket = new DatagramSocket(0);
                try {
                    hostname = InetAddress.getByName(input.split(" ")[0]);
                } catch (UnknownHostException e) {}
                Connection = true;
    //            ByteArrayOutputStream output = new ByteArrayOutputStream();
                String filename = input.split(" ")[1];
                buf = new byte[packetLength+4];
                try {
                    byte[] filebyte = filename.getBytes();
                    int len = 2 + filebyte.length + mode.length + 2;
                    ByteArrayOutputStream output = new ByteArrayOutputStream(len);
                    byte[] opcode = {0,1};
                    try {
                        output.write(opcode);
                        output.write(filebyte);
                        output.write("0".getBytes());
                        output.write(mode);
                        output.write("0".getBytes());
                    } catch (IOException e) {}
                    byte[] packetByte = output.toByteArray();
                    sendPacket = new DatagramPacket(packetByte, packetByte.length, hostname, destTID);
                    socket.send(sendPacket);
                    socket.setSoTimeout(5000);
                } catch(Exception e){System.out.println("Connection problem");}
                boolean receiveMessage = true;
                while (true) {
                    try{
                        receivePacket = new DatagramPacket(buf, buf.length);
                        socket.setSoTimeout(18000);
                        socket.receive(receivePacket);
                        byte[] data = receivePacket.getData();
                        byte[] RRQ = {data[0],data[1]};
                        if(RRQ[1] == 5){
                            byte[] err = new byte[data.length-5];
                            ByteArrayOutputStream output = new ByteArrayOutputStream(err.length);
                            int j = 0;
                            for(int i = 4; i<data.length-1; i++){
                                err[j++] = data[i];
                            }
                            try {
                                output.write(err);
                            } catch (IOException e) {}
                            String errMsg = err.toString();
                            System.out.println(errMsg);
                            break;
                        }
                        if((receivePacket.getLength() < packetLength+4) && (RRQ[1] == 3)){
                            FileOutputStream fileStream = new FileOutputStream(filename);
                            byte[] dataBytes = new byte[packetLength-4];
                            int j = 0;
                            for(int i = 4; i<data.length; i++){
                                dataBytes[j++] = data[i];
                            }
                            ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
                            output.write(dataBytes);
                            fileStream.write(output.toByteArray());
                            fileStream.close();
                            byte[] blockNo = {data[2],data[3]};
                            int size = 2 + blockNo.length;
                            ByteArrayOutputStream os = new ByteArrayOutputStream(size);
                            byte[] ack = {0,4};
                            try {
                                os.write(ack);
                                os.write(blockNo);
                            } catch (IOException e) {}
                            byte[] ackByte = os.toByteArray();
                            DatagramPacket ackPacket = new DatagramPacket(ackByte, ackByte.length, hostname, receivePacket.getPort());
                            socket.send(ackPacket);

                            System.out.println("End of file transmission");
                            break;
                        }
                        if(RRQ[1] == 3){
                            if(receiveMessage){
                                receiveMessage = false;
                            }
                            byte[] dataBytes = new byte[packetLength-4];
                            int j = 0;
                            for(int i = 4; i<data.length; i++){
                                dataBytes[j++] = data[i];
                            }
                            ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
                            output.write(dataBytes);
                            byte[] blockNo = {data[2],data[3]};
                            int size = 2 + blockNo.length;
                            ByteArrayOutputStream os = new ByteArrayOutputStream(size);
                            byte[] ack = {0,4};
                            try {
                                os.write(ack);
                                os.write(blockNo);
                            } catch (IOException e) {}
                            byte[] ackByte = os.toByteArray();
                            DatagramPacket ackPacket = new DatagramPacket(ackByte, ackByte.length, hostname, receivePacket.getPort());
                            socket.send(ackPacket);
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("System timeout flag");
                        break;
                    }
                } 
            }else if (input == "" || input == null){
                if(Connection){
                    Connection = false;
                }
            }
        }
    }    
}
