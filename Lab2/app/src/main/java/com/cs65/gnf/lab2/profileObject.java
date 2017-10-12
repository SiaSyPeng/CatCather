package com.cs65.gnf.lab2;

import com.google.gson.annotations.SerializedName;

/**
 * Created by siapeng on 10/10/17.
 */


public class profileObject {
    @SerializedName("name")
    private String uname;
    @SerializedName("realName")
    private String realName;
    @SerializedName("password")
    private String password;
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private String data;

    public profileObject(String uname, String realName, String password, String status, String data){
        this.uname = uname;
        this.realName = realName;
        this.password = password;
        this.status = status;
        this.data = data;
    }

    class dataObject{
        @SerializedName("name")
        private String uname;
        @SerializedName("realName")
        private String realName;
        @SerializedName("password")
        private String password;
    }

    public String getUname() {
        return uname;
    }

    public String getData(){ return data; }

    public String getRealName() {
        return realName;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }
}

