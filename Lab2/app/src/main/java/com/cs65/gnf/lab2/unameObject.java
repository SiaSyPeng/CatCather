package com.cs65.gnf.lab2;
import com.google.gson.annotations.SerializedName;

/**
 * Created by siapeng on 10/10/17.
 */

public class unameObject {
    @SerializedName("name")
    private String uname;
    @SerializedName("avail")
    private String navail;

    public unameObject(String uname, String navail){
        this.uname = uname;
        this.navail = navail;
    }

    public String getUname(){
        return uname;
    }

    public String getNavail() {
        return navail;
    }
}
