package com.cs65.gnf.lab2

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
import android.util.Log
import org.jetbrains.anko.defaultSharedPreferences
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
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
    private val SAVE_URL = "http://cs65.cs.dartmouth.edu/profile.pl"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.fragment_preferences)

        //Set default values (depending on whether values have previously been set)
        PreferenceManager.setDefaultValues(context, USER_PREFS,
                Context.MODE_PRIVATE, R.xml.fragment_preferences, false)



        val signoutPref = findPreference(getString(R.string.prefs_signout_key))
        val aboutPref = findPreference(getString(R.string.prefs_about_key))

        signoutPref.setOnPreferenceClickListener { _ -> //If  user signs out

            //Remove everything from default sharedPrefs
            activity.defaultSharedPreferences
                    .edit()
                    .clear()
                    .apply()

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

        aboutPref.setOnPreferenceClickListener { _ -> //If user clicks on the "about" page
            val url = "https://web.cs.dartmouth.edu" //Dartmouth CS page
            val intent = Intent()

            intent.data = Uri.parse(url)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.applicationContext.startActivity(intent) //sends them to Dartmouth CS page
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

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        val jsonReq = JSONObject()

        when (key) {
            getString(R.string.prefs_privacy_key) -> { //Checkbox preference for privacy

                //Get the new setting
                val privacy = activity.defaultSharedPreferences.getBoolean(key,true)

                //Put it into sharedPrefs
                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(key,privacy)
                        .apply()

                //Put it into server
                try {
                    jsonReq.put(key,privacy)
                } catch (e: JSONException) {
                    // Warn the user that something is wrong; do not connect
                    Log.d("JSON", "Invalid JSON: " + e.toString())
                    toast("Invalid JSON")
                }
            }

            getString(R.string.prefs_alert_key) -> { //List preference for alerts
                val alert = activity.defaultSharedPreferences.getString(key,"r")

                //Put it into sharedPrefs
                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putString(key,alert)
                        .apply()

                //Put it into server
                try {
                    jsonReq.put(key,alert)
                } catch (e: JSONException) {
                    // Warn the user that something is wrong; do not connect
                    Log.d("JSON", "Invalid JSON: " + e.toString())
                    toast("Invalid JSON")

                    return
                }
            }
        }


    }
}
