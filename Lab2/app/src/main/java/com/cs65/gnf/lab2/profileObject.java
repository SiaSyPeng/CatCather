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

    public profileObject(String uname, String realName, String password, String status){
        this.uname = uname;
        this.realName = realName;
        this.password = password;
        this.status = status;
    }

    public String getUname() {
        return uname;
    }

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
