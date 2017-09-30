package com.cs65.gnf.lab1

import android.Manifest
import android.content.ContentValues
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
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.view.inputmethod.InputMethodManager
import java.io.File


class MainActivity : AppCompatActivity() {

    private var anythingEntered = false
    private val STORAGE_SPACE = Environment.getExternalStorageDirectory().absolutePath + "/cat_app"
    private val IMAGE_REQUEST_CODE = 1001
    private val CROP_REQUEST_CODE = 2002
    private val CAMERA_REQUEST_CODE = 101
    private val WRITE_REQUEST_CODE = 202
    private val READ_REQUEST_CODE = 303
    private lateinit var transitoryImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Ask for permission for the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
        }
        //Ask for permission to write to files
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_REQUEST_CODE)
        }
        //Ask for permission to read files
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_REQUEST_CODE)
        }

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
        mPassword.setOnFocusChangeListener({_, hasFocus: Boolean ->
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
        outState?.putBoolean("anything",anythingEntered)
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
        anythingEntered = savedInstanceState.getBoolean("anything")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode==RESULT_OK) {
            when (requestCode) {
                IMAGE_REQUEST_CODE -> {
                    val pathOfFile = STORAGE_SPACE+"/uncropped.jpg"

                    val cropIntent = Intent(applicationContext,CropActivity::class.java)
                    cropIntent.putExtra("image",pathOfFile)

                    val uri = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID+".provider",croppedSave())
                    cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    startActivityForResult(cropIntent,CROP_REQUEST_CODE)
                }
                CROP_REQUEST_CODE -> {
                    val mPic: CircleButton = findViewById(R.id.pict_button)
                    transitoryImage= BitmapFactory.decodeFile(STORAGE_SPACE+"/cropped.jpg")
                    mPic.setImageBitmap(toRoundDrawable(transitoryImage)?.bitmap)
                }
            }
        }
    }

    /**
     * Changes Bitmaps and Drawables to Round Drawables
     */
    private fun toRoundDrawable(orig : Any): RoundedBitmapDrawable? {
        return when (orig) {
            is Bitmap -> {
                val circleBitmap = RoundedBitmapDrawableFactory.create(resources,orig)
                circleBitmap.isCircular = true
                circleBitmap
            }
            is Drawable -> {
                val bitmap = (orig as BitmapDrawable).bitmap
                val circleBitmap = RoundedBitmapDrawableFactory.create(resources,bitmap)
                circleBitmap.isCircular = true
                circleBitmap
            }
            else -> null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                            CAMERA_REQUEST_CODE)
                }
                return
            }
            WRITE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            WRITE_REQUEST_CODE)
                }
                return
            }
            READ_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            READ_REQUEST_CODE)
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
    private fun passwordConfirm(orig_text: String) {
        val PASS_CONFIRM = "pass_confirm_dialog";

        val manager = fragmentManager
        val dialog = AuthDialog()
        dialog.show( manager, PASS_CONFIRM)

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
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        enterAnything(true)

//        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        val uri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID+".provider",unCroppedSave())


        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, IMAGE_REQUEST_CODE)
        }
    }

    fun submitButton(v: View) {
        //TODO Pass all those values to the server
    }

    private fun unCroppedSave(): File {
        val folder = File(STORAGE_SPACE)

        if (!folder.exists()) folder.mkdir()
        val file =  File(folder,"uncropped.jpg")
        file.createNewFile()
        return file
    }

    private fun croppedSave(): File {
        val folder = File(STORAGE_SPACE)

        val imageFile = File(folder,"uncropped.jpg")
        imageFile.delete()

        val file = File(folder,"user_image.jpg")
        file.createNewFile()
        return file
    }
}