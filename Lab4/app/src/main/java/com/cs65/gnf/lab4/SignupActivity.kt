package com.cs65.gnf.lab4

import android.Manifest
import android.app.DialogFragment
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.*
import com.google.gson.GsonBuilder
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SignupActivity : AppCompatActivity(), AuthDialog.DialogListener {

    //Global bools
    private var anythingEntered = false //(used to help set login button to clear)
    private var ifPassMatch: Boolean = false // (if password has been reentered and matched)
    private var ifNameAvailable: Boolean = false // if the username is available

    private var USER_PREFS = "profile_data" //shared Preference String

    //Request codes
    private val IMAGE_REQUEST_CODE = 1 //To send intent to Android's camera app
    private val CAMERA_REQUEST_CODE = 2 //To request use of camera
    private val WRITE_REQUEST_CODE = 3 //To request use of writing files
    private val READ_REQUEST_CODE = 4 //To request use of reading files

    //For safely saving
    private val USER_STRING = "Username"
    private val NAME_STRING = "Name"
    private val PASS_STRING = "Password"
    private val MATCH_STRING = "match"
    private val ENTER_STRING = "anything"
    private val NAME_AVAIL_STRING = "ifNameAvailable"
    private val IMAGE_STRING = "image"

    //Server URLs
    private val REQ_URL = "http://cs65.cs.dartmouth.edu/nametest.pl?name="
    private val SAVE_URL = "http://cs65.cs.dartmouth.edu/profile.pl"

    //Global views and other vars that require frequent access
    private lateinit var mDialog: AuthDialog //Dialog declared globally so it can be accessed later
    private lateinit var mUsername: EditText //Username field
    private lateinit var mName: EditText //Full Name field
    private lateinit var mPassword: EditText //Password field
    private lateinit var mImg: MLRoundedImageView // User picture
    private lateinit var mAvail: TextView // Name Availability
    private lateinit var queue: RequestQueue //Request
    private var imgUri: Uri? = null //Location of user's picture
    private lateinit var jsonReq: JSONObject
    private lateinit var sp: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        //init
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this)

        //Set variables for global items
        mImg = findViewById(R.id.pict_button)
        mPassword = findViewById(R.id.passwrd)
        mName = findViewById(R.id.full_name)
        mUsername = findViewById(R.id.username)
        mAvail = findViewById(R.id.availability)

        //get shared preferences
        sp = getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)

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
                //If password changes it no longer matches
                ifPassMatch = false
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        //Once the password loses focus it'll trigger the dialog
        mPassword.setOnFocusChangeListener{ _ , hasFocus ->
            if (!hasFocus) {
                passwordConfirm()
            }
        }

        //Once userName loses focus, check availability
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
        outState?.putString(USER_STRING, mUsername.text.toString())
        outState?.putString(NAME_STRING, mName.text.toString())
        outState?.putString(PASS_STRING, mPassword.text.toString())
        outState?.putBoolean(ENTER_STRING, anythingEntered)
        outState?.putBoolean(MATCH_STRING,ifPassMatch)
        outState?.putBoolean(NAME_AVAIL_STRING,ifNameAvailable)
        outState?.putParcelable(IMAGE_STRING,imgUri)
    }

    /**
     * Gets back information after phone is flipped
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        //Set back text fields, booleans, and the image
        mUsername.setText(savedInstanceState.getString(USER_STRING, null))
        mName.setText(savedInstanceState.getString(NAME_STRING), null)
        mPassword.setText(savedInstanceState.getString(PASS_STRING), null)
        enterAnything(savedInstanceState.getBoolean(ENTER_STRING,false))
        ifPassMatch = savedInstanceState.getBoolean(MATCH_STRING,false)
        ifNameAvailable = savedInstanceState.getBoolean(NAME_AVAIL_STRING,false)
        val img: Uri? = savedInstanceState.getParcelable(IMAGE_STRING)
        if (img!=null) {
            imgUri= img
            doAsync { mImg.setImageURI(imgUri) } //set the new image
        }

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
                    imgUri = result.uri //This comes from usage of this 3rd party app, documented
                    //on their github wiki
                    doAsync { mImg.setImageURI(imgUri) } //set the new image
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
    private fun checkName() {

        val req = mUsername.text.toString()

        val url = REQ_URL + req

        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    try {
                        // parse the string, based on provided class object as template
                        val gson = GsonBuilder().create()
                        val nameo = gson.fromJson(response, UnameObject::class.java)
                        val av = nameo.avail
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

    /**
     * Helper method to check_name
     * Will update "Availability" textView in the UI
     *
     */
    private fun updateAvail(res: String?) {
        when (res) {
            "true" -> {
                mAvail.text = getString(R.string.signup_available)
                mAvail.setTextColor(getColor(R.color.Black))
                ifNameAvailable = true
            }
            "false" -> {
                mAvail.text = getString(R.string.signup_unavailable)
                mAvail.setTextColor(getColor(R.color.colorPrimaryDark))
                ifNameAvailable = false
            }
            is String -> toast(res)
        }
    }

    /**
     * A POST Request to the server, post profile information
     * Called when submit button is clicked and every fields is ok
     * Create a Json object with username, name, password
     *
     */
    private fun saveProfile(){

        //  fields to save
        val usernameToSave = mUsername.text.toString()
        val nameToSave = mName.text.toString()
        val passToSave = mPassword.text.toString()

        val url = SAVE_URL
        // put into json objects
        try {
            jsonReq = JSONObject()
            jsonReq.put("name", usernameToSave)
            jsonReq.put("realName", nameToSave)
            jsonReq.put("password", passToSave)

            //Save default preferences to the server
            jsonReq.put(getString(R.string.prefs_privacy_key), true)
            jsonReq.put(getString(R.string.prefs_alert_key), "r")

        } catch (e: JSONException) { // Handle error cases
            // Warn the user that something is wrong; do not connect
            Log.d("JSON", "Invalid JSON: " + e.toString())

            toast("Invalid JSON")

            return
        }


        // Request a Json response from the provided URL.
        val joRequest = object: JsonObjectRequest (url, // POST is presumed
                jsonReq,
                Response.Listener<JSONObject> { response ->
                    try {
                        // parse the string, based on provided class object as template
                        val gson = GsonBuilder().create()
                        val profo = gson.fromJson(response.toString(), ProfileObject::class.java)

                        // get two fields from profile object
                        val status = profo.status
                        val profJson = profo.json
                        val name = profJson?.name

//                        // alternative method to getname: create a sub-jsonobject to access the inner member of data field
//                        val data = profo.getData()
//                        val dataJsonObj = JSONObject(data)
//                        val name = dataJsonObj.getString("name")

                        Log.d("JSON", response.toString())
                        checkSubmit(status, name)
                    } catch (e: Exception) {
                        Log.d("JSON", e.toString())
                    }
                }, Response.ErrorListener { error -> // Handle error cases
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
          // This to set custom headers:
          //   https://stackoverflow.com/questions/17049473/how-to-set-custom-header-in-volley-request
        @Throws(AuthFailureError::class)
             override  fun getHeaders(): Map<String, String> {
                run {
                    val params = HashMap<String, String>()
                    // params.put("Accept", "application/json");
                    params.put("Accept-Encoding", "identity")
                    params.put("Content-Type", "application/json")

                    return params
                }
            }
        }

        queue.add(joRequest)
    }

    /*
     * Helper method to save_profile
     * Will check status of POST response,
     * and catch additional error not caught by volley exception
     */
    fun checkSubmit(status: String?, name: String?){
        if (status == "OK") {
            Toast.makeText(this, "Welcome "+ name.toString(), Toast.LENGTH_LONG).show()
        } else if (status == "ERROR"){
            toast("Sorry! Error occured when saving your profile.")
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
    }

    /**
     * Helper function to call the password confirmation dialog
     */
    private fun passwordConfirm() {
        if(!ifPassMatch) {
            doAsync {
                mDialog = AuthDialog()
                mDialog.show(fragmentManager, "dialogShow")
            }
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        ifPassMatch = mDialog.ifMatch
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
            doAsync {  mImg.setImageResource(R.drawable.cat_cut) }
            //Reset booleans
            enterAnything(false)
            ifPassMatch = false
        } else {
            val i = Intent(this,LoginActivity::class.java)
            startActivity(i)
        }
    }

    /**
     * Called when the picture is pressed
     */
    fun pictButton(v: View) {
        v.requestFocus()

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
            mUsername.text.isEmpty() -> {
                toast("Please enter a Username")
                highlight(mUsername)
            }
            mName.text.isEmpty() -> {
                toast("Please enter a name")
                highlight(mName)
            }
            mPassword.text.isEmpty() -> {
                toast("Please enter a password")
                highlight(mPassword)
            }
            !ifNameAvailable -> {
                Toast.makeText(applicationContext, "Username must be available", Toast.LENGTH_LONG).show()
            }
            !ifPassMatch -> { //if password is not matched
                mPassword.requestFocus()
                mPassword.clearFocus() //call the pass match dialog
                v.requestFocus()
                toast("Please confirm password")
            }
            else -> { //once everything is checked

                //Store fields and also default values for the server
                sp.edit()
                        .putString(USER_STRING, mUsername.text.toString())
                        .putString(NAME_STRING, mName.text.toString())
                        .putString(PASS_STRING, mPassword.text.toString())
                        .putBoolean(MATCH_STRING, ifPassMatch)
                        .putBoolean(ENTER_STRING, anythingEntered)
                        .putString(IMAGE_STRING,imgUri.toString())
                        .apply()

                //2. Save profile to server
                saveProfile()
                
                //Open main activity
                val i = Intent(applicationContext,MainActivity::class.java)
                startActivity(i)
            }
        }
    }

    //Helper methods

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
     * Highlights a view if something was forgotten there
     */
    private fun highlight(v: EditText) {
        //Create a blinking animation
        val blink = AlphaAnimation(1f,0f)
        blink.interpolator = DecelerateInterpolator()
        blink.duration = 1000
        blink.repeatCount = 1

        //Assign that to the view
        v.setBackgroundResource(R.color.colorPrimaryDark)
        v.startAnimation(blink)
//        Timer().schedule(object : TimerTask() {
//            override fun run() {
//                v.setBackgroundResource(R.color.transparent)
//            }
//        }, 2000)
        doAsync {
            Thread.sleep(2000)
            uiThread {
                v.setBackgroundResource(R.color.transparent)
            }
        }
    }
}