package com.cs65.gnf.lab4

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.picasso.Picasso
import com.varunmishra.catcameraoverlay.CameraViewActivity
import com.varunmishra.catcameraoverlay.Config
import com.varunmishra.catcameraoverlay.OnCatPetListener
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import android.app.NotificationChannel
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.Color
import android.support.v4.app.TaskStackBuilder

class MapActivity : AppCompatActivity(), OnMapReadyCallback, 
    GoogleMap.OnMarkerClickListener, LocationListener, OnCatPetListener {

    // Map variables
    private lateinit var mMap: GoogleMap
    private var currLoc: LatLng? = null
    private var mgr : LocationManager? = null
    private val LOC_REQUEST_CODE = 1
    private var RADIUS_OF_SHOWN_MARKERS: Float = 500f

    // Cat variables
    private var listOfCats: List<Cat>? = null
    private var visibleCats : HashMap<Int,Cat> = HashMap()
    private lateinit var selectedCatID: ListenableCatID
    private lateinit var selectedCat: Cat

    //For from shared preferences
    private val USER_PREFS = "profile_data" //Shared with other activities
    private val USER_STRING = "Username"
    private val PASS_STRING = "Password"
    private val MODE_STRING = "mode"
    private val TIME_STRING = "minTime"
    private val DIS_STRING = "dis"

    //View variables
    private lateinit var trackButton: Button

    //Notification variables
    private var notificationID = 1
    private var channelId : String = "" // set in createChannel, only used in API >= 26

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //get necessary views
        trackButton = findViewById(R.id.track_button)

        // get necessary permissions
        requestPermissions()

        //create channel for notification
        createChannel()


        //Set an onChangeListener for the CatID
        //Update panel accordingly
        selectedCatID = ListenableCatID(object: ListenableCatID.ChangeListener {
            override fun onChange() {
                //Redraw the map, also generating the map of visible cats
                drawThings()

                //Get the selected cat
                val selectedCat = visibleCats[selectedCatID.id]
                if (selectedCat == null) {
                    toast("You're too far away from any cats!")
                }
                else {
                    //enable or disable button depending on if cat has been petted
                    val ifPetted: Boolean = selectedCat.petted
                    val patButton: Button = findViewById(R.id.pat_button)
                    patButton.isEnabled = !ifPetted
                  // Update the panel with the following:
                  // cat pic, cat name, and cat distance to current location

                    //Update Cat pic
                    val url = selectedCat.picUrl
                    val mImg: ImageView = findViewById(R.id.panel_img)
                    Picasso.with(applicationContext).load(url).placeholder(R.drawable.pointer).into(mImg)

                    //Update Cat name
                    val mName: TextView = findViewById(R.id.map_panel_name)
                    mName.text = selectedCat.name

                    //Update Distance
                    if (currLoc!= null){
                        //get distance between cat and user
                        val dist = FloatArray(1)
                        Location.distanceBetween(
                                selectedCat.lat,selectedCat.lng,
                                currLoc!!.latitude,currLoc!!.longitude,
                                dist
                        )
                        // cast distance to string
                        val readableDist = dist[0].toInt().toString() + " metres"

                        // update view
                        val mDis: TextView = findViewById(R.id.map_panel_distance)
                        mDis.text = readableDist
                }






                    //update the map
                    drawThings()
                }
            }
        })
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near the Dartmouth Green.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        // set map when ready
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        mMap.setOnMarkerClickListener(this) //Defined after onMapReady function

        getLocation()

        //Step 1— Get the username, password and mode
        val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        val user = prefs.getString(USER_STRING,null)
        val pass = prefs.getString(PASS_STRING,null)
        //mode as string is "hard" if mode is true, "easy" otherwise
        val mode = if (prefs.getBoolean(MODE_STRING,false)) "hard" else "easy"

        //Set the radius
        RADIUS_OF_SHOWN_MARKERS = when (prefs.getString(DIS_STRING, "m")) {
            "l" -> 1000f
            "m" -> 500f
            "s" -> 250f
            else -> 500f
        }

        //Step 2— Make the URL
        val listUrl = "http://cs65.cs.dartmouth.edu/catlist.pl?name=$user&password=$pass&mode=$mode"

        //Step 3— Open a Volley request queue and pass a string request
        Volley.newRequestQueue(this)
                .add(
                        StringRequest(Request.Method.GET,listUrl,
                                Response.Listener<String> { response ->
                                    val moshi = Moshi.Builder() //Build the Moshi, adding all needed adapters
                                            .add(KotlinJsonAdapterFactory())
                                            .add(StringToDoubleAdapter())
                                            .add(StringToIntAdapter())
                                            .add(StringToBoolAdapter())
                                            .build()

                                    //Try to change to a JSON object
                                    val errorObject: JSONObject? = try {
                                        JSONObject(response) //If it can be done, set  that to error object
                                    }
                                    catch (e: JSONException) { //If it can't be changed to an object it may be a list
                                        null //set the "error object" to null
                                    }

                                    if (errorObject!=null) { //if there was an error object made
                                        Log.d("SERVOR ERROR",errorObject.getString("error"))
                                    }
                                    else { //if no error object was found we can set our cat list
                                        val type = Types.newParameterizedType(List::class.java,Cat::class.java)

                                        val catAdaptor: JsonAdapter<List<Cat>> = moshi.adapter(type)

                                        //Step 4— set the list of cats
                                        listOfCats = catAdaptor.fromJson(response)

                                        if (listOfCats==null) { //if the list cannot be made
                                            Log.d("ERROR","List of cats not found")
                                        }
                                        else {
                                            //Step 5— Set the closest cat to SelectedCat to begin with
                                            selectedCatID.id = getClosestCat(listOfCats!!,currLoc!!)

                                            val patButton: Button = findViewById(R.id.pat_button)
                                            patButton.visibility = (View.VISIBLE)
                                            trackButton.visibility = (View.VISIBLE)
                                            trackButton.setBackgroundColor(getResources().getColor(R.color.LLGreen))


                                            //Step 6— Draw all markers
                                            drawThings()

                                        }
                                    }
                                },
                                Response.ErrorListener { error -> // Handle error cases
                                    when (error) {
                                        is NoConnectionError ->
                                            toast("Connection Error")
                                        is TimeoutError ->
                                            toast("Timeout Error")
                                        is AuthFailureError ->
                                            toast("AuthFail Error")
                                        is NetworkError ->
                                            toast("Network Error")
                                        is ParseError ->
                                            toast("Parse Error")
                                        is ServerError ->
                                            toast("Server Error")
                                        else -> toast("Error: " + error)
                                    }
                                }
                        ))
    }

    /**
     * Called when a marker  is clicked
     * Sets the selectedCatID to that marker's tag
     */
    override fun onMarkerClick(p0: Marker?): Boolean {
        val markerId = p0?.tag //Get the associated Cat ID we saved in the marker
        if (markerId is Int) selectedCatID.id = markerId //set selected cat to that
                                                         //this will call the listener function

        // change panel track button back
        trackButton.setText("TRACK")
        return true //Suppresses default behaviour of clicking on the marker
    }

    /**
     * when user location is changed,
     * create a new point of latitude and longtitude for this location
     * update it on map
     */
    override fun onLocationChanged(location : Location){
        Log.d("LOCATION", "CHANGED: " + location.latitude + " " + location.longitude)

        //Change current location
        currLoc = LatLng(location.latitude,location.longitude)

        //Repopulate the map
        drawThings()

        //Get the selected cat from the visible cat list
        val selectedCat = visibleCats[selectedCatID.id]

        //if it's null that means the selected cat is now invisible
        //So now set the closest cat to selected cat
        if (selectedCat== null) {
            selectedCatID.id = getClosestCat(listOfCats!!,currLoc!!)
        }

        //Otherwise update the distance between the cat and the user
        else if (currLoc!= null) {
            //get distance between cat and user
            val dist = FloatArray(1)
            Location.distanceBetween(
                    selectedCat.lat, selectedCat.lng,
                    currLoc!!.latitude, currLoc!!.longitude,
                    dist
            )

            val readableDist = dist[0].toInt().toString() + " metres"

            // update view
            val mDis: TextView = findViewById(R.id.map_panel_distance)
            mDis.text = readableDist
        }
    }

    /**
     * OnClick Pat button
     * Will send request to server and pat the cat
     */
    fun onPat(v: View) {
        //get Username / password
        val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        val user = prefs.getString(USER_STRING,null)
        val pass = prefs.getString(PASS_STRING,null)

        // start foreground camera activity
        val selectedCat = visibleCats[selectedCatID.id]
        if (selectedCat == null) {
            //TODO: what happens if selectedCat is null? Shall we start the camera anyway?
            // If so, how should we get the params for config? would it be a default cat?
            toast("You're too far away from any cats!")
        }
        else {
            Config.catName = selectedCat.name
            Config.catLatitude = selectedCat.lat
            Config.catLongitude = selectedCat.lng
            //TODO: not sure if this is the distance range he meant ,
            // TODO: also do we need to change cat picture? it's not in the example but i would suppose so, and it's a bitmap
            Config.locDistanceRange = RADIUS_OF_SHOWN_MARKERS.toDouble()
            Config.useLocationFilter = true // use this only for testing. This should be true in the final app.
            Config.onCatPetListener = this
            val i = Intent(this, CameraViewActivity::class.java)
            startActivity(i)

            //get the pet result
            //TODO: should we move this to his onCatPet? not sure what that is doing actually
            petCat(this, user, pass, selectedCatID.id, currLoc)
        }
    }

    /*
     * onClick Track button
     * will open camera activity
     */
    fun onTrack(v: View) {

        // change button text to stop
        trackButton.setText("STOP")
        trackButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))

        // start foreground notification service
        displayNotification()

    }

    /*
     * required for camera interface
     */
    override fun onCatPet(catName: String) {
        Toast.makeText(this, "You just Pet - " + catName, Toast.LENGTH_LONG).show()
    }

    /*
     * create channel for notifications
     */
    private fun createChannel() {

        if (android.os.Build.VERSION.SDK_INT >= 26) {

            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // The id of the channel.
            channelId = "my_channel_01"

            // The user-visible name of the channel.
            val name = getString(R.string.channel_name)
            // The user-visible description of the channel.
            val description = getString(R.string.channel_description)

            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(channelId, name, importance)

            // Configure the notification channel.
            mChannel.description = description

            mChannel.enableLights(true)

            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.lightColor = Color.RED

            // Sets vibration if the device supports it
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    /*
     * Intent to launch another activity if the user clicks "track"
     */
    private fun displayNotification()
    {

        //TODO: stop on intent?
        val i : Intent = Intent(this, NotificationActivity::class.java )

        Log.d("INTENT", i.toString())

        i.putExtra("notificationID", notificationID)
        i.putExtra("action", "stop")

        val stackBuilder : TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(i)

        //To be wrapped in a PendingIntent, because
        //it will be sent from whatever activity manages notifications;
        //this activity may not even be running.
        val pendingIntent : PendingIntent =
                PendingIntent.getActivity(this,
                        0, // on some SDK versions, the code should NOT be 0, or else it won't work
                        i,
                        PendingIntent.FLAG_UPDATE_CURRENT) // flags are important, see https://developer.android.com/reference/android/app/PendingIntent.html

        //The Notification.Builder provides an builder interface to create an Notification object.
        //Use a PendingIntent to specify the action which should be performed once the user select the notification.
        val nm : NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // build notification
        // Using the approved constructor for 26, which mandates that Channels be used;
        //   the older channel-less constructor is deprecated.
        val notiBuilder = if ( android.os.Build.VERSION.SDK_INT >= 26 )
            Notification.Builder(this, channelId) else  Notification.Builder(this)

        // TODO: get actual cat info: name + dis(maybe make global?
        val title= "Catching "
        val dis = "miles away"
        notiBuilder.setSmallIcon(R.drawable.green_marker)
                .setContentTitle(title)
                .setContentText(dis)
                .addAction(R.mipmap.ic_launcher, "STOP", pendingIntent)
                .setContentIntent(pendingIntent)
//               .setAutoCancel(true)  --if not set, the notification needs to be
        //   cancelled explicitly, or else it sticks in the notification bar.
        //   See cancellation logic in NotificationActivity.

        val n = notiBuilder.build() // API >= 16, otherwise use NotificationCompat

        nm.notify(notificationID, n)
    }

//    private fun stopTracking() {
//        var nm : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
//                as NotificationManager
//
//        //---cancel the notification that we started---
//        val notiId = intent.extras.getInt("notificationID")
//        nm.cancel(notiId)
//    }


    /**
     * Called whenever location is changed or selected cat is changed. Redraws markers according to
     * the display range and which cat is currently selected
     *
     * Only call once current location definitely exists and list of cats has definitely been made
     */
    private fun drawThings() {
        visibleCats.clear() //clear current map of visible cats
        mMap.clear() //clear the map view of all markers

        //Redraw user marker
        mMap.addMarker(MarkerOptions()
                .position(LatLng(currLoc!!.latitude,currLoc!!.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_user_marker)))

        for (kitty in listOfCats!!) {
            //get distance between cat and user
            val dist = FloatArray(1)
            Location.distanceBetween(
                    kitty.lat, kitty.lng,
                    currLoc!!.latitude,currLoc!!.longitude,
                    dist
            )

            if (dist[0]<RADIUS_OF_SHOWN_MARKERS) { //if in range
                visibleCats.put(kitty.catId,kitty) //put it in visible map
                if (kitty.catId==selectedCatID.id) { //if this is the selected cat
                    mMap.addMarker(MarkerOptions()
                            .position(LatLng(kitty.lat,kitty.lng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_ready_cat)))
                            .tag = kitty.catId
                }
                else { //if it was any other cat
                    mMap.addMarker(MarkerOptions()
                            .position(LatLng(kitty.lat,kitty.lng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.rsz_gray_cat)))
                            .tag = kitty.catId
                }
            }
        }
    }

    /**
     * Check and request location permits:
     * Fine, coarse, internet
     * Once location request granted, request map manager and get map asynchronously
     */
    private fun requestPermissions() {
        // Here, thisActivity is the current activity

        //TODO: check vibrate permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET),
                    LOC_REQUEST_CODE)
        }
        else {
            // get map manager
            val mapFrag = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFrag.getMapAsync(this)
        }
    }

    /**
     * When permissions are not granted ask for them again
     * Once they are, start up the map
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOC_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    // permissions not obtained
                    requestPermissions()
                } else {
                    val mapFrag = supportFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                    mapFrag.getMapAsync(this)
                }
            }
        }
    }

    /**
     * Application criteria for selecting a location provider. See line 158 "getBestProvider"
     * https://developer.android.com/reference/android/location/Criteria.html
     */
    private fun getCriteria(): Criteria {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.isAltitudeRequired = true
        criteria.isBearingRequired = false
        criteria.isSpeedRequired = true
        criteria.isCostAllowed = true
        return criteria
    }

    /**
     * This is called after location permissions is granted
     * Make sure you declare the corresponding permission in your manifest.
     */
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val criteria = getCriteria()
            val provider: String
            mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
            if (mgr != null) {
                provider = mgr!!.getBestProvider(criteria, true)
                // If curr location not found, Add a marker in Hanover(default)
                // and move the camera to curr loc
                val x = 43.70805181058869
                val y = -72.28422369807957
                val hanover = LatLng( x, y )
                // get last known location
                var l : Location? = null // remains null if Location is disabled in the phone
                try {
                    l = mgr!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                catch( e: SecurityException ){
                    Log.d("PERM", "Security Exception getting last known location. Using Hanover.")
                }
                currLoc = if (l != null)  LatLng(l.latitude, l.longitude) else hanover

                // Move camera: zoom in and out
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc))
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17f))

                //Find out how long the user wants between location updates
                val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
                val time: Long = when (prefs.getString(TIME_STRING,"f")) {
                    "f" -> 0
                    "m" -> 100
                    "s" -> 1000
                    else -> 0
                }
                //Ask for location updates
                mgr!!.requestLocationUpdates(provider, time, 0f, this)
            }
        }
    }

    // some interface functions that we don't actually use

    override fun onProviderDisabled(s: String) {
        // required for  interface, not used
    }

    override fun onProviderEnabled(s: String) {
        // required for interface, not used
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // required for  interface, not used
    }
}
