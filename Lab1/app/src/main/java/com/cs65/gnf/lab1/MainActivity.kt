package com.cs65.gnf.lab1

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
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.*


class MainActivity : AppCompatActivity(), AuthDialog.DialogListener {

    private var anythingEntered = false
    private var ifPassMatch: Boolean = false
    private var SHARED_PREF = "my_sharedpref"
    private lateinit var mdialog: AuthDialog
    private val IMAGE_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2
    private val WRITE_REQUEST_CODE = 3
    private val READ_REQUEST_CODE = 4
    private lateinit var mUsername: EditText
    private lateinit var mName: EditText
    private lateinit var mPassword: EditText
    private lateinit var mPic: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set variables for layout items
        mPic = findViewById(R.id.pict_button)
        mPassword = findViewById(R.id.passwrd)
        mName = findViewById(R.id.full_name)
        mUsername = findViewById(R.id.username)

        //To see if picture has been saved internally
        val file = File(filesDir,"user_image.png")
        if (file.exists()) mPic.setImageBitmap(BitmapFactory.decodeFile(file.path))

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

        //If anything starts to be entered, the Clear button will change to the login button
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
                ifPassMatch = false
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        //Also once the password loses focus it'll trigger the dialog
        mPassword.setOnFocusChangeListener({ _, hasFocus: Boolean ->
            if (!hasFocus) {
                passwordConfirm()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putString("mUsername", mUsername.text.toString())
        outState?.putString("mName", mName.text.toString())
        outState?.putString("mPassword", mPassword.text.toString())
        outState?.putBoolean("anything", anythingEntered)

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

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mUsername.setText(savedInstanceState.getString("mUsername", null))
        mName.setText(savedInstanceState.getString("mName"), null)
        mPassword.setText(savedInstanceState.getString("mPassword"), null)
        anythingEntered = savedInstanceState.getBoolean("anything")

        //Open and set picture
        val file = File(filesDir, "temporary_picture.png")
        mPic.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))

        //Delete that file
        file.delete()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_REQUEST_CODE -> {

                    //Start 3rd party cropper
                    CropImage
                            .activity(unCroppedSave())
                            .setAspectRatio(1, 1)
                            .setFixAspectRatio(true)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .start(this)
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val uri = result.uri
                    mPic.setImageBitmap(BitmapFactory.decodeFile(uri.path))
                    File(filesDir, "uncropped.png").delete()
                }
            }
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
        topButton.text = if (entered) "Clear" else "Login"
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
    fun loginOrClearButton(v: View) {
        if (anythingEntered) {

            mUsername.text = null
            mName.text = null
            mPassword.text = null
            mPic.setImageResource(R.drawable.cat_cut)
            enterAnything(false)

            //Hide Keyboard
            val view = currentFocus
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromInputMethod(view.windowToken, 0)
        } else {
            //TODO Open the view to login
        }
    }

    fun pictButton(v: View) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        enterAnything(true)

        Log.d("URIPath", unCroppedSave().path)

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, unCroppedSave())

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, IMAGE_REQUEST_CODE)
        }
    }

    /*
     * Click Submit triggers two cases:
     * 1. it will save text values in sharedPreference:
     * username, name, password, only if every field entered and password matches
     * 2. otherwise, it will notify the user about the error
     */
    fun submitButton(v: View) {

        //Pop up Confirm password when Password doesn't match and submitButton checked
        if(!ifPassMatch){
            mPassword.clearFocus()
            v.requestFocus()
            Toast.makeText(this, "Please confirm password", Toast.LENGTH_LONG).show()
        }

        // fields to check in order to enable submit button
        val mUsernameS = mUsername.text.toString()
        val mNameS = mName.text.toString()
        val mPasswordS = mPassword.text.toString()

        // check whether every field is field and password matches
        if (mUsernameS.isEmpty()) {
            Toast.makeText(this, "Please enter an Username", Toast.LENGTH_LONG).show()
        } else if (mNameS.isEmpty()) {
            Toast.makeText(this, "Please enter a Name", Toast.LENGTH_LONG).show()
        } else if (mPasswordS.isEmpty()) {
            Toast.makeText(this, "Please enter a Password", Toast.LENGTH_LONG).show()
        } else if (!ifPassMatch) {
            Toast.makeText(this, "Password does not match", Toast.LENGTH_LONG).show()
        } else {
            // initiate sharedpreference instance
            val sp = getSharedPreferences(SHARED_PREF, 0)
            val editor = sp.edit()
            // store fields
            editor.putString("Username", mUsername.text.toString())
            editor.putString("Name", mName.text.toString())
            editor.putString("Password", mPassword.text.toString())

            editor.apply()


            // saving image to a file
            val bitmap = (mPic.drawable as BitmapDrawable).bitmap
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

            // check if successully saved by making a toast
            Toast.makeText(this, "Thanks for registering! \nYour Username is saved as "
                    + sp.getString("Username", ""), Toast.LENGTH_LONG).show()
        }

    }

    private fun unCroppedSave(): Uri {
        val folder = File(filesDir, "images")
        if (!folder.exists()) folder.mkdir()
        val file = File(folder, "uncropped.png")
        if (!file.exists()) file.createNewFile()
        return FileProvider.getUriForFile(applicationContext,
                BuildConfig.APPLICATION_ID + ".provider", file)
    }
}