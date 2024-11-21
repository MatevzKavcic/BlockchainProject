package org.example;

import java.net.Socket;

public class PeerInfo {
    private Socket socket;
    private Thread thread; // Reference to the thread

    public PeerInfo(Socket socket, Thread thread) {
        this.socket = socket;
        this.thread = thread;
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
}
