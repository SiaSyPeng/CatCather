package com.cs65.gnf.lab3

import android.app.Fragment
import android.util.Log
import android.widget.TextView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.JSONException
import org.json.JSONObject
import org.jetbrains.anko.toast


/**
 * Does something with a list of cats, from a queue, username, password, and game mode
 */
fun getCatList(frag: Fragment, user: String, pass: String, mode: String?) {
    val url = "http://cs65.cs.dartmouth.edu/catlist.pl?name=$user&password=$pass&mode=$mode"

    Volley.newRequestQueue(frag.activity)
            .add(StringRequest(Request.Method.GET,url, Response.Listener<String> { response ->
                Log.d("HIER",response)
                try {
                    val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .add(StringToFloatAdapter())
                            .add(StringToIntAdapter())
                            .add(StringToBoolAdapter())
                            .build()

                    //First, check to see if there is an error message, set it to null if not
                    val errorObject: JSONObject? = try {
                        JSONObject(response) //Try to create an object
                    }
                    catch (e: JSONException) { //If it can't be changed to an object
                        Log.d("JSON","I guess this is our list!")
                        null //set it to null
                    }

                    if (errorObject!=null) { //if there is an error
                        Log.d("SERVOR ERROR",errorObject.getString("error"))
                    }
                    else { //if there was no error
                        //we need to set our Cat List
                        val type = Types.newParameterizedType(List::class.java,Cat::class.java)

                        val catAdaptor: JsonAdapter<List<Cat>> = moshi.adapter(type)

                        val listOfCats = catAdaptor.fromJson(response) //we have our list

                        //TODO Now, do whatever you need to do with that list using the fragment
                        val text: TextView = frag.view.findViewById(R.id.HistoryID)
                        text.text = listOfCats.toString()
                    }

                }
                catch (e: Exception) {e.printStackTrace()}
            },
            Response.ErrorListener { error -> // Handle error cases
                when (error) {
                    is NoConnectionError ->
                        frag.toast("Connection Error")
                    is TimeoutError ->
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
 * Pets a cat, from catID, latitude and longitude of user
 */
fun petCat(frag: Fragment, user: String, pass: String, id: Int) {
    //TODO Get location

    //TODO remove the next two lines
    val lng = -72.28647
    val lat = 43.70729

    val url = "http://cs65.cs.dartmouth.edu/pat.pl?name=$user&password=$pass&catid=$id&lat=$lat&lng=$lng"

    Volley.newRequestQueue(frag.activity)
            .add(StringRequest(Request.Method.GET,url,
                    Response.Listener<String> { response ->

                        Log.d("PETRESPONSE",response)
                        try {
                            val moshi = Moshi.Builder()
                                    .add(KotlinJsonAdapterFactory())
                                    .add(StringToIntAdapter())
                                    .build()

                            Log.d("HI","hi")

                            val petAdaptor = moshi.adapter(PetResult::class.java)

                            val petRes = petAdaptor.fromJson(response)

                            when (petRes?.status) {
                                Status.OK -> {
                                    //TODO whatever is done when pet was success
                                }
                                Status.ERROR -> {
                                    //TODO this toast or whatever else you want to do if it fails
                                    frag.toast(petRes.reason.toString())
                                }
                            }
                            val textView: TextView = frag.view.findViewById(R.id.HistoryID)

                            textView.text = petRes.toString()
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
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