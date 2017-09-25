package com.cs65.gnf.lab1

import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.android.flexbox.FlexboxLayout
import android.app.DialogFragment

class MainActivity : AppCompatActivity() {

    private var passwordEntered = false
    private var usernameEntered = false
    private var anythingEntered = false
    private lateinit var pass_rect: Rect
    private lateinit var user_rect: Rect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mPassword: EditText = findViewById(R.id.passwrd)
        mPassword.setOnClickListener({ v: View? ->
            if (v != null) {
                pass_rect = Rect(v.left, v.top, v.right, v.bottom)
                passwordEntered = true
                anythingEntered = true
            }
        })

        val mUsername: EditText = findViewById(R.id.username)
        mUsername.setOnClickListener({ v: View? ->
            if (v!=null) {
                user_rect = Rect(v.left, v.top, v.right, v.bottom)
                usernameEntered = true
                anythingEntered = true
            }
        })

        val screen: FlexboxLayout = findViewById(R.id.screen)
        screen.setOnClickListener({ v: View? ->
            if (v != null) {
                if (passwordEntered)
                    passwordEntered = false
                    passwordConfirm(mPassword.text.toString())
            }
        })
    }

    /**
     * Sets the text values for all our xml elements, as flexboxLayout seems to have trouble reading
     * from the strings.xml file
     */
    //    fun startTextVals() {
//        var new: Button = findViewById(R.id.login_or_clear)
//        new.text = getText(R.string.login_button_text)
//        var new:
//    }


    fun passwordConfirm(orig_text: String) {
        val dialogueBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogueView = inflater.inflate(R.layout.dialog_password, null)
        dialogueBuilder.setView(dialogueView)

        val edt : EditText = dialogueView.findViewById(R.id.pass_confirm)

        val b = dialogueBuilder.create()
        b.show()
    }


    //A lot of buttons

    fun pictButton(v: View) {
        //TODO Go to the picture tool
    }

    fun submitButton(v: View) {
        //TODO Pass all those values to the server
    }

    fun topButton(v: View) {

    }

}