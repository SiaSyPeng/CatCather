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
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.*
import java.net.URL
import java.util.HashMap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.Method
import java.net.SocketException

class LoginActivity : Activity() {

    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    internal val USER_INFO = "profile_data" //can be accessed by other activities
    private lateinit var queue: RequestQueue


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

    fun login(v: View) {
        Log.d("CYCLE","pressed button")
        val uname = mUsername.text.toString()
        val pass = mPassword.text.toString()
        when {
            uname.isEmpty() ->
                Toast.makeText(this,"Please enter an Username", Toast.LENGTH_LONG).show()
            pass.isEmpty() ->
                Toast.makeText(this,"Please enter a Password", Toast.LENGTH_LONG).show()
            else -> {
                // Check the server to make sure this is a correct combo

                val url = "http://cs65.cs.dartmouth.edu/profile.pl?name=" + uname + "&password=" + pass;

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
                                    // when is????
                                    if (code == "AUTH_FAIL") {
                                        Toast.makeText(this, "Password doesn't match" , Toast.LENGTH_LONG).show()
                                    } else if (code == "NAME_NOT_FOUND") {
                                        Toast.makeText(this, "Username not found" , Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    // if no error, authenticated, log in the the home page
                                    login(uname, pass)
                                }
                            } catch (e: Exception) {
                                Log.d("JSON", e.toString())
                            }
                        },
                        Response.ErrorListener { error ->
                            when (error) {
                                is NoConnectionError ->
                                    Toast.makeText(this, "Connection Error" , Toast.LENGTH_LONG).show()
                                is TimeoutError->
                                    Toast.makeText(this, "Timeout Error" , Toast.LENGTH_LONG).show()
                                is AuthFailureError ->
                                    Toast.makeText(this, "AuthFailure Error" , Toast.LENGTH_LONG).show()
                                is NetworkError ->
                                    Toast.makeText(this, "Network Error" , Toast.LENGTH_LONG).show()
                                is ParseError ->
                                    Toast.makeText(this, "Parse Error" , Toast.LENGTH_LONG).show()
                                is ServerError ->
                                    Toast.makeText(this, "Server Error" , Toast.LENGTH_LONG).show()
                                else -> Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show()
                            }
                        }) {
                }

                // Add the request to the RequestQueue.
                queue.add(stringRequest)

            }
        }
    }

    fun login(uname: String?, pass: String?){
        // save to local
        val sp = getSharedPreferences(USER_INFO,0)
        val editor = sp.edit()
        editor.putString("Username", uname)
        editor.putString("Password", pass)

        //Start main activity
        val i = Intent(applicationContext,MainActivity::class.java)
        startActivity(i)

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
