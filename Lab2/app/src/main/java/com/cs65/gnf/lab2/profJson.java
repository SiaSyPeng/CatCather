package com.cs65.gnf.lab2;

import com.google.gson.annotations.SerializedName;

/**
 * Java reflection to catch the POST response from server
 * User when saving profile in signup
 * Helper to get inner fields from sub-json object
 * Created by siapeng on 10/12/17.
 */

public class profJson {
    // Fields User saved
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

    // Getter
    public String getRealName() {
        return realName;
    }

    public String getPassword() {
        return password;
    }

    public String getname() {
        return name;
    }

    public String getAlert() {return alert; }

    public boolean getPrivacy() {return privacy;}

}
