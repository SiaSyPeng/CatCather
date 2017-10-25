package com.cs65.gnf.lab3

import android.app.Fragment
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.net.Uri
import android.preference.Preference
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

/**
 * Fragment for "Play" Tab
 */
class PlayFrag: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play, container, false)
    }
}

/**
 * Fragment for "History" Tab
 */
class HistoryFrag: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_history, container, false)

        val user = "DarthPoseidon"

        val pass = "qwerty"

        val newPass = "newPass"

        val mode = "easy"

        val id = 4

//        getCatList(this,user,pass,mode)
//        petCat(this,user,pass,4)
//        resetList(this,user,pass)
//        changePassword(this,user,newPass,pass)

        // Inflate the layout for this fragment
        return view
    }
}

/**
 * Fragment for "Ranking" Tab
 */
class RankingFrag: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }
}

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
    private val NEWPASS_STRING = "newPass"
    private val MODE_STRING = "mode"

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
                Context.MODE_PRIVATE, R.xml.fragment_preferences, false)

        //Get the preferences we'll be assigning listeners to here
        val signoutPref = findPreference(getString(R.string.prefs_signout_key))
        val aboutPref = findPreference(getString(R.string.prefs_about_key))
        val newGamePref = findPreference(getString(R.string.prefs_newgame_key))
        val modePref = findPreference(getString(R.string.prefs_mode_key))

        // Clean up when the user sign out
        signoutPref.setOnPreferenceClickListener { _ ->

            //Remove everything from user sharedPrefs
            activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
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

        //TODO: send request to server to get new cat list
        newGamePref.setOnPreferenceClickListener { _ ->
            true
        }
        //TODO: depending on different mode, get different result from the server
//        modePref.setOnPreferenceChangeListener(object: Preference.OnPreferenceChangeListener {
//            override fun onPreferenceChange(p0: Preference?, p1: Any?): Boolean {
//                val isOn = p1 as Boolean
//                if (isOn) {
//
//                }
//                else {
//
//                }
//                return true
//            }
//        })
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

    /*
     * When user change setting preference fragements,
     * Update local storage and server storage
     *
     */
    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        val jsonReq = JSONObject()

        // POST Request must have name and password, we get it from sharedpref, which is saved/updated when login
        // And since POST in server right now use overwrite not update, we'll have to keep track of all the fields
        // This can be done by a GET request,
        // or just simply get from sharedpref since we saved data from GET into local when logging in
        val userPrefs = activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        val uName = userPrefs.getString(USER_STRING,null)
        val pass = userPrefs.getString(PASS_STRING,null)
        val realName = userPrefs.getString(NAME_STRING,null)
        var privacy = userPrefs.getBoolean(PRIV_STRING, true)
        var alert = userPrefs.getString(ALER_STRING, null)
        var newPass = userPrefs.getString(NEWPASS_STRING,null)
        var mode = userPrefs.getBoolean(MODE_STRING,false)

        // Update json object
        jsonReq.put("name", uName)
        jsonReq.put("password", pass)
        jsonReq.put("realName", realName)


        when (key) {
        //Checkbox preference for privacy

            getString(R.string.prefs_newpass_key) -> {

                newPass = activity.defaultSharedPreferences.getString(key,null)

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putString(key, newPass)
                        .apply()

                //TODO: send this password to server

            }

            getString(R.string.prefs_mode_key) -> {

                mode = activity.defaultSharedPreferences.getBoolean(key,false)

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putString(key, newPass)
                        .apply()

                //TODO: send this password to server

            }

            getString(R.string.prefs_privacy_key) -> {
                //Get the new setting
                privacy = activity.defaultSharedPreferences.getBoolean(key,true)

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(key,privacy)
                        .apply()

                //Update json request
                try {
                    jsonReq.put(key, privacy)
                    jsonReq.put(ALER_STRING, alert)
                } catch (e: JSONException) {
                    // Warn the user that something is wrong; do not connect
                    Log.d("JSON", "Invalid JSON: " + e.toString())
                    toast("Invalid JSON")
                }
            }

        //List preference for alerts
            getString(R.string.prefs_alert_key) -> {
                alert = activity.defaultSharedPreferences.getString(key,"r")

                //Put it into sharedPrefs storage
                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putString(key,alert)
                        .apply()

                //Update json request
                try {
                    jsonReq.put(key, alert)
                    jsonReq.put(PRIV_STRING,privacy)
                } catch (e: JSONException) {
                    // Warn the user that something is wrong; do not connect
                    Log.d("JSON", "Invalid JSON: " + e.toString())
                    toast("Invalid JSON")

                    return
                }
            }
        }

        // POST jsonrequest to the reserver
        val joRequest = object: JsonObjectRequest(SAVE_URL, // POST is presumed
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
                    toast("Connection Error")
                is TimeoutError ->
                    toast("Timeout Error")
                is AuthFailureError ->
                    toast("AuthFailure Error")
                is NetworkError ->
                    toast("Network Error")
                is ParseError ->
                    toast("Parse Error" )
                is ServerError ->
                    toast("Server Error" )
                else -> toast("Error: " + error)
            }
        }) {
            // This to set custom headers:
            //   https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
            @Throws(AuthFailureError::class)
            override  fun getHeaders(): Map<String, String> {
                run {
                    val params = HashMap<String, String>()
                    // params.put("Accept", "application/json");
                    params.put("Accept-Encoding", "identity")
                    params.put("Content-Type", "application/json")

                    return params
                }
            }
        }

        // Add the request to the RequestQueue.
        queue.add(joRequest)
    }
}
