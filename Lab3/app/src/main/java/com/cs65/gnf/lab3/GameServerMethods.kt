package com.cs65.gnf.lab3

import android.app.Activity
import android.app.Fragment
import android.util.Log
import android.widget.TextView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.jetbrains.anko.toast
import java.lang.StrictMath.pow

/**
 * Pets a cat, from catID, latitude and longitude of user
 */
fun petCat(act: Activity, user: String?, pass: String?, id: Int): PetResult? {
    //TODO Get location

    //TODO remove the next two lines
    val lng = -72.289024
    val lat = 43.70484

    val url = "http://cs65.cs.dartmouth.edu/pat.pl?name=$user&password=$pass&catid=$id&lat=$lat&lng=$lng"

    var petRes: PetResult? = null

    Volley.newRequestQueue(act)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> { response ->

                        Log.d("PETRESPONSE",response)
                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(StringToIntAdapter())
                                .add(StringToStatusAdapter())
                                .build()

                        val petAdaptor = moshi.adapter(PetResult::class.java)

                        petRes = petAdaptor.fromJson(response)

                        if (petRes==null) {
                            Log.d("ERROR","Pat result is null")
                        }
                        else {
                            when (petRes?.status) {
                                Status.OK -> {
                                    val txt: TextView = act.findViewById(R.id.HistoryID)
                                    txt.text = response
                                    //TODO whatever is done when pet was success
                                }
                                Status.ERROR -> {
                                    //TODO this toast or whatever else you want to do if it fails
                                    act.toast(petRes?.reason.toString())
                                }
                            }
                        }
                    },
                    Response.ErrorListener { error -> // Handle error cases
                        when (error) {
                            is NoConnectionError ->
                                act.toast("Connection Error")
                            is TimeoutError->
                                act.toast("Timeout Error")
                            is AuthFailureError ->
                                act.toast("AuthFail Error")
                            is NetworkError ->
                                act.toast("Network Error")
                            is ParseError ->
                                act.toast("Parse Error")
                            is ServerError ->
                                act.toast("Server Error")
                            else -> act.toast("Error: " + error)
                        }
                    }
            ))

    return petRes

}

/**
 * Resets the cat list
 * */
fun resetList(frag: Fragment,user: String?, pass: String?) {
    val url = "http://cs65.cs.dartmouth.edu/resetlist.pl?name=$user&password=$pass"

    Volley.newRequestQueue(frag.activity)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> {response->
                        Log.d("RESETRESPONSE",response)

                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(StringToStatusAdapter())
                                .build()

                        val resetAdaptor = moshi.adapter(OpResult::class.java)

                        val result = resetAdaptor.fromJson(response)

                        if (result==null) {
                            Log.d("ERROR","Reset list failed")
                        }
                        else {
                            when (result.status) {
                                Status.OK -> {
                                    //TODO do whatever needs to be done
                                }
                                Status.ERROR -> {
                                    //TODO Idk if anything else should be done here
                                    Log.d("ERROR",result.error)
                                }
                            }
                        }
                    },
                    Response.ErrorListener { error -> // Handle error cases
    when (error) {
        is NoConnectionError ->
            frag.toast("Connection Error")
        is TimeoutError->
            frag.toast("Timeout Error")
        is AuthFailureError ->
            frag.toast("AuthFail Error")
        is NetworkError ->
            frag.toast("Network Error")
        is ParseError ->
            frag.toast("Parse Error")
        is ServerError ->
            frag.toast("Server Error")
        else -> frag.toast("Error: " + error)
    }
}
))
}

/**
 * Changes the user's password, from username, old password and new password
 * */
fun changePassword(frag: Fragment, user: String?, pass: String?, newPass: String?) {
    val url = "http://cs65.cs.dartmouth.edu/changepass.pl?name=$user&password=$pass&newpass=$newPass"

    Volley.newRequestQueue(frag.activity)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> {response ->

                        Log.d("JSON",response)

                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(StringToStatusAdapter())
                                .build()

                        val passChangeAdapter = moshi.adapter(OpResult::class.java)

                        val result = passChangeAdapter.fromJson(response)

                        if (result==null) {
                            Log.d("ERROR","no result from pass change")
                        }
                        else {
                            //TODO Password has been changed, what else needs to be done
                            frag.toast("Password changed successfully!")
                        }
                    },
                    Response.ErrorListener { error -> // Handle error cases
                        when (error) {
                            is NoConnectionError ->
                                frag.toast("Connection Error")
                            is TimeoutError->
                                frag.toast("Timeout Error")
                            is AuthFailureError ->
                                frag.toast("AuthFail Error")
                            is NetworkError ->
                                frag.toast("Network Error")
                            is ParseError ->
                                frag.toast("Parse Error")
                            is ServerError ->
                                frag.toast("Server Error")
                            else -> frag.toast("Error: " + error)
                        }
                    }
                    ))
}

/**
 * Returns the ID of the closest cat in the cat list
 */
fun getClosestCat(list: List<Cat>): Int {
    //TODO get location

    //TODO delete the bottom two lines of code
    val lng = -72.289034
    val lat = 43.70414

    var closestId = 0
    var closestDist = 9999.9 //start with a high number
    for (kitty in list) { //for each cat
        val dif = pow(lat-kitty.lat,2.0)+pow(lng-kitty.lng,2.0) //get dx^2+dy^2
        if (dif<closestDist) { //if this distance is less than the closest distance
            closestDist = dif
            closestId = kitty.catId
        }
    }

    return closestId
}