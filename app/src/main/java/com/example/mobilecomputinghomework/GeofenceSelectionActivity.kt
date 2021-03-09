package com.example.mobilecomputinghomework

import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GeofenceSelectionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var location_x: String
    private lateinit var location_y: String
    private lateinit var name: String
    private lateinit var key: String

    private lateinit var reminderMarker: Marker
    private lateinit var latLng: LatLng
    private lateinit var map: GoogleMap
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_selection)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        loadIntentValues()
        database = Firebase.database(getString(R.string.firebase_db_url))
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        findViewById<Button>(R.id.bthSaveLocation).setOnClickListener { saveLocation() }
    }

    private fun saveLocation() {
        database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList/" + key + "/location_x").setValue(latLng.latitude.toString())
        database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList/" + key + "/location_y").setValue(latLng.longitude.toString())
        finish()
    }

    private fun loadIntentValues() {
        key = intent.getStringExtra("key")!!
        name = intent.getStringExtra("name")!!
        location_x = intent.getStringExtra("location_x")!!
        location_y = intent.getStringExtra("location_y")!!
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
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        if (location_x != "" && location_y != "") {
            updateMarker(LatLng(location_x.toDouble(), location_y.toDouble()))
        }
        onLongClick(map)
    }

    private fun updateMarker(latLng: LatLng) {
        reminderMarker.remove()
        this.latLng = latLng
        reminderMarker = map.addMarker(MarkerOptions().position(latLng).title("reminderMarker"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }
    private fun onLongClick(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapLongClickListener {
            updateMarker(it)
        }
    }
    private fun getLoggedInUsername(): String? {
        return applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
    }
}