package com.cs65.gnf.lab4

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Called when the app is opened
 */
class LaunchActivity: Activity() {
    private val USER_PREFS = "profile_data" //shared with other activities
    private val USER_STRING = "Username"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
        val user = prefs.getString(USER_STRING, null)

        val intent = if (user == null) //if no user saved
            Intent(applicationContext, LoginActivity::class.java) //launch login
        else  //if there is a user saved
            Intent(applicationContext, MainActivity::class.java) //just open the game

        startActivity(intent)
    }
}