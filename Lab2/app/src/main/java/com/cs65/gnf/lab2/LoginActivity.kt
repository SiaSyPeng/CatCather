package com.cs65.gnf.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import org.jetbrains.anko.toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import java.util.*

class LoginActivity : Activity() {

    //Safely save/retrieve
    private val USER_STRING = "Username"
    private val NAME_STRING = "Name"
    private val PASS_STRING = "Password"
    private val ALERT_STRING = "alert"
    private val PRIVACY_STRING = "privacy"

    //Views needed many times
    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText

    private val USER_INFO = "profile_data" //shared with other activities

    private lateinit var queue: RequestQueue //Used to see if username/password combo is good

    override fun onCreate(savedInstanceState: Bundle?) {
        //init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //set global vars
        mUsername = findViewById(R.id.login_username)
        mPassword = findViewById(R.id.login_password)

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this)
    }

    /**
     * Attempts login
     */
    fun login(v: View) {
        val uname = mUsername.text.toString()
        val pass = mPassword.text.toString()
        when {
            // Check if uname and password entered
            uname.isEmpty() -> {
                toast("Please enter an Username")
                highlight(mUsername)
            }
            pass.isEmpty() -> {
                toast("Please  enter a Password")
                highlight(mPassword)
            }
            else -> {
                // Check the server to make sure this is a correct combo
                val url = "http://cs65.cs.dartmouth.edu/profile.pl?name=$uname&password=$pass"

                // Request a string response from the provided URL.
                val stringRequest = object : StringRequest(Request.Method.GET, url,
                        Response.Listener<String> { response ->
                            try {
                                Log.d("LOGIN RESPONSE", response.toString())
                                // parse the string, based on provided class object as template
                                val gson = GsonBuilder().create()
                                val loginRes = gson.fromJson(response, loginResponse::class.java)
                                // Check the status of login response
                                // if error is null, then get succesful response
                                // if error exists, get failure response
                                // can also do this in java reflection object runtimecheck
                                val error = loginRes.error
                                if (error != null) { // error
                                    val code = loginRes.code // get error type
                                    if (code == "AUTH_FAIL") {
                                        toast("Passwords don't match")
                                    } else if (code == "NAME_NOT_FOUND") {
                                        toast("Username not found")
                                    }
                                } else {
                                    // if no error, authenticated, log in the the home page
                                    // update the newest user infor and preferences from the server
                                    loginSubmit(loginRes.name, loginRes.password, loginRes.privacy, loginRes.alert)
                                }
                            } catch (e: Exception) {
                                Log.d("JSON", e.toString())
                            }
                        },
                        Response.ErrorListener { error -> // Handle error cases
                            when (error) {
                                is NoConnectionError ->
                                    toast("Connection Error")
                                is TimeoutError->
                                    toast("Timeout Error")
                                is AuthFailureError ->
                                    toast("AuthFail Error")
                                is NetworkError ->
                                    toast("Network Error")
                                is ParseError ->
                                    toast("Parse Error")
                                is ServerError ->
                                    toast("Server Error")
                                else -> toast("Error: " + error)
                            }
                        }) {
                }

                // Add the request to the RequestQueue.
                queue.add(stringRequest)

            }
        }
    }
    /**
     * Once log in is successful after checking server
     * Update local storage from server response
     * and
     * Takes you to home page
     */
    fun loginSubmit(uname: String?, pass: String?, privacy: Boolean?, alert: String?){
        // save existent things to local
        val sp = getSharedPreferences(USER_INFO,0)
        val editor = sp.edit()
        if (uname is String) editor.putString(USER_STRING, uname)
        if (pass is String) editor.putString(PASS_STRING, pass)
        if (privacy is Boolean) editor.putBoolean(PRIVACY_STRING, privacy)
        if (alert is String) editor.putString(ALERT_STRING, alert)
        editor.apply()

        //Start main activity
        val i = Intent(applicationContext,MainActivity::class.java)
        startActivity(i)
    }
    
    /**
    * Takes you to sign up page (triggered by clicking "create new account")
    */
    fun toSignupPage(v: View) {
        val intent = Intent(applicationContext,SignupActivity::class.java)
        startActivity(intent)
    }

    /**
     * Saves things when phone is flipped
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(USER_STRING, mUsername.text.toString())
        outState?.putString(PASS_STRING, mPassword.text.toString())
    }

    /**
     * Gets everything back when phone is flipped
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mUsername.setText(savedInstanceState.getString(USER_STRING, null))
        mPassword.setText(savedInstanceState.getString(PASS_STRING, null))
    }

    /**
     * Highlights a view if something was forgotten there
     */
    private fun highlight(v: EditText) {
        //Create a blinking animation
        val blink = AlphaAnimation(1f,0f)
        blink.interpolator = DecelerateInterpolator()
        blink.duration = 1000
        blink.repeatCount = 1

        //Assign that to the view
        v.setBackgroundResource(R.color.colorPrimaryDark)
        v.startAnimation(blink)
        Timer().schedule(object : TimerTask() {
            override fun run() {
                v.setBackgroundResource(R.color.transparent)
            }
        }, 2000)
    }
}
