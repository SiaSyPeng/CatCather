package com.cs65.gnf.lab2;

import com.google.gson.annotations.SerializedName;

/**
 * Created by siapeng on 10/12/17.
 */

public class profJson {
    @SerializedName("name")
    private String name;
    @SerializedName("realName")
    private String realName;
    @SerializedName("password")
    private String password;

    public String getRealName() {
        return realName;
    }

    public String getPassword() {
        return password;
    }
    public String getname() {
        return name;
    }
}
