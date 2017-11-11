package com.cs65.gnf.lab4

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.squareup.picasso.Picasso
import com.varunmishra.catcameraoverlay.CameraViewActivity
import com.varunmishra.catcameraoverlay.Config
import com.varunmishra.catcameraoverlay.OnCatPetListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import java.io.ObjectInputStream

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
    private val TIME_STRING = "minTime"
    private val DIS_STRING = "dis"
    private val READY_STRING = "ready"

    private val BROADCAST_ACTION = "com.cs65.gnf.lab4.ready"

    //For internal storage
    private val CAT_LIST_FILE = "cat_list"
    private var ready = false

    //View variables
    private lateinit var trackButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val broadcastReceiver =  MyRecvr()


        //Start listening for / getting the catList
        val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        if (prefs.getBoolean(READY_STRING,false)) { //if ready
            val fis = openFileInput(CAT_LIST_FILE)
            val ois = ObjectInputStream(fis)
            doAsync {
                listOfCats = ois.readObject() as ArrayList<Cat>
                ready = true
            }
        }
        else {
            val i = IntentFilter(BROADCAST_ACTION)
            LocalBroadcastManager.getInstance(applicationContext)
                    .registerReceiver(broadcastReceiver,i)
        }

        //Set the radius
        RADIUS_OF_SHOWN_MARKERS = when (prefs.getString(DIS_STRING, "m")) {
            "l" -> 1000f
            "m" -> 500f
            "s" -> 250f
            else -> 500f
        }

        //setup views
        trackButton = findViewById(R.id.track_button)

        requestPermissions()

        //Set an onChangeListener for the CatID
        //Update panel accordingly
        selectedCatID = ListenableCatID(object: ListenableCatID.ChangeListener {
            override fun onChange() {
                //Redraw the map, also generating the map of visible cats
                drawThings()

                //Get the selected cat
                val selectedCat = visibleCats[selectedCatID.id]
                if (selectedCat == null) {
                    longToast("You're too far away from any cats!")
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

        if (ready) { //if the list has been prepped by this point go on
            selectedCatID.id = getClosestCat(listOfCats!!,currLoc!!)

            val patButton: Button = findViewById(R.id.pat_button)
            patButton.visibility = (View.VISIBLE)
            trackButton.visibility = (View.VISIBLE)
            trackButton.setBackgroundColor(getColor(R.color.LLGreen))


            //Draw all markers
            drawThings()
        }
        else { //else wait in another thread for the list
            doAsync {
                while (!ready) {}
                selectedCatID.id = getClosestCat(listOfCats!!,currLoc!!)

                val patButton: Button = findViewById(R.id.pat_button)
                patButton.visibility = (View.VISIBLE)
                trackButton.visibility = (View.VISIBLE)
                trackButton.setBackgroundColor(getColor(R.color.LLGreen))


                //Draw all markers
                drawThings()
            }
        }
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
        trackButton.text = "TRACK"
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
            longToast("You're too far away from any cats!")
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
        trackButton.text = "STOP"
        trackButton.setBackgroundColor(getColor(R.color.colorPrimaryDark))

    }

    /*
     * required for camera interface
     */
    override fun onCatPet(catName: String) {
        Toast.makeText(this, "You just Pet - " + catName, Toast.LENGTH_LONG).show()
    }

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

    /**
     * inner class that is a broadcast receiver, so that when the broadcast is received we can getCats
     */
    inner class MyRecvr : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val fis = openFileInput(CAT_LIST_FILE)
            val ois = ObjectInputStream(fis)
            doAsync {
                listOfCats = ois.readObject() as ArrayList<Cat>
                ois.close()
                fis.close()
                ready = true
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