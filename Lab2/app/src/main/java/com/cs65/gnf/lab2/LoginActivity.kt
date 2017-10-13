package com.cs65.gnf.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.flexbox.FlexboxLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder

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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mUsername = findViewById(R.id.login_username)
        mPassword = findViewById(R.id.login_password)

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this)

        //Hides keyboard if EditTexts lose focus
        mUsername.setOnFocusChangeListener({v,hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        })
        mPassword.setOnFocusChangeListener({v,hasFocus ->
            if (!hasFocus) hideKeyboard(v)
        })

        val screen: FlexboxLayout = findViewById(R.id.log_in_screen)
        screen.setOnFocusChangeListener { v, _ ->  hideKeyboard(v)}
    }

    /**
     * Attempts login
     */
    fun login(v: View) {
        val uname = mUsername.text.toString()
        val pass = mPassword.text.toString()
        when {
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
                                Log.d("JSON", response.toString())
                                // parse the string, based on provided class object as template
                                val gson = GsonBuilder().create()
                                val loginRes = gson.fromJson(response, loginResponse::class.java)
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
                                    login(loginRes.name, loginRes.password)
                                }
                            } catch (e: Exception) {
                                Log.d("JSON", e.toString())
                            }
                        },
                        Response.ErrorListener { error ->
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
     * Takes you to home page
     */
    fun login(uname: String?, pass: String?){

        // save existent things to local
        val sp = getSharedPreferences(USER_INFO,0)
        val editor = sp.edit()
        if (uname is String) editor.putString(USER_STRING, uname)
        if (pass is String) editor.putString(PASS_STRING, pass)
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

    //Get back everything when phone is flipped
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mUsername.setText(savedInstanceState.getString(USER_STRING, null))
        mPassword.setText(savedInstanceState.getString(PASS_STRING, null))
    }

    /**
     * Helper method that hides keyboard
     */
    private fun hideKeyboard(v:View){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun highlight(v: View) {
        doAsync {
            //Change background to red
            v.setBackgroundColor(getColor(R.color.colorPrimaryDark))

            //Create a blinking animation
            val fadeIn = AlphaAnimation(1f,0f)
            fadeIn.interpolator = DecelerateInterpolator()
            fadeIn.duration = 1000
            fadeIn.repeatCount = 0

            //Assign that to the view
            v.animation = fadeIn

            v.background.alpha = 0
        }
    }
}
