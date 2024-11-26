package org.example;

import java.io.PrintWriter;

public class WriteMeThread extends Thread{

    private PrintWriter out ;

    public WriteMeThread(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void run() {

        // kdr se zacne thread cakej naaaaa to da bos mogu poslat message in komu bos mogu poslat.

    }

    public void  test(){
        System.out.println("thecuk");
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }
}
