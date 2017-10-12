package com.cs65.gnf.lab2

import android.Manifest
import android.app.Activity
import android.app.DialogFragment
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.content.Intent
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.android.flexbox.FlexboxLayout
import android.widget.*
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


class SignupActivity : AppCompatActivity(), AuthDialog.DialogListener {

    private var anythingEntered = false //(used to help set login button to clear)
    private var ifPassMatch: Boolean = false // (if password has been reentered and matched)
    private var ifNameAvailable: Boolean = false // if the username is available
    private var SHARED_PREF = "profile_data"
    private lateinit var mdialog: AuthDialog //Dialog declared globally so it can be accessed later
    private val IMAGE_REQUEST_CODE = 1 //To send intent to Android's camera app
    private val CAMERA_REQUEST_CODE = 2 //To request use of camera
    private val WRITE_REQUEST_CODE = 3 //To request use of writing files
    private val READ_REQUEST_CODE = 4 //To request use of reading files
    private lateinit var mUsername: EditText //Username field
    private lateinit var mName: EditText //Full Name field
    private lateinit var mPassword: EditText //Password field
    private lateinit var mPic: ImageView // User picture
    private lateinit var mAvail: TextView // Name Availability
    //private lateinit var dl: Handler
    //private lateinit var ctx: Activity
    private lateinit var queue: RequestQueue
    private lateinit var jsonReq: JSONObject


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)

        //dl = Handler()
        //ctx = this
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this)

        //Set variables for layout items
        mPic = findViewById(R.id.pict_button)
        mPassword = findViewById(R.id.passwrd)
        mName = findViewById(R.id.full_name)
        mUsername = findViewById(R.id.username)
        mAvail = findViewById(R.id.availability)


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



        //Ask for permissions
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE)
        }

        //If anything starts to be entered, enterAnything() is called, changing 'Login' to 'Clear'
        //button (see function below)
        mUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                enterAnything(p0?.length != 0)
//                checkSubmit()
                //TODO check availability
//                checkName()
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
        mPassword.setOnFocusChangeListener{ v, hasFocus ->
            if (!hasFocus) {
                passwordConfirm()
            }
        }

        //Once userName is entered, check availability
        mUsername.setOnFocusChangeListener({ _, hasFocus: Boolean ->
            if (!hasFocus) {
                checkName()
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
        outState?.putBoolean("passMatch",ifNameAvailable)

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
        ifNameAvailable = savedInstanceState.getBoolean("ifNameAvailable",false)

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

    /*
     * A GET request, get name and its availability
     * This function checks the availability of Username
     * Create Gson object with response get from server and unameObject
     * will then call updateAvail() to update UI
     *
     * It is called when username field loses focus
     *
     */
    fun checkName() {

        val req = mUsername.getText().toString()

        val url = "http://cs65.cs.dartmouth.edu/nametest.pl?name=" + req;

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    try {
                        // parse the string, based on provided class object as template
                        val gson = GsonBuilder().create()
                        val nameo = gson.fromJson(response, unameObject::class.java)
                        val av = nameo.getNavail()
                        updateAvail(av)
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

    /*
     * Helper method to check_name
     * Will update "Availability" textView in the UI
     *
     */
    private fun updateAvail(res: String?) {
//        val avail: TextView = ctx.findViewById(R.id.availability)
        if (res == "true") {
            mAvail.setText("Available")
            ifNameAvailable = true
        } else if (res == "false") {
            mAvail.setText("Unavailable")
            ifNameAvailable = false
        } else {
            Toast.makeText(this, res, Toast.LENGTH_LONG).show()
        }
    }

    /*
     * A POST Request to the server, post profile information
     * Called when submit button is clicked and every fields is ok
     * Create a Json object with username, name, password
     *
     */
    private fun saveProfile(){

        //  fields to save
        val usernameToSave = mUsername.getText().toString()
        val nameToSave = mName.getText().toString()
        val passToSave = mPassword.getText().toString()

        val url = "http://cs65.cs.dartmouth.edu/profile.pl"

        // put into json objects
        try {
            jsonReq = JSONObject()
            jsonReq.put("name", usernameToSave)
            jsonReq.put("realName", nameToSave)
            jsonReq.put("password", passToSave)

        } catch (e: JSONException) {
            // Warn the user that something is wrong; do not connect
            Log.d("JSON", "Invalid JSON: " + e.toString())

            Toast.makeText(this, "Invalid JSON" + e.toString(), Toast.LENGTH_LONG).show()

            return
        }


        // Request a string response from the provided URL.

        val joRequest = object: JsonObjectRequest (url, // POST is presumed
                jsonReq,
                Response.Listener<JSONObject> { response ->
                    try {
                        // parse the string, based on provided class object as template
                        val gson = GsonBuilder().create()
                        val nameo = gson.fromJson(response.toString(), profileObject::class.java)
                        val status = nameo.getStatus()
                        val data = nameo.data
                        //val name = nameo.getUname()
                        val dataJsonObj = JSONObject(data)
                        val name = dataJsonObj.getString("name")


                        Log.d("JSON", response.toString() )
                        check_submit(status, name)
                    } catch (e: Exception) {
                        Log.d("JSON", e.toString())
                    }
                }, Response.ErrorListener { error ->
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
        })

        {


//        // This to set custom headers:
//        //   https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
        @Throws(AuthFailureError::class)
             override  fun getHeaders(): Map<String, String> {
                run {
                    val params = HashMap<String, String>()
                    // params.put("Accept", "application/json");
                    params.put("Accept-Encoding", "identity")
                    params.put("Content-Type", "application/json");

                    return params
                }
            }
        }

        queue.add(joRequest)
    }

    fun check_submit(status: String?, name: String?){
        if (status == "OK") {
            Toast.makeText(this, "Welcome "+ name.toString(), Toast.LENGTH_LONG).show()
        } else if (status == "ERROR"){
            Toast.makeText(this, "Sorry! Error occured when saving your profile." + status, Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Requests permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                //If we get Camera permission,
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Ask for permission to write to files
                    if (ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                WRITE_REQUEST_CODE)
                    }
                } else { //if permission wasn't  granted, request camera again
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                            CAMERA_REQUEST_CODE)
                }
                return
            }
            WRITE_REQUEST_CODE -> {
                //If we get write permission,
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Ask for permission to read files
                    if (ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                READ_REQUEST_CODE)
                    }
                } else {//if permission wasn't  granted, request it again
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            WRITE_REQUEST_CODE)
                }
                return
            }
            READ_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {//if permission wasn't  granted, request it again
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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
        if (ifNameAvailable == true) {
            mAvail.setText("Available")
        } else if (ifNameAvailable == false) {
            mAvail.setText("Unavailable")
        }
    }

    /**
     * Helper function to call the password confirmation dialog
     */
    private fun passwordConfirm() {
        if(!ifPassMatch) {
            mdialog = AuthDialog()
            Log.d("CYCLE","here")
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

            //TODO Hide Keyboard
        } else {
            val i = Intent(this,LoginActivity::class.java)
            startActivity(i)
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

        // check whether every field is filled and password matches
        when {
            !ifPassMatch -> { //if password is not matched
                mPassword.clearFocus() //call the pass match dialog
                v.requestFocus()
                Toast.makeText(applicationContext, "Please confirm password", Toast.LENGTH_LONG).show()
            }
            !ifNameAvailable -> {
                Toast.makeText(applicationContext, "Username must be available", Toast.LENGTH_LONG).show()
            }
            mUsername.text.isEmpty() ->
                Toast.makeText(this, "Please enter an Username",Toast.LENGTH_LONG).show()
            mName.text.isEmpty() ->
                Toast.makeText(this, "Please enter a Name", Toast.LENGTH_LONG).show()
            mPassword.text.isEmpty() ->
                Toast.makeText(this, "Please enter a Password", Toast.LENGTH_LONG).show()

            else -> { //once everything is checked
                // 1. Save locally
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

                //2. Save profile to server
                saveProfile()
                
                //Open main activity
                val i = Intent(applicationContext,MainActivity::class.java)
                startActivity(i)
            }
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

    /**
     * Helper method that hides the keyboard
     */
    fun hideKeyboard(v:View){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
    }
}