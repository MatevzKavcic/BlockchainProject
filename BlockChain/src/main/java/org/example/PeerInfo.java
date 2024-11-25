package org.example;

import java.net.Socket;

public class PeerInfo {
    private Socket socket;
    private Thread thread; // Reference to the thread


    private int serverPort;

    public PeerInfo(Socket socket, Thread thread, int serverPort) {
        this.socket = socket;
        this.thread = thread;
        this.serverPort = serverPort;
    }

    public Socket getSocket() {
        return socket;
    }

    public Thread getThread() {
        return thread;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }

}
