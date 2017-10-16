package com.cs65.gnf.lab3;

import com.google.gson.annotations.SerializedName;

/**
 * Java reflection to catch the POST response from server
 * User when saving profile in signup
 * Created by siapeng on 10/10/17.
 */


public class profileObject {

    @SerializedName("status")
    private String status;
    // this will get string response (need to parse again)
    @SerializedName("data")
    private String data;
    // this will get json response
    @SerializedName("json")
    private profJson json;
    // this specifies error case
    @SerializedName("code")
    private String code;

    // Getters
    public String getData(){ return data; }

    public String getStatus() {return status;}

    public profJson getJson() {return json;}

    public String getCode() {return code;}
}

