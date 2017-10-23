package com.cs65.gnf.lab3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.toast


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var permCheck : Boolean = false
//    private lateinit var mgr : LocationManager
    private lateinit var loc : LatLng
    private val LOC_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        permCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED

        if( ! permCheck ){
            Toast.makeText(this, "GPS permission FAILED", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOC_REQUEST_CODE)
        }
        else{
            Toast.makeText(this, "GPS permission OK", Toast.LENGTH_LONG).show()

            // TODO: make mgr working
//            mgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//            // why error below?
//            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, /* milliseconds */
//                    5f /* meters */ , this);
        }

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
        mMap = googleMap

        // Add a marker in Hanover and move the camera
        // Right now it's just a default
        // TODO: get latitude & long of cats and add markers
        val x : Double = 43.70805181058869
        val y : Double = -72.28422369807957

        val hanover = LatLng( x, y )
//        var l : Location? = null // remains null if Location is disabled in the phone
//        try {
//            l = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//        }
//        catch( e: SecurityException ){
//            Log.d("PERM", "Security Exception getting last known location. Using Hanover.")
//        }

//        loc = if (l != null)  LatLng(l.latitude, l.longitude) else hanover
        Log.d("Coords", x.toString() + " " + y.toString() )

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(MarkerOptions().position(hanover).title("Marker in Hanover"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hanover))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f))

        mMap.setOnMapClickListener { p0: LatLng? ->
            Log.d( "Map", p0.toString())
            if( p0 != null ) {
                mMap.addMarker(MarkerOptions().position(p0).title(p0.toString()))
            }
        }
    }

//    override fun onLocationChanged(location : Location){
//        Log.d("LOCATION", "CHANGED: " + location.latitude + " " + location.longitude)
//        Toast.makeText(this, "LOC: " + location.latitude + " " + location.longitude,
//                Toast.LENGTH_LONG).show()
//
//        val newPoint = LatLng( location.latitude, location.longitude )
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(newPoint))
//        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f))
//
//    }
//
//    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
//        // Called when the provider status changes.
//    }


    /*
     * OnClick Pat button
     * Will send request to server and pat the cat
     */
    fun onPat(v: View) {
        //enable/disable button
        toast("You pat it!")

    }
}
