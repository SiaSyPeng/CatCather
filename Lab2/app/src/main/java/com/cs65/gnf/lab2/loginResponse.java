package com.cs65.gnf.lab2;

import com.google.gson.annotations.SerializedName;

/**
 * Created by siapeng on 10/12/17.
 */

public class loginResponse {
    @SerializedName("name")
    private String name;
    @SerializedName("password")
    private String password;
    @SerializedName("realName")
    private String realName;
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

    public String getError() {
        return error;
    }

    public String getCode() {
        return code;
    }
}
