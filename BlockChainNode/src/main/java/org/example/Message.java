package org.example;

import java.io.Serializable;
import java.security.PublicKey;

public class Message implements Serializable {
    private MessageType header;
    private String body;

    private String publicKey;

    public Message(MessageType header, String body,String publicKey) {
        this.header = header;
        this.body = body;
        this.publicKey = publicKey;
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
}