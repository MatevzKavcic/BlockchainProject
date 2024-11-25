package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ListenToMeThred implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private final BlockingQueue<String> messageQueue;


    public ListenToMeThred (Socket socket, BufferedReader in, BlockingQueue<String> messageQueue) {
        this.socket = socket;
        this.in = in;
        this.messageQueue = messageQueue;
    }



    @Override
    public void run() {

        try {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server recived message : " + message + " from socket "+ socket.getPort());
                messageQueue.put(message); // Add message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void  test(){
        System.out.println("thecuk");
    }
}
