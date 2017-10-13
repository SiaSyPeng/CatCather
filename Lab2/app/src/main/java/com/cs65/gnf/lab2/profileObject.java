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
    @SerializedName("privacy")
    private boolean privacy;
    @SerializedName("alert")
    private String alert;



    public String getData(){ return data; }


    public String getAlert() {return alert; }

    public boolean getPrivacy() {return privacy;}

    public String getStatus() {return status;}

    public profJson getJson() {return json;}

}

