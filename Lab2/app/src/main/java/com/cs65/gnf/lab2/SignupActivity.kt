package com.cs65.gnf.lab2

import android.Manifest
import android.app.DialogFragment
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.*


class SignupActivity : AppCompatActivity(), AuthDialog.DialogListener {

    private var anythingEntered = false //(used to help set login button to clear)
    private var ifPassMatch: Boolean = false // (if password has been reentered and matched)
    private var SHARED_PREF = "my_sharedpref"
    private lateinit var mdialog: AuthDialog //Dialog declared globally so it can be accessed later
    private val IMAGE_REQUEST_CODE = 1 //To send intent to Android's camera app
    private val CAMERA_REQUEST_CODE = 2 //To request use of camera
    private val WRITE_REQUEST_CODE = 3 //To request use of writing files
    private val READ_REQUEST_CODE = 4 //To request use of reading files
    private lateinit var mUsername: EditText //Username field
    private lateinit var mName: EditText //Full Name field
    private lateinit var mPassword: EditText //Password field
    private lateinit var mPic: ImageView // User picture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set variables for layout items
        mPic = findViewById(R.id.pict_button)
        mPassword = findViewById(R.id.passwrd)
        mName = findViewById(R.id.full_name)
        mUsername = findViewById(R.id.username)

        //If a picture has previously been saved, set it (defaults to src file specified in xml)
        val file = File(filesDir,"user_image.png")
        if (file.exists()) mPic.setImageBitmap(BitmapFactory.decodeFile(file.path))

        //fill text fields with shared preferences (or, if they don't exist, with null)
        val sp = getSharedPreferences(SHARED_PREF, 0)
        mUsername.setText(sp.getString("Username", null))
        mName.setText(sp.getString("Name", null))
        mPassword.setText(sp.getString("Password", null))

        //keep the booleans as they were when information was last saved
        ifPassMatch=sp.getBoolean("ifPassMatch",false)
        enterAnything(sp.getBoolean("anythingEntered", false))



        //Ask for permission for the camera
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
        }
        //Ask for permission to write to files
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_REQUEST_CODE)
        }
        //Ask for permission to read files
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_REQUEST_CODE)
        }

        //If anything starts to be entered, enterAnything() is called, changing 'Login' to 'Clear'
        //button (see function below)
        mUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                enterAnything(p0?.length != 0)
//                checkSubmit()
                //TODO check availability
                //TODO possibly change the text value of R.id.availability
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        mName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                enterAnything(p0?.length != 0)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        mPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                enterAnything(p0?.length != 0)
                ifPassMatch = false //additionally, if the password changes, it may no longer match
                //the reentered password
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        //Once the password loses focus it'll trigger the dialog
        mPassword.setOnFocusChangeListener({ _, hasFocus: Boolean ->
            if (!hasFocus) {
                passwordConfirm()
            }
        })
    }

    /**
     * Makes sure we don't lose everything when the phone orientation is flipped
     */
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        //saves bools and text fields
        outState?.putString("mUsername", mUsername.text.toString())
        outState?.putString("mName", mName.text.toString())
        outState?.putString("mPassword", mPassword.text.toString())
        outState?.putBoolean("anything", anythingEntered)
        outState?.putBoolean("passMatch",ifPassMatch)

        //Save the picture to internal storage
        var fos: FileOutputStream? = null
        try {
            fos = openFileOutput("temporary_picture.png", Context.MODE_PRIVATE)
            val mBitmap = (mPic.drawable as BitmapDrawable).bitmap
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (fos != null) fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Gets back information after phone is flipped
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        //Set back text fields and booleans
        mUsername.setText(savedInstanceState.getString("mUsername", null))
        mName.setText(savedInstanceState.getString("mName"), null)
        mPassword.setText(savedInstanceState.getString("mPassword"), null)
        enterAnything(savedInstanceState.getBoolean("anything",false))
        ifPassMatch = savedInstanceState.getBoolean("passMatch",false)

        //Open and set picture
        val file = File(filesDir, "temporary_picture.png")
        mPic.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))

        //Delete that file (as it was only meant to be temporary)
        file.delete()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_REQUEST_CODE -> { //When the image has been taken

                    //Start 3rd party cropper
                    CropImage
                            .activity(unCroppedSave()) //call it on the filename the image saved to
                            .setAspectRatio(1, 1) //starts at 1:1 aspect ratio
                            .setFixAspectRatio(true) //aspect ratio cannot change
                            .setCropShape(CropImageView.CropShape.OVAL) //is a circle
                            .start(this)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> { //When we get back our cropped pic
                    val result = CropImage.getActivityResult(data)
                    val uri = result.uri //This comes from usage of this 3rd party app, documented
                    //on their github wiki
                    mPic.setImageBitmap(BitmapFactory.decodeFile(uri.path)) //set the new image
                    File(filesDir, "uncropped.png").delete() //delete the uncropped image
                }
            }
        }
    }

    /**
     * Requests permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else { //if permission wasn't  granted, request it again
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                            CAMERA_REQUEST_CODE)
                }
                return
            }
            WRITE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {//if permission wasn't  granted, request it again
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            WRITE_REQUEST_CODE)
                }
                return
            }
            READ_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {//if permission wasn't  granted, request it again
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
        anythingEntered = entered //changes the boolean (we still need it, if only to be able to
        //store it in sharedPrefs)
        val topButton: Button = findViewById(R.id.login_or_clear)
        topButton.text = if (entered) "Clear" else "Login" //changes text of the top button
    }

    /**
     * Helper function to call the password confirmation dialog
     */
    private fun passwordConfirm() {
        if(!ifPassMatch) {
            mdialog = AuthDialog()
            mdialog.show(fragmentManager, "dialogShow")
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        ifPassMatch = mdialog.checkMatch()
    }

    //A lot of buttons

    /**
     * This either goes to the login page or clears everything, depending on
     * the value of anythingEntered
     */
    fun loginOrClearButton(v: View) {
        if (anythingEntered) {
            //Reset text fields
            mUsername.text = null
            mName.text = null
            mPassword.text = null
            //Reset image
            mPic.setImageResource(R.drawable.cat_cut)
            //Reset booleans
            enterAnything(false)
            ifPassMatch = false

            //Hide Keyboard
            val view = this.currentFocus
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromInputMethod(view?.windowToken, 0)
        } else {
            //TODO Open the view to login
        }
    }

    /**
     * Called when the picture is pressed
     */
    fun pictButton(v: View) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //start the camera intent
        enterAnything(true) //set anythingEntered

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, unCroppedSave()) //tell it where to save
        //(see unCroppedSave())
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, IMAGE_REQUEST_CODE)
        }
    }

    /**
     * If every field has been entered and password has been matched, this will save everything
     * Else, it will notify the user of the error
     */
    fun submitButton(v: View) {

        //Pop up Confirm password when Password doesn't match and submitButton checked
        if(!ifPassMatch){
            mPassword.clearFocus()
            v.requestFocus()
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_LONG).show()
        }

        // fields to check in order to enable submit button
        // check whether every field is field and password matches
        if (mUsername.text.isEmpty()) {
            Toast.makeText(this, "Please enter an Username", Toast.LENGTH_LONG).show()
        } else if (mName.text.isEmpty()) {
            Toast.makeText(this, "Please enter a Name", Toast.LENGTH_LONG).show()
        } else if (mPassword.text.isEmpty()) {
            Toast.makeText(this, "Please enter a Password", Toast.LENGTH_LONG).show()
        } else { //Only after everything has been checked
            // initiate sharedPreferences
            val sp = getSharedPreferences(SHARED_PREF, 0)
            val editor = sp.edit()
            // store fields
            editor.putString("Username", mUsername.text.toString())
            editor.putString("Name", mName.text.toString())
            editor.putString("Password", mPassword.text.toString())

            // store booleans
            editor.putBoolean("ifPassMatch", ifPassMatch)
            editor.putBoolean("anythingEntered", anythingEntered)

            editor.apply()

            // Images can't be put safely into sharedPrefs, so the userimage is saved internally
            val bitmap = (mPic.drawable as BitmapDrawable).bitmap //pull the bitmap
            var fos: FileOutputStream? = null
            try {
                fos = openFileOutput("user_image.png",Context.MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
            }
            catch (e: IOException) {e.printStackTrace()}
            finally {
                try {
                    fos?.close()
                }
                catch (e: IOException) {e.printStackTrace()}
            }

            // If all went well, thank the user and remind them of their username
            Toast.makeText(this, "Thanks for registering! \nYour Username is saved as "
                    + sp.getString("Username", ""), Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Helper function that returns the URI to pass to the camera app
     */
    private fun unCroppedSave(): Uri {
        val folder = File(filesDir, "images") //creates a folder in our app's internal storage
        if (!folder.exists()) folder.mkdir() //if it doesn't exist, creates the directory
        val file = File(folder, "uncropped.png")
        if (!file.exists()) file.createNewFile()  //creates the file if it doesn't exist
        return FileProvider.getUriForFile(applicationContext,
                BuildConfig.APPLICATION_ID + ".provider", file) //returns  the URI
    }
}