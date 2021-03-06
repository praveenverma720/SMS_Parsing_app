package com.praveen.getolly;

import java.io.Serializable;


public class SmsDetail implements Serializable,Comparable<SmsDetail> {
    private String _id;
    private String _address;
    private String _msg;
    private String _readState;
    private String _time;
    private String _folderName;
    public String getId(){
        return _id;
    }
    public String getAddress(){
        return _address;
    }
    public String getMsg(){
        return _msg;
    }
    public String getReadState(){
        return _readState;
    }
    public String getTime(){
        return _time;
    }
    public String getFolderName(){
        return _folderName;
    }

    public void setId(String id){
        _id = id;
    }
    public void setAddress(String address){
        _address = address;
    }
    public void setMsg(String msg){
        _msg = msg;
    }
    public void setReadState(String readState){
        _readState = readState;
    }
    public void setTime(String time){
        _time = time;
    }
    public void setFolderName(String folderName){
        _folderName = folderName;
    }

    @Override
    public int compareTo(SmsDetail another) {
        return getTime().compareTo(another.getTime());
    }
}