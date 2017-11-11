package com.cs65.gnf.lab4

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.location.Location
import android.support.v4.content.ContextCompat.startActivity
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
                                    act.longToast(petRes.reason.toString()) //toast that error
                                }
                                Status.OK -> { //if pet is successful start the success activity
                                    act.longToast("mrowwwww")
                                    petInternal(act,id)
                                    val intent = Intent(act.applicationContext,SuccessActivity::class.java)
                                    act.startActivity(intent)
                                }
                            }
                        }
                    },
                    Response.ErrorListener { error -> // Handle error cases
                        when (error) {
                            is NoConnectionError ->
                                act.longToast("Connection Error")
                            is TimeoutError->
                                act.longToast("Timeout Error")
                            is AuthFailureError ->
                                act.longToast("AuthFail Error")
                            is NetworkError ->
                                act.longToast("Network Error")
                            is ParseError ->
                                act.longToast("Parse Error")
                            is ServerError ->
                                act.longToast("Server Error")
                            else -> act.longToast("Error: " + error)
                        }
                    }
            ))
}

/**
 * Resets the cat list
 * */
fun resetList(ctx: Context,user: String?, pass: String?) {
    val USER_PREFS = "profile_data" //Shared with other activities
    val READY_STRING = "ready"

    //For internal storage
    val CAT_LIST_FILE = "cat_list"

    //create url
    val url = "http://cs65.cs.dartmouth.edu/resetlist.pl?name=$user&password=$pass"

    //start volley request for resetting list
    Volley.newRequestQueue(ctx)
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
                                    ctx.longToast("New game starting!")

                                    //set ready in shared preferences as not true
                                    ctx.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                                            .edit()
                                            .putBoolean(READY_STRING,false)
                                            .apply()

                                    //delete from internal storage
                                    ctx.deleteFile(CAT_LIST_FILE) //Clear the file

                                    //Restart the main activity
                                    val i = Intent(ctx,MainActivity::class.java)
                                    ctx.startActivity(i)
                                }
                                Status.ERROR -> {
                                    Log.d("ERROR",result.error)
                                    ctx.longToast("Error starting a new game")
                                }
                            }
                        }
                    },
                    Response.ErrorListener { error -> // Handle error cases
    when (error) {
        is NoConnectionError ->
            ctx.longToast("Connection Error")
        is TimeoutError->
            ctx.longToast("Timeout Error")
        is AuthFailureError ->
            ctx.longToast("AuthFail Error")
        is NetworkError ->
            ctx.longToast("Network Error")
        is ParseError ->
            ctx.longToast("Parse Error")
        is ServerError ->
            ctx.longToast("Server Error")
        else -> ctx.longToast("Error: " + error)
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
                            frag.longToast("Password changed successfully!")
                        }
                    },
                    Response.ErrorListener { error -> // Handle error cases
                        when (error) {
                            is NoConnectionError ->
                                frag.longToast("Connection Error")
                            is TimeoutError->
                                frag.longToast("Timeout Error")
                            is AuthFailureError ->
                                frag.longToast("AuthFail Error")
                            is NetworkError ->
                                frag.longToast("Network Error")
                            is ParseError ->
                                frag.longToast("Parse Error")
                            is ServerError ->
                                frag.longToast("Server Error")
                            else -> frag.longToast("Error: " + error)
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

/**
 * Sets a cat in internal storage to petted by pulling up internal storage, finding the correct cat,
 * modifying it, and writing it back to internal storage. Done Async.
 */
fun petInternal(ctx: Context, id: Int) {
    ctx.doAsync {
        val CAT_LIST_FILE = "cat_list"

        //First get the file
        val fis = ctx.openFileInput(CAT_LIST_FILE)
        val ois = ObjectInputStream(fis)

        //Read it
        val listOfCats = ois.readObject() as ArrayList<Cat>
        fis.close()
        ois.close()

        //Find the one to pet and pet it
        listOfCats.filter { it.catId == id }
                .map { it.petted = true }


        ctx.deleteFile(CAT_LIST_FILE) //Clear the file

        //write new file to internal
        val fos = ctx.openFileOutput(CAT_LIST_FILE, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(listOfCats)
        fos.close()
        oos.close()
    }


}