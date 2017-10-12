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
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.toast


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

    val USER_PREFS = "user_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.fragment_preferences)

        //Set default values (depending on whether values have previously been set)
        PreferenceManager.setDefaultValues(context, USER_PREFS,
                Context.MODE_PRIVATE, R.xml.fragment_preferences, false)

        //TODO Start with existing values, if any, for the prefs

        val signoutPref = findPreference(getString(R.string.prefs_signout_key))
        val aboutPref = findPreference(getString(R.string.prefs_about_key))

        signoutPref.setOnPreferenceClickListener { _ -> //If  user signs out

            //Remove everything from internal sharedPrefs
            activity.defaultSharedPreferences
                    .edit()
                    .clear()
                    .apply()

            //Remove everything from User sharedPrefs
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
        when (key) {
            getString(R.string.prefs_privacy_key) -> { //Checkbox preference for privacy
                val privacy = activity.defaultSharedPreferences.getBoolean(key,true)

                //TODO send to server

                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean(key,privacy)
                        .apply()

                toast("Displays score to others: " + privacy)
            }
            getString(R.string.prefs_alert_key) -> { //List preference for alerts
                val alert = activity.defaultSharedPreferences.getString(key,"r")

                //TODO Send to server

                activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                        .edit()
                        .putString(key,alert)
                        .apply()
            }
        }
    }
}
