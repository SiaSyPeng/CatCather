package com.cs65.gnf.lab3

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
    }

    fun onAgain(v: View){
        val intent = Intent(applicationContext,MapActivity::class.java)
        startActivity(intent)
    }

    fun onDone(v: View){
        val intent = Intent(applicationContext,MainActivity::class.java)
        startActivity(intent)
    }
}
