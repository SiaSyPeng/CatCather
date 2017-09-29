package com.cs65.gnf.lab1

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import at.markushi.ui.CircleButton
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity() {

    var anythingEntered = false
    val IMAGE_REQUEST_CODE = 1001
    val CROP_REQUEST_CODE = 2002
    val PERMISSIONS_REQUEST_CODE = 101
    lateinit var transitoryImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mPic: CircleButton = findViewById(R.id.pict_button)

        //If anything starts to be entered, the Clear button will change to the login button
        val mUsername: EditText = findViewById(R.id.username)
        mUsername.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                enterAnything(p0?.length!=0)
                //TODO check availability
                //TODO possibly change the text value of R.id.availability
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        val mName: EditText = findViewById(R.id.full_name)
        mName.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {enterAnything(p0?.length!=0)}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        val mPassword: EditText = findViewById(R.id.passwrd)
        mPassword.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {enterAnything(p0?.length!=0)}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        //Also once the password loses focus it'll trigger the dialog
        mPassword.setOnFocusChangeListener({v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                passwordConfirm(mPassword.text.toString())
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        val mUsername: EditText = findViewById(R.id.username)
        val mName: EditText = findViewById(R.id.full_name)
        val mPassword: EditText = findViewById(R.id.passwrd)
        val mPic: CircleButton = findViewById(R.id.pict_button)

        outState?.putString("mUsername",mUsername.text.toString())
        outState?.putString("mName",mName.text.toString())
        outState?.putString("mPassword",mPassword.text.toString())
        outState?.putParcelable("mPic",(mPic.drawable as BitmapDrawable).bitmap)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)


        val mUsername: EditText = findViewById(R.id.username)
        val mName: EditText = findViewById(R.id.full_name)
        val mPassword: EditText = findViewById(R.id.passwrd)
        val mPic: CircleButton = findViewById(R.id.pict_button)

        mUsername.setText(savedInstanceState.getString("mUsername",null))
        mName.setText(savedInstanceState.getString("mName"),null)
        mPassword.setText(savedInstanceState.getString("mPassword"),null)
        mPic.setImageBitmap(savedInstanceState.getParcelable("mPic"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val extras = data?.extras
        if (resultCode==RESULT_OK) {
            when (requestCode) {
                IMAGE_REQUEST_CODE -> {
                    val cropIntent = Intent(applicationContext,CropActivity::class.java)
                    cropIntent.putExtra("image",extras?.get("data") as Bitmap)
                    startActivityForResult(cropIntent,CROP_REQUEST_CODE)
                }
                CROP_REQUEST_CODE -> {
                    val mPic: CircleButton = findViewById(R.id.pict_button)
                    transitoryImage= extras?.get("cropped_image") as Bitmap
                    mPic.setImageBitmap(toRoundDrawable(transitoryImage)?.bitmap)
                }
            }
        }
    }

    /**
     * Changes Bitmaps and Drawables to Round Drawables
     */
    fun toRoundDrawable(orig : Any): RoundedBitmapDrawable? {
        when (orig) {
            is Bitmap -> {
                val circleBitmap = RoundedBitmapDrawableFactory.create(resources,orig)
                circleBitmap.isCircular = true
                return circleBitmap
            }
            is Drawable -> {
                val bitmap = (orig as BitmapDrawable).bitmap
                val circleBitmap = RoundedBitmapDrawableFactory.create(resources,bitmap)
                circleBitmap.isCircular = true
                return circleBitmap
            }
            else -> return null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                            PERMISSIONS_REQUEST_CODE)
                }
                return
            }
        }
    }
    /**
     * Helper function for top button functionality
     */
    fun enterAnything(entered: Boolean) {
        anythingEntered = entered
        val topButton: Button = findViewById(R.id.login_or_clear)
        topButton.text =  if (entered) "Clear" else "Login"
    }

    /**
     * Helper function to call the password confirmation dialog
     */
    fun passwordConfirm(orig_text: String) {
        //TODO Make a dialog
    }

    //A lot of buttons

    fun loginOrClearButton(v: View) {
        if (anythingEntered) {
            val mUsername: EditText = findViewById(R.id.username)
            val mName: EditText = findViewById(R.id.full_name)
            val mPassword: EditText = findViewById(R.id.passwrd)
            val mPic: CircleButton = findViewById(R.id.pict_button)

            mUsername.text = null
            mName.text = null
            mPassword.text = null
            mPic.setImageResource(R.drawable.cat_cut)
            enterAnything(false)

            //Hide Keyboard
            val view = currentFocus
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromInputMethod(view.windowToken, 0)
        }
        else {
            //TODO Open the view to login
        }
    }

    fun pictButton(v: View) {
        //Ask for permission for the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    PERMISSIONS_REQUEST_CODE)
        }




        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        Log.d("CHECK","got here")
        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        Log.d("CHECK","got this far")
        enterAnything(true)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, IMAGE_REQUEST_CODE)
        }
    }

    fun submitButton(v: View) {
        //TODO Pass all those values to the server
    }

}