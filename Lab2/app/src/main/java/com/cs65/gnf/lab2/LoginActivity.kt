package com.cs65.gnf.lab2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.google.android.flexbox.FlexboxLayout

class LoginActivity : Activity() {

    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    internal val USER_INFO = "profile_data" //can be accessed by other activities
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

    fun login(v: View) {
        Log.d("CYCLE","pressed button")
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

                    //Start main activity
                    val i = Intent(applicationContext,MainActivity::class.java)
                    startActivity(i)

                }
            }
        }
    }

    fun toSignupPage(v: View) {
        val intent = Intent(applicationContext,SignupActivity::class.java)
        startActivity(intent)
    }

    /**
     * Helper method that hides keyboard
     */
    fun hideKeyboard(v:View){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }
}
