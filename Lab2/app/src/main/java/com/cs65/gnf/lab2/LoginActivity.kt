package com.cs65.gnf.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.flexbox.FlexboxLayout
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class LoginActivity : Activity() {

    private val USER_INFO = "USER_INFO_SHARED_PREFS" //same sharedPrefs as other activities

    //Safely save/retrieve stuff when phone is flipped
    private val USER_STRING = "mUsername"
    private val PASS_STRING = "mPassword"

    //Views needed many times
    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mUsername = findViewById(R.id.login_username)
        mPassword = findViewById(R.id.login_password)

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
        when {
            mUsername.text.isEmpty() -> {
                toast("Please enter a username")
                highlight(mUsername)
            }
            mPassword.text.isEmpty() -> {
                toast("Please enter a password")
                highlight(mPassword)
            }
            else -> {
                //TODO Check the server to make sure this is a correct combo
                if (true) { //TODO Change to "if this was a good combo"
                    val sp = getSharedPreferences(USER_INFO,0)
                    val editor = sp.edit()
                    editor.putString(USER_STRING, mUsername.text.toString())
                    editor.putString(PASS_STRING, mPassword.text.toString())
                    //TODO Get Full Name and preferences from Server and put it into sharedPrefs
                    editor.apply()
                    //Start main activity
                    val i = Intent(applicationContext,MainActivity::class.java)
                    startActivity(i)

                }
            }
        }
    }

    /**
     * Takes you to sign up page
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
