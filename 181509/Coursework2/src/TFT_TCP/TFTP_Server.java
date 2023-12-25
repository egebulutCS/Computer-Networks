/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TFT_TCP;

import java.io.*;
import java.net.*;

public class TFTP_Server {
    
    public static void main(String args[]) throws Exception{
        while(true){
            int port = 5678;
            ServerSocket serverSocket = new ServerSocket(port);
            while(true){
                Socket connection = serverSocket.accept();
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                DataOutputStream socketOutput = new DataOutputStream(connection.getOutputStream());
                String input = socketInput.readLine();
                File file = new File(input);
                byte[] buffer = new byte[(int) file.length()]; 
                socketOutput.write(buffer);
                socketOutput.flush();
                serverSocket.close();
            }
        }
    } 
}
