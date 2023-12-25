/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFTP_UDP;

import java.io.*;
import java.net.*;

public class TFTP_Server {
    public static void main(String[] args) throws Exception{
        
        int len = 516;
        byte[] buf = new byte[len];
        int port = 4970;
        DatagramSocket datagramsocket = new DatagramSocket();
        SocketAddress socketaddr = new InetSocketAddress("0.0.0.0", port);
        datagramsocket.bind(socketaddr);
        datagramsocket.setReuseAddress(true);
        
        while(true){
            DatagramPacket recvPacket = new DatagramPacket(buf,buf.length);
            datagramsocket.receive(recvPacket);
            TFTP_Server_Thread server = new TFTP_Server_Thread(recvPacket);
            server.start();
        }
    }
}
