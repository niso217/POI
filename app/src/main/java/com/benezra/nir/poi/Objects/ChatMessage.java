package com.benezra.nir.poi.Objects;

import java.util.Date;

/**
 * Created by Hathibelagal on 7/10/16.
 */
public class ChatMessage {

    private String messageText;
    private String messageUser;
    private long messageTime;
    private String messageImage;


    public ChatMessage(String messageText, String messageUser, String messageImage) {
        this.messageText = messageText;
        this.messageUser = messageUser;
        messageTime = new Date().getTime();
        this.messageImage = messageImage;
    }

    public ChatMessage(){

    }


    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }
}
