package com.example.mobilecomputinghomework

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase

class SelectCurrentLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var reminderMarker: Marker
    private lateinit var latLng: LatLng
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_current_location)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        latLng = LatLng(65.01284753266825, 25.466887798384903)
        reminderMarker = map.addMarker(MarkerOptions().position(latLng).title("reminderMarker"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        map.setOnMapLongClickListener {
            setLocation(it)
        }
    }

    private fun setLocation(latLng: LatLng) {
        val username = getLoggedInUsername()
        applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).edit().putFloat("x$username", latLng.latitude.toFloat()).apply()
        applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).edit().putFloat("y$username", latLng.longitude.toFloat()).apply()
        finish()
    }
    private fun getLoggedInUsername(): String {
        return applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")!!
    }
}