package org.example;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageType header;
    private String body;

    private String publicKey;

    private int serverPort;


    // to bo moj prvi message ki se bo poslau po networku da ves public key njegov in njegov server port.
    public Message(MessageType header, String body, String publicKey) {
        this.header = header;
        this.body = body;
        this.publicKey = publicKey;
    }

    public Message(MessageType header, int serverPort) {
        this.header= header;
        this.serverPort=serverPort;
    }

    public MessageType getHeader() {
        return header;
    }

    public void setHeader(MessageType header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "Message{" +
                "header='" + header + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}