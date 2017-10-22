package com.cs65.gnf.lab3

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
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
}
