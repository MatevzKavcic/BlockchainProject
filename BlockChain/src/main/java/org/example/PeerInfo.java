package org.example;

import java.net.Socket;

public class PeerInfo {
    private Socket socket;
    private WriteMeThread thread; // Reference to the thread

    private int serverPort;

    public PeerInfo(Socket socket, WriteMeThread thread, int serverPort) {
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


    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
