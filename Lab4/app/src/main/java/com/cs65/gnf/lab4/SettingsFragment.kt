package com.cs65.gnf.lab4

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

/**
 * Fragment for "Settings" Tab
 */
class SettingsFrag: PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val USER_PREFS = "profile_data" //Shared with other activities

    //URL for server
    private val SAVE_URL = "http://cs65.cs.dartmouth.edu/profile.pl"

    // Constant key to get value from sharedPref
    private val USER_STRING = "Username"
    private val PASS_STRING = "Password"
    private val NAME_STRING = "Name"
    private val PRIV_STRING = "privacy"
    private val ALER_STRING = "alert"
    private val MODE_STRING = "mode"
    private val DIS_STRING = "dis"
    private val TIME_STRING = "minTime"

    //volley request
    private lateinit var queue: RequestQueue


    override fun onCreate(savedInstanceState: Bundle?) {
        //init
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.fragment_preferences)

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(activity)

        //Set default values (depending on whether values have previously been set)
        PreferenceManager.setDefaultValues(context, USER_PREFS,
                Context.MODE_PRIVATE, R.xml.fragment_preferences, true)

        //Get the preferences we'll be assigning listeners to here
        val signoutPref = findPreference(getString(R.string.prefs_signout_key))
        val aboutPref = findPreference(getString(R.string.prefs_about_key))
        val newGamePref = findPreference(getString(R.string.prefs_newgame_key))

        val prefs = activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)

        // Clean up when the user sign out
        signoutPref.setOnPreferenceClickListener { _ ->

            //Remove everything from user sharedPrefs
            activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

            //Go back to login
            val i = Intent(activity.applicationContext,LoginActivity::class.java)
            startActivity(i)
            true
        }

        //If user clicks on the "about" page, go to cs web
        aboutPref.setOnPreferenceClickListener { _ ->
            val url = "https://web.cs.dartmouth.edu" //Dartmouth CS page
            val intent = Intent()

            intent.data = Uri.parse(url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.applicationContext.startActivity(intent) //sends them to Dartmouth CS page
            true
        }

        newGamePref.setOnPreferenceClickListener { _ ->
            //Get username and password
            val user = prefs.getString(USER_STRING,null)
            val pass = prefs.getString(PASS_STRING,null)

            //Reset the list
            resetList(this.activity,user,pass)

            true
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * When user change setting preference fragements,
     * Update local storage and server storage
     */
    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        val jsonReq = JSONObject()

        // POST Request must have name and password, we get it from sharedpref, which is saved/updated when login
        // And since POST in server right now use overwrite not update, we'll have to keep track of all the fields
        // This can be done by a GET request,
        // or just simply get from sharedpref since we saved data from GET into local when logging in
        val userPrefs = activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
        val uName = userPrefs.getString(USER_STRING,null)
        val pass = userPrefs.getString(PASS_STRING,null)
        val realName = userPrefs.getString(NAME_STRING,null)
        var privacy = userPrefs.getBoolean(PRIV_STRING, true)
        var alert = userPrefs.getString(ALER_STRING, null)
        var mode = userPrefs.getBoolean(MODE_STRING,false)
        var dis = userPrefs.getString(DIS_STRING, null)
        var time = userPrefs.getString(TIME_STRING,null)

        // Update json object
        jsonReq.put("name", uName)
        jsonReq.put("password", pass)
        jsonReq.put("realName", realName)


        when (key) {

        //Changing password
            getString(R.string.prefs_newpass_key) -> {

                //Get the new password
                val newPass = activity.defaultSharedPreferences.getString(key,null)

                //Send password change req to server
                changePassword(this,uName,pass,newPass) //ask the server to change password
                jsonReq.put("password",newPass) //change the object being sent to the server

                //Change password in local storage
                userPrefs.edit()
                        .putString(PASS_STRING,newPass)
                        .apply()

                //Clear the preference
                activity.defaultSharedPreferences
                        .edit()
                        .putString(key,null)
                        .apply()
            }

        //Changing game mode
            getString(R.string.prefs_mode_key) -> {

                //Get mode (true = hard, false = easy)
                mode = activity.defaultSharedPreferences.getBoolean(key,false)

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(key,mode)
                        .apply()
            }

        //Time
            getString(R.string.prefs_mintime_key) -> {
                time = activity.defaultSharedPreferences.getString(key,"f")

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(key,time)
                        .apply()
            }

        //Distance
            getString(R.string.prefs_dis_key) -> {
                dis = activity.defaultSharedPreferences.getString(key,"m")

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(key,dis)
                        .apply()
            }

        //Privacy
            getString(R.string.prefs_privacy_key) -> {
                //Get the new setting
                privacy = activity.defaultSharedPreferences.getBoolean(key,true)

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(key,privacy)
                        .apply()
            }

        //List preference for alerts
            getString(R.string.prefs_alert_key) -> {
                alert = activity.defaultSharedPreferences.getString(key,"r")

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(key,alert)
                        .apply()
            }
        }

        //Update json request
        try {
            jsonReq.put(PRIV_STRING, privacy)
            jsonReq.put(ALER_STRING, alert)
            jsonReq.put(MODE_STRING,mode)
            jsonReq.put(DIS_STRING, dis)
            jsonReq.put(TIME_STRING,time)
        } catch (e: JSONException) {
            // Warn the user that something is wrong; do not connect
            Log.d("JSON", "Invalid JSON: " + e.toString())
            longToast("Invalid JSON")
        }

        /**
         * Custom class that retries the request if there are  timeout errors etc
         */
        class MyReq(url: String, obj: JSONObject,
                    listener: Response.Listener<JSONObject>,
                    errorListener: Response.ErrorListener) :
                JsonObjectRequest(url,obj, listener, errorListener) {
            override fun setRetryPolicy(retryPolicy: RetryPolicy?): Request<*> {
                return super.setRetryPolicy(DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            }

            // This to set custom headers:
            //   https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                // params.put("Accept", "application/json");
                params.put("Accept-Encoding", "identity")
                params.put("Content-Type", "application/json")

                return params
            }
        }

        // POST jsonrequest to the reserver
        val joRequest = MyReq(SAVE_URL, // POST is presumed
                jsonReq,
                Response.Listener<JSONObject> { response ->
                    try {
                        Log.d("Setting JSON", response.toString() )

                    } catch (e: Exception) {
                        Log.d("Setting JSON", e.toString())
                    }
                }, Response.ErrorListener { error ->
            when (error) { // Handle POST error cases by making a toast
                is NoConnectionError ->
                    longToast("Connection Error")
                is TimeoutError ->
                    longToast("Timeout Error")
                is AuthFailureError ->
                    longToast("AuthFailure Error")
                is NetworkError ->
                    longToast("Network Error")
                is ParseError ->
                    longToast("Parse Error" )
                is ServerError ->
                    longToast("Server Error" )
                else -> longToast("Error: " + error)
            }
        })

        // Add the request to the RequestQueue.
        queue.add(joRequest)
    }
}