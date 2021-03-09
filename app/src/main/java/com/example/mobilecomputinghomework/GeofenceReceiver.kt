package com.example.mobilecomputinghomework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GeofenceReceiver : BroadcastReceiver() {
    lateinit var key: String
    lateinit var text: String
    lateinit var name: String
    lateinit var user: String
    private lateinit var database: FirebaseDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            database = Firebase.database("https://mobilecomputing-hw-default-rtdb.firebaseio.com/")
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            val geofencingTransition = geofencingEvent.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                // Retrieve data from intent
                if (intent != null) {
                    key = intent.getStringExtra("key")!!
                    text = intent.getStringExtra("message")!!
                    user = intent.getStringExtra("user")!!
                }
                ReminderList.showNofitication(context,text, name)
                ReminderList.removeGeofence(context, key)
                database.getReference("data/users/"+ user +"/reminderList/" + key + "/reminder_seen").setValue(true)
            }
        }
    }
}
