package com.cs65.gnf.lab4

import android.app.Fragment
import android.content.Context
import android.os.Bundle
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

        return view
    }
}

/**
 * Fragment for "History" Tab
 */
class HistoryFrag: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_history, container, false)
    }
}




