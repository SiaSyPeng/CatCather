package com.cs65.gnf.lab3

import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.JSONException
import org.json.JSONObject
import org.jetbrains.anko.toast


/**
 * Returns a list of cats, from a queue, username, password, and game mode
 */
fun getCatList(queue: RequestQueue, user: String?, pass: String?, mode: String?): List<Cat>? {
    val url = "http://cs65.cs.dartmouth.edu/catlist.pl?name=$user&password=$pass&mode=$mode"

    var listOfCats:List<Cat>? = null

    val req = StringRequest(Request.Method.GET,url,
            Response.Listener<String> { response ->
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

                        listOfCats = catAdaptor.fromJson(response) //we have our list

                    }

                }
                catch (e: Exception) {e.printStackTrace()}
            },
            Response.ErrorListener { error -> // Handle error cases
//                when (error) {
//                    is NoConnectionError ->
//                        toast("Connection Error")
//                    is TimeoutError ->
//                        toast("Timeout Error")
//                    is AuthFailureError ->
//                        toast("AuthFail Error")
//                    is NetworkError ->
//                        toast("Network Error")
//                    is ParseError ->
//                        toast("Parse Error")
//                    is ServerError ->
//                        toast("Server Error")
//                    else -> toast("Error: " + error)
//                }
            }
    )
    queue.add(req)
    return listOfCats
}