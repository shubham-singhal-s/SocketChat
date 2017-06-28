package com.bennyhawk.socketchat;

import java.util.Date;

import io.realm.RealmObject;


public class Message extends RealmObject {

    public static final int TYPE_MESSAGE_USER = 1;
    public static final int TYPE_MESSAGE_OTHER = 2;
    public static final int TYPE_LOG = 3;
    public static final int TYPE_MESSAGE_USER_CONTINUE = 4;
    public static final int TYPE_MESSAGE_OTHER_CONTINUE = 5;


    private Date mRecievedDate;
    private String mUsername;
    private String mMessage;
    private int mType;

    //TODO - Add group
/*
    public Date getmRecievedDate() {
        return mRecievedDate;
    }

    public int getmType() {
        return mType;
    }

    public String getmMessage() {
        return mMessage;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmMessage(String mMessage) {
        this.mMessage = mMessage;
    }

    public void setmRecievedDate(Date mRecievedDate) {
        this.mRecievedDate = mRecievedDate;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }
    */

public Message(){

}

    private Message (MessageBuilder builder){
        this.mRecievedDate = builder.mRecievedDate;
        this.mUsername=builder.mUsername;
        this.mMessage=builder.mMessage;
        this.mType=builder.mType;
    }

    public Date getmRecievedDate() {
        return mRecievedDate;
    }

    public String getmUsername() {
        return mUsername;
    }

    public String getmMessage() {
        return mMessage;
    }

    public int getmType() {
        return mType;
    }

    public static class MessageBuilder{
        private final Date mRecievedDate;
        private String mUsername;
        private String mMessage;
        private int mType;
        //TODO - Add group

        public MessageBuilder (Date recievedDate){
            this.mRecievedDate=recievedDate;
        }

        public MessageBuilder type(int type){
            this.mType=type;
            return this;
        }

        public MessageBuilder username(String username){
            this.mUsername=username;
            return this;
        }

        public MessageBuilder message(String message){
            this.mMessage=message;
            return this;
        }

        public Message build(){
            Message message = new Message(this);

            /*Checks on message created. Disabled for development.

            if(message.getmRecievedDate()==null){
                throw new IllegalStateException("Date stamp missing");
            }

            if(!(message.getmType()==1 || message.getmType()==2 || message.getmType()==3)){
                throw new IllegalStateException("Message type invalid");
            }

            if(message.getmUsername().equals("")){
                throw new IllegalStateException("Null username");
            }

            if (message.getmMessage().equals("")){
                throw new IllegalStateException("Null message content");
            }
*/

            return message;
        }

    }


}