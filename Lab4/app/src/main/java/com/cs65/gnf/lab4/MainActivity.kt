package com.cs65.gnf.lab4

import android.os.Bundle
import android.app.FragmentManager
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.support.v13.app.FragmentPagerAdapter
import android.support.v4.app.FragmentActivity
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.json.JSONException
import org.json.JSONObject
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class MainActivity: FragmentActivity() {

    private val tabs: ArrayList<Fragment> = ArrayList()

    //For from shared preferences
    private val USER_PREFS = "profile_data" //Shared with other activities
    private val USER_STRING = "Username"
    private val PASS_STRING = "Password"
    private val MODE_STRING = "mode"
    private val READY_STRING = "ready"

    //For internal storage
    private val CAT_LIST_FILE = "cat_list"

    private val BROADCAST_ACTION = "com.cs65.gnf.lab4.ready"

    var catList: ArrayList<Cat>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //Init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Assign global vars
        val tabStrip: SlidingTabLayout = findViewById(R.id.tabs)
        val mViewPager: ViewPager = findViewById(R.id.pager)
        val adapter = TabAdapter(fragmentManager)

        //Add tabs
        tabs.add(PlayFrag())
        tabs.add(CatsFrag())
        tabs.add(SettingsFrag())

        //Assign adapter (see below)
        mViewPager.adapter = adapter

        //Set tab to evenly distribute, and then connect it to the viewPager
        tabStrip.setDistributeEvenly(true)
        tabStrip.setViewPager(mViewPager)

        //And now we'll get the list of cats from the internet, and save it to internal storage for
        //catsFrag, playFrag, etc.

        //First, make sure that the cat list has not already been saved to internal storage
        val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        val ready = prefs.getBoolean(READY_STRING,false)
        //If it's not been readied, then get the cat list and save to internal storage
        if (!ready) {

            // get the username, password and mode
            val user = prefs.getString(USER_STRING,null)
            val pass = prefs.getString(PASS_STRING,null)

            //mode as string is "hard" if mode is true, "easy" otherwise
            val mode = if (prefs.getBoolean(MODE_STRING,false)) "hard" else "easy"

            //Make the URL
            val listUrl = "http://cs65.cs.dartmouth.edu/catlist.pl?name=$user&password=$pass&mode=$mode"

            //Open volley req (end result of this is saving the cat list to internal storage)
            Volley.newRequestQueue(this)
                    .add(
                            object : StringRequest(Request.Method.GET,listUrl,
                                    Response.Listener { response ->

                                        //Build the Moshi, adding all needed adapters
                                        val moshi = Moshi.Builder()
                                                .add(KotlinJsonAdapterFactory())
                                                .add(StringToDoubleAdapter())
                                                .add(StringToIntAdapter())
                                                .add(StringToBoolAdapter())
                                                .build()

                                        //Try to change to a JSON object
                                        //If this succeeds, then the JSON was an error object
                                        val errorObject: JSONObject? = try {
                                            JSONObject(response)
                                        }

                                        //If there's a JSONException it may be a list
                                        catch (e: JSONException) {
                                            null //set the "error object" to null
                                        }

                                        if (errorObject!=null) { //if there was an error object made
                                            Log.d("SERVOR ERROR",errorObject.getString("error"))
                                        }
                                        else { //if no error object was found we can set our cat list
                                            val type = Types.newParameterizedType(List::class.java,Cat::class.java)

                                            val catAdaptor: JsonAdapter<List<Cat>> = moshi.adapter(type)

                                            //Set the list of cats
                                             catList = ArrayList(catAdaptor.fromJson(response))

                                            if (catList==null) { //if the list cannot be made
                                                Log.d("ERROR","List of cats not found")
                                            }
                                            else {
                                                //Open and write to the file
                                                val fos = openFileOutput(CAT_LIST_FILE,Context.MODE_PRIVATE)
                                                val oos = ObjectOutputStream(fos)
                                                doAsync {
                                                    oos.writeObject(catList)
                                                    oos.close()
                                                    fos.close()

                                                    //Set ready to true so this doesn't keep
                                                    //being called
                                                    prefs.edit()
                                                            .putBoolean(READY_STRING,true)
                                                            .apply()

                                                    //Send a broadcast to everyone listening, so that
                                                    //people waiting for the cat list know they can
                                                    //get it now
                                                    val intent = Intent()
                                                    intent.action = BROADCAST_ACTION

                                                    LocalBroadcastManager
                                                            .getInstance(applicationContext)
                                                            .sendBroadcast(intent)
                                                }
                                            }
                                        }
                                    },
                                    Response.ErrorListener { error -> // Handle error cases
                                        when (error) {
                                            is NoConnectionError ->
                                                longToast("Connection Error")
                                            is TimeoutError ->
                                                longToast("Timeout Error")
                                            is AuthFailureError ->
                                                longToast("AuthFail Error")
                                            is NetworkError ->
                                                longToast("Network Error")
                                            is ParseError ->
                                                longToast("Parse Error")
                                            is ServerError ->
                                                longToast("Server Error")
                                            else -> longToast("Error: " + error)
                                        }
                                    }
                            ) {
                                override fun setRetryPolicy(retryPolicy: RetryPolicy?): Request<*> {
                                    return super.setRetryPolicy(DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                                            2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
                                }
                            }
                    )
        }
        else { //get from internal storage to this activity
            val fis = openFileInput(CAT_LIST_FILE)
            val ois = ObjectInputStream(fis)
            catList = ois.readObject() as ArrayList<Cat>
            }
        }

    /**
     * Adapter for the tab in this activity
     */
    inner class TabAdapter (fm: FragmentManager): FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return tabs.size
        }

        override fun getItem(position: Int): Fragment {
            return tabs[position]
        }

        // Four fields for tab
        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Play"
                1 -> "Cats"
                2 -> "Settings"
                else -> null
            }
        }
    }

    /*
     * OnClick Play button
     * Will direct user to Map Activity
     */
    fun toPlay(v: View) {
        val intent = Intent(applicationContext,MapActivity::class.java)
        startActivity(intent)
    }

    /*
     * Onclick Reset button
     * reset cat list
     */
    fun toReset(v: View) {
        val user = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE).getString(USER_STRING, null)
        val pass = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE).getString(PASS_STRING, null)

        resetList(this,user,pass)

    }

}