package com.cs65.gnf.lab2;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

/**
 * Created by siapeng on 10/12/17.
 */

public class loginResponse {
    @SerializedName("name")
    private String name;
    @SerializedName("realName")
    private String realName;
    @SerializedName("password")
    private String password;
    @SerializedName("privacy")
    private boolean privacy;
    @SerializedName("alert")
    private String alert;

    @SerializedName("error")
    private String error;
    @SerializedName("code")
    private String code;


    public String getName() {
        return name;
    }

    public String getRealName() {
        return realName;
    }

    public String getPassword() {
        return password;
    }

    public String getAlert() {return alert; }

    public boolean getPrivacy() {return privacy;}

    public String getError() {
        return error;
    }

    public String getCode() {
        return code;
    }
}
