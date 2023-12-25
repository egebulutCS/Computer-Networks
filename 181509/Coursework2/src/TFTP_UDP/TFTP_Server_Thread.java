/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFTP_UDP;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TFTP_Server_Thread extends Thread {
    
    DatagramPacket pckt;
    SocketAddress permaddr;
    SocketAddress tempaddr;
    
    public TFTP_Server_Thread(DatagramPacket pckt){
        this.pckt = pckt;
        this.permaddr = pckt.getSocketAddress();
        this.tempaddr = pckt.getSocketAddress();
    }
    
    @Override
    public void run(){
        SocketAddress socketAddr;
        DatagramSocket datagramSocket = null;
        
        try {
            while (true){
                datagramSocket = new DatagramSocket(null);
                byte[] data = pckt.getData();
                int opcode = data[0] + data[1];
                
                String filename = "";
                int i = 2;
                char charAti = (char) data[i];
                while(charAti != '0'){
                    filename += charAti;
                    i++;
                    charAti = (char) data[i];
                }
                
                Random r = new Random();
                assignPort:
                for(;;){
                    try{
                        socketAddr = new InetSocketAddress("0.0.0.0", r.nextInt((65534 - 65000 + 1) + 65000));
                        datagramSocket.bind(socketAddr);
                        break assignPort;
                    } catch (BindException e){}
                }
                
                if(opcode == 1){
                    File rrqFile = new File(filename);
                    if(rrqFile.exists()){
                        Path path = Paths.get(filename);
                        ArrayList<byte[]> filePackets = packetPartition(path);
                        datagramSocket.setSoTimeout(4000);
                        iteratePackets: for(byte[] packet:filePackets){
                            int timeout_count = 0;
                            int timeout_limit = 5;
                            sendPackets: for(;;){
                                try{
                                    DatagramPacket sendPacket = new DatagramPacket(packet,packet.length,permaddr);
                                    
                                    byte[] recvData = new byte[516];
                                    DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
                                    datagramSocket.receive(sendPacket);
                                    datagramSocket.receive(recvPacket);
                                    recvData = recvPacket.getData();
                                    byte[] ack = {recvData[2],recvData[3]};
                                    if((ack[0] == packet[2]) && (ack[1] == packet[3])){
                                        break sendPackets;
                                    }
                                }catch(SocketTimeoutException e){
                                    if(timeout_count>timeout_limit){
                                        break iteratePackets;
                                    }
                                    timeout_count++;
                                }
                            }
                        }
                    }
                    else if(opcode == 2){
                        ArrayList<byte[]> receivedPackets = new ArrayList<>();
                        byte[] firstAck = new byte[4];
                        firstAck[0] = 0;
                        firstAck[1] = 4;
                        firstAck[2] = 0;
                        firstAck[3] = 0;
                        
                        DatagramPacket firstAckPacket = new DatagramPacket(firstAck,firstAck.length,permaddr);
                        
                        datagramSocket.send(firstAckPacket);
                        
                        int timeout_count = 0;
                        int timeout_limit = 5;
                        datagramSocket.setSoTimeout(4000);
                        
                        byte[] recvData = null;
                        DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
                        
                        ReceiveSendAck: for(;;){
                            
                            try{
                                datagramSocket.receive(recvPacket);
                                if(recvPacket.getLength()<516){
                                    recvData = Arrays.copyOf(recvPacket.getData(), recvPacket.getLength());
                                } else {
                                    recvData = recvPacket.getData();
                                }
                                byte[] ack = recvPacket.getData();
                                int ackOpcode = ack[0] + ack[1];
                                byte[] ackBlockByte = {ack[2],ack[3]};
                                int ackBlockNo = (ackBlockByte[0]*256)+ackBlockByte[1];
                                if(receivedPackets.size() > 0){
                                    byte[] lastRecvPacket = receivedPackets.get(receivedPackets.size()-1);
                                    int recvBlockNo = (lastRecvPacket[2]*256)+lastRecvPacket[3];
                                    if(recvBlockNo+1 == ackBlockNo){
                                        receivedPackets.add(ack);
                                    }
                                } else {
                                    if(ackBlockNo == 1){
                                        receivedPackets.add(ack);
                                    }
                                }
                                DatagramPacket ackToClient = new DatagramPacket(ack, ack.length, permaddr);
                                datagramSocket.send(ackToClient);
                            } catch(SocketTimeoutException e){}
                            timeout_count++;
                        }
                    }
                }
            }
        }catch(Exception e){}
    }
    
    public byte[] TFTPPacket(byte[] data, int blockNo){
        byte[] packet = new byte[data.length+4];
        packet[0] = 0;
        packet[1] = 3;
        packet[2] = (byte) (blockNo/256);
        packet[3] = (byte) (blockNo % 256);
        for(int i = 4; i<packet.length; i++){
            packet[i] = data[i-4];
        }
        return packet;
    }
    
    public ArrayList<byte[]> packetPartition(Path filepath) throws IOException{
        byte[] fileBytes = Files.readAllBytes(filepath);
        int packetNo = (fileBytes.length/512)+1;
        ArrayList<byte[]> packets = new ArrayList<>();
        for(int i = 0; i < packetNo; i++){
            byte[] data = new byte[512];
            for(int j = 0; j < data.length; j++){
                data[j] = fileBytes[(i*512)+j];
            }
            packets.add(TFTPPacket(data,(i++)));
        }
        int lastIndex = 512*(packetNo-1);
        int remBytes = fileBytes.length - lastIndex;
        byte[] finalPack = new byte[remBytes];
        for(int i = lastIndex; i < fileBytes.length; i++){
            finalPack[i%512] = fileBytes[i];
        }
        packets.add(TFTPPacket(finalPack,packetNo));
        return packets;
    }
    
}
