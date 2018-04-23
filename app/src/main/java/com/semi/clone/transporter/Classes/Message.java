package com.semi.clone.transporter.Classes;


public class Message {
    private String message, id;

    public Message(){}
    public Message(String message){
        this.message =message;
    }
    public String getMessage() {
        return this.message;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

}
