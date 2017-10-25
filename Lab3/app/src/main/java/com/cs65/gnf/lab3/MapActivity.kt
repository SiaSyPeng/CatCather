package com.cs65.gnf.lab3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationListener
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject

class MapActivity : AppCompatActivity(), OnMapReadyCallback, 
    GoogleMap.OnMarkerClickListener, LocationListener {

    private lateinit var mMap: GoogleMap
    private var permCheck : Boolean = false
    private var mapOfCats : HashMap<Int,Cat> = HashMap()
    private lateinit var selectedCatID: ListenableCatID
    private lateinit var mgr : LocationManager
    private lateinit var loc : LatLng
    private val LOC_REQUEST_CODE = 1

    //For from shared preferences
    private val USER_PREFS = "profile_data" //Shared with other activities
    private val USER_STRING = "Username"
    private val PASS_STRING = "Password"
    private val MODE_STRING = "mode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        //check and request location permits
        permCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED

        if( ! permCheck ){
            Toast.makeText(this, "GPS permission FAILED", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOC_REQUEST_CODE)
        }
        else{
            Toast.makeText(this, "GPS permission OK", Toast.LENGTH_LONG).show()

            // Get location updates
            mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, /* milliseconds */
                    5f /* meters */ , this)
        }

        //Set an onChangeListener for the CatID
        selectedCatID = ListenableCatID(object: ListenableCatID.ChangeListener {
            override fun onChange() {
                val selectedCat = mapOfCats[selectedCatID.id] //get the selected cat

                //Get Image URI
                val uri = Uri.parse(selectedCat?.picUrl)

                //TODO change this to the actual view that should be displaying the picture
                val mImg = ImageView(applicationContext).setImageURI(uri)

                //TODO change this to the actual view that should be displaying the name
                val mText = TextView(applicationContext).setText(selectedCat?.name)
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
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        mMap.setOnMarkerClickListener(this) //Defined after onMapReady function

        //WE NEED TO GET THE LIST OF CATS AND MAKE MARKERS ON THE SCREEN

        //Step 1— Get the username, password and mode
        val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        val user = prefs.getString(USER_STRING,null)
        val pass = prefs.getString(PASS_STRING,null)
        //mode as string is "hard" if mode is true, "easy" otherwise
        val mode = if (prefs.getBoolean(MODE_STRING,false)) "hard" else "easy"

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
                                        val listOfCats = catAdaptor.fromJson(response)

                                        if (listOfCats==null) { //if the list cannot be made
                                            Log.d("ERROR","List of cats not found")
                                        }
                                        else {
                                            //Step 5— add the markers
                                            for (kitty in listOfCats) { //for every cat
                                                val pos = LatLng(kitty.lat,kitty.lng) //get cat's position
                                                mMap.addMarker(MarkerOptions() //add marker
                                                        .position(pos) //at that position
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker)))
                                                        .tag = kitty.catId //set its tag to the catId

                                                //Put every cat into a map for easily accessing a specific cat
                                                mapOfCats.put(kitty.catId,kitty)
                                            }

                                            //Step 6— Set the closest cat to SelectedCat to begin with
                                            selectedCatID.id = getClosestCat(listOfCats)

                                            val patButton: View = findViewById(R.id.pat_button)
                                            patButton.visibility = (View.VISIBLE)

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



        // Add a marker in Hanover and move the camera
        val x = 43.70805181058869
        val y = -72.28422369807957
        val hanover = LatLng( x, y )

        // get last known location
        var l : Location? = null // remains null if Location is disabled in the phone
        try {
            l = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        catch( e: SecurityException ){
            Log.d("PERM", "Security Exception getting last known location. Using Hanover.")
        }
        loc = if (l != null)  LatLng(l.latitude, l.longitude) else hanover
        mMap.addMarker(MarkerOptions().position(loc).title("Curr loc").icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker)))

        Log.d("Coords", x.toString() + " " + y.toString() )

        //grey marker
        mMap.addMarker(MarkerOptions().position(hanover).title("Marker in Hanover").icon(BitmapDescriptorFactory.fromResource(R.drawable.grey_marker)))

        // Move camera: zoom in and out
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hanover))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f))
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        val markerId = p0?.tag //Get the associated Cat ID we saved in the marker
        if (markerId is Int) selectedCatID.id = markerId //set selected cat to that
                                                         //this will call the listener function

        return true //Suppresses default behaviour of clicking on the marker
    }

    /*
     * when user location is changed,
     * create a new point of latitude and longtitude for this location
     * update it on map
     * TODO: erase previous curr loc marker
     */
    override fun onLocationChanged(location : Location){
        Log.d("LOCATION", "CHANGED: " + location.latitude + " " + location.longitude)
        Toast.makeText(this, "LOC: " + location.latitude + " " + location.longitude,
                Toast.LENGTH_LONG).show()

        // new location latitude and longtitude
        // add red marker
        val ncurrLoc = LatLng( location.latitude, location.longitude )
        //val m: Marker;
        mMap.addMarker(MarkerOptions().position(ncurrLoc).title("Curr loc").icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker)))
        //m.setP
        // move camera around
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ncurrLoc))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f))

        val selectedCat = mapOfCats[selectedCatID.id]
        if (selectedCat!=null) {
            val lat = selectedCat.lat
            val lng = selectedCat.lng
            val latlng = LatLng(lat,lng)
        }

    }

    override fun onProviderDisabled(s: String) {
        // required for  interface, not used
    }

    override fun onProviderEnabled(s: String) {
        // required for interface, not used
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // Called when the provider status changes.
    }

    /*
     * OnClick Pat button
     * Will send request to server and pat the cat
     */
    fun onPat(v: View) {
        //get Username / password
        val prefs = getSharedPreferences(USER_PREFS,Context.MODE_PRIVATE)
        val user = prefs.getString(USER_STRING,null)
        val pass = prefs.getString(PASS_STRING,null)

        //get the pet result
        val petResult = petCat(this,user,pass,selectedCatID.id)

        when (petResult?.status) {
            Status.OK -> {
                toast("mrowwwww")
                val intent = Intent(applicationContext,SuccessActivity::class.java)
                startActivity(intent)
            }
            Status.ERROR -> {
                if (petResult.reason!=null) toast(petResult.reason) //Tell the user why the petting
                                                                    //was unsuccessful
            }
        }
    }
}
