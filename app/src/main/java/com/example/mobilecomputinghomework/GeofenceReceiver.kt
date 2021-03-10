package com.example.mobilecomputinghomework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
                    name = intent.getStringExtra("name")!!
                    val timeInMillis = intent.getLongExtra("timeInMillis", 0)
                    //check time is good
                    val firebase = Firebase.database
                    val reference = firebase.getReference("data/users/$user/reminderList")
                    val reminderListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val reminder_seen = snapshot.child("reminder_seen").value as Boolean
                            if ((System.currentTimeMillis() > timeInMillis || timeInMillis == 9223372036854775807) && !reminder_seen) {
                                database.getReference("data/users/$user/reminderList/$key/reminder_seen").setValue(true)
                                ReminderList.showNofitication(context,text, name)
                                ReminderList.removeGeofence(context, key)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println("reminder:onCancelled: ${error.details}")
                        }

                        }
                    val child = reference.child(key)
                    child.addValueEventListener(reminderListener)
                }
            }
        }
    }
}
