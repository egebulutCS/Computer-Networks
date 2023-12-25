/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFT_TCP;

import java.io.*;
import java.net.*;

public class TFTP_Client{
    
    public static void main(String args[]) throws Exception{
        InetAddress serverAddr = InetAddress.getByName("localhost");
        int port = 5678;
        Socket socket = new Socket(serverAddr, port);
        System.out.println("Input:");
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        String input = userInput.readLine();
        while(!input.isEmpty()){
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            output.writeBytes(input);
            BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = serverInput.readLine();
            while(!response.isEmpty()){
                System.out.println("Server response:");
                break;
            }
            System.out.println("Input:");
            userInput = new BufferedReader(new InputStreamReader(System.in));
            input = userInput.readLine();
        }
        System.out.println("Terminating connection...");
        socket.close();
    } 
}
