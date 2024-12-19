package org.example;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ListenToMeThread implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private final BlockingQueue<String> messageQueue;


    public ListenToMeThread(Socket socket, BufferedReader in, BlockingQueue<String> messageQueue) {
        this.socket = socket;
        this.in = in;
        this.messageQueue = messageQueue;
    }



    @Override
    public void run() {

        try {
            String message;
            while ((message = in.readLine()) != null) {
                messageQueue.put(message); // Add message to queue
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
