package com.cs65.gnf.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast

class LoginActivity : Activity() {

    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    val USER_INFO = "USER_INFO_SHARED_PREFS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mUsername = findViewById(R.id.login_username)
        mPassword = findViewById(R.id.login_password)
    }

    fun login(v: View) {
        when {
            mUsername.text.isEmpty() ->
                Toast.makeText(this,"Please enter an Username", Toast.LENGTH_LONG).show()
            mPassword.text.isEmpty() ->
                Toast.makeText(this,"Please enter a Password", Toast.LENGTH_LONG).show()
            else -> {
                //TODO Check the server to make sure this is a correct combo
                if (true) { //TODO Change to "if this was a good combo"
                    val sp = getSharedPreferences(USER_INFO,0)
                    val editor = sp.edit()
                    editor.putString("Username", mUsername.text.toString())
                    editor.putString("Password", mPassword.text.toString())
                    //TODO Create an intent and call the actual game's main activity
                }
            }
        }
    }

    fun toSignupPage(v: View) {
        val intent = Intent(this,SignupActivity::class.java)
        startActivity(intent)
    }
}
