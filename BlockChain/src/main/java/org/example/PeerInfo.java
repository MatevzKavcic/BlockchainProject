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

    public synchronized Socket getSocket() {
        return socket;
    }

    public synchronized Thread getThread() {
        return thread;
    }

    public synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }

    public synchronized void setThread(Thread thread) {
        this.thread = thread;
    }

    public synchronized  int getServerPort() {
        return serverPort;
    }

    public synchronized  void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
