package com.cs65.gnf.lab2;

import com.google.gson.annotations.SerializedName;

/**
 * Created by siapeng on 10/10/17.
 */


public class profileObject {

    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private String data;
    @SerializedName("json")
    private profJson json;
    @SerializedName("code")
    private String code;


    public String getData(){ return data; }

    public String getStatus() {return status;}

    public profJson getJson() {return json;}

    public String getCode() {return code;}
}

