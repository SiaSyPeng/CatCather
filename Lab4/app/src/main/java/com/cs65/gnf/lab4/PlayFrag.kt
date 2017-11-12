package com.cs65.gnf.lab4

import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Fragment for "Play" Tab
 */
class PlayFrag: Fragment() {
    private val USER_PREFS = "profile_data" //Shared with other activities
    private val USER_STRING = "Username"
    private val READY_STRING = "ready"

    private lateinit var broadcastReceiver: BroadcastReceiver

    private val BROADCAST_ACTION = "com.cs65.gnf.lab4.ready"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        broadcastReceiver = MyRecvr()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_play, container, false)

        // using the player name saved in local shared preference
        val mUName= activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE).getString(USER_STRING, " ")

        // Update player name
        val mPlayName: TextView = view.findViewById(R.id.play_name)
        val text = "Hi, $mUName"
        mPlayName.text=text

        val prefs = activity.getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)

        if (!prefs.getBoolean(READY_STRING,false)) { //if not ready
            val i = IntentFilter(BROADCAST_ACTION)

            LocalBroadcastManager.getInstance(activity.applicationContext)
                    .registerReceiver(broadcastReceiver,i) //register broadcast receiver
        }
        else {
            setNumber()
        }
        return view
    }

    /**
     * inner class that is a broadcast receiver, so that when the broadcast is received we can getCats
     */
    inner class MyRecvr : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setNumber()
        }
    }

    /**
     * Function that sets the number of cats the user has access to
     */
    private fun setNumber() {
        val welcome: TextView = view.findViewById(R.id.welcome_message)
        val cats = (activity as MainActivity).catList?.size
        val text = "Welcome! You have $cats to catch!"
        welcome.text = text
    }
}



