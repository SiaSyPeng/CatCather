package com.cs65.gnf.lab3

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.location.Location
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import org.jetbrains.anko.toast

/**
 * Pets a cat, from catID, latitude and longitude of user
 */
fun petCat(act: Activity, user: String?, pass: String?, id: Int, loc: LatLng?) {

    //Get lat and long
    val lat = loc?.latitude
    val lng = loc?.longitude

    //make the url
    val url = "http://cs65.cs.dartmouth.edu/pat.pl?name=$user&password=$pass&catid=$id&lat=$lat&lng=$lng"

    //open a request
    Volley.newRequestQueue(act)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> { response ->

                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(StringToIntAdapter())
                                .add(StringToStatusAdapter())
                                .build()

                        val petAdaptor = moshi.adapter(PetResult::class.java)

                        //get the pet result
                        val petRes = petAdaptor.fromJson(response)

                        if (petRes==null) {
                            Log.d("ERROR","Pat result is null") //this shouldn't happen
                        }
                        else {
                            when (petRes.status) {
                                Status.ERROR -> { //If error is returned
                                    act.toast(petRes.reason.toString()) //toast that error
                                }
                                Status.OK -> { //if pet is successful start the success activity
                                    act.toast("mrowwwww")
                                    val intent = Intent(act.applicationContext,SuccessActivity::class.java)
                                    act.startActivity(intent)
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
}

/**
 * Resets the cat list
 * */
fun resetList(frag: Fragment,user: String?, pass: String?) {
    //create url
    val url = "http://cs65.cs.dartmouth.edu/resetlist.pl?name=$user&password=$pass"

    //start volley request
    Volley.newRequestQueue(frag.activity)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> {response->

                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(StringToStatusAdapter())
                                .build()

                        val resetAdaptor = moshi.adapter(OpResult::class.java)

                        //get result
                        val result = resetAdaptor.fromJson(response)

                        if (result==null) {
                            Log.d("ERROR","Reset list failed") //shouldn't happen
                        }
                        else {
                            when (result.status) {
                                Status.OK -> {
                                    frag.toast("New game started!")
                                }
                                Status.ERROR -> {
                                    Log.d("ERROR",result.error)
                                    frag.toast("Error starting a new game")
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
    //make the url
    val url = "http://cs65.cs.dartmouth.edu/changepass.pl?name=$user&password=$pass&newpass=$newPass"

    //start a volley request
    Volley.newRequestQueue(frag.activity)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> {response ->

                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .add(StringToStatusAdapter())
                                .build()

                        val passChangeAdapter = moshi.adapter(OpResult::class.java)

                        val result = passChangeAdapter.fromJson(response)

                        if (result==null) {
                            Log.d("ERROR","no result from pass change") //shouldn't happen
                        }
                        else {
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
fun getClosestCat(list: List<Cat>, loc: LatLng): Int {

    //get user's location
    val lat = loc.latitude
    val lng = loc.longitude

    var closestId = 0 //no such cat id
    var closestDist = Float.MAX_VALUE //start with a high dist
    for (kitty in list) { //for each cat

        //get the distance
        val dist = FloatArray(1)
        Location.distanceBetween(
                lat, lng,
                kitty.lat, kitty.lng,
                dist)

        if (dist[0]<closestDist) { //if this distance is less than the closest distance
            closestDist = dist[0]
            closestId = kitty.catId
        }
    }

    return closestId //returns the closest cat's ID
}