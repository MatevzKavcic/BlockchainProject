package org.example;

import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

public class WriteMeThread extends Thread{

    private PrintWriter out ;

    public WriteMeThread(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void run() {

        // kdr se zacne thread cakej naaaaa to da bos mogu poslat message in komu bos mogu poslat.

    }

    public void newPeer(String message){
        out.println(message);
        out.flush();
    }
}
