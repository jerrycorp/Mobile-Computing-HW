package com.example.mobilecomputinghomework

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobilecomputinghomework.databinding.ActivityReminderListBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.mobilecomputinghomework.db.ReminderInfo
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit
import kotlin.random.Random

const val LOCATION_REQUEST_CODE = 123
const val GEOFENCE_LOCATION_REQUEST_CODE = 123
const val GEOFENCE_RADIUS = 500
const val GEOFENCE_ID = "REMINDER_GEOFENCE_ID"
const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 1000 //10 days
const val GEOFENCE_DWELL_DELAY = 10 * 1000 //10 seconds

class ReminderList : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var binding: ActivityReminderListBinding
    private lateinit var database: FirebaseDatabase
    private var listOfReminders = mutableListOf<ReminderInfo>()
    private var shownListOfReminders = mutableListOf<ReminderInfo>()
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var listOfGeofenceKeys = mutableListOf<String>()
    private lateinit var lastLocation: Location
    override fun onBackPressed() {
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //permission check
        if (!isLocationPermissionGranted()) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                LOCATION_REQUEST_CODE
            )
        }
        else {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            geofencingClient = LocationServices.getGeofencingClient(this)
        }
        getSavedLocation()
        listView = binding.reminderList
        updateNickname()
        database = Firebase.database(getString(R.string.firebase_db_url))
        database.getReference("data/users/"+ getLoggedInUsername() +"/accessed").setValue(System.currentTimeMillis())
        //loadReminderInfo()
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnNewReminder).setOnClickListener {
            startActivity(Intent(applicationContext, EditActivity::class.java))
        }
        findViewById<Button>(R.id.btnDebugLocation).setOnClickListener {
            startActivity(Intent(applicationContext, SelectCurrentLocationActivity::class.java))
        }
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, id ->
            val selectedReminder = listView.adapter.getItem(position) as ReminderInfo
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("key", selectedReminder.key)
            intent.putExtra("date", selectedReminder.date)
            intent.putExtra("time", selectedReminder.time)
            intent.putExtra("timeInMillis", selectedReminder.timeInMillis)
            intent.putExtra("name", selectedReminder.name)
            intent.putExtra("makeNotification", selectedReminder.makeNotification)
            intent.putExtra("creation_time", selectedReminder.creation_time)
            intent.putExtra("creator_id", selectedReminder.creator_id)
            intent.putExtra("reminder_seen", selectedReminder.reminder_seen)
            intent.putExtra("location_x", selectedReminder.location_x)
            intent.putExtra("location_y", selectedReminder.location_y)
            intent.putExtra("message", selectedReminder.message)
            startActivity(intent)
        }
        findViewById<Switch>(R.id.switchShowAll).setOnCheckedChangeListener { _, isChecked ->
            getSavedLocation()
        }
        findViewById<Switch>(R.id.switchDebugLocation).setOnCheckedChangeListener { _, isChecked ->
            getSavedLocation()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!isLocationPermissionGranted()) {
            Toast.makeText(
                applicationContext,
                "This application requires location access\n Enable it from system settings since android API 11",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun getLoggedInUsername(): String? {
        return applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
    }
    private fun updateNickname() {
        val username = applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
        findViewById<TextView>(R.id.name).setText(getName(username!!))
    }

    private fun getName(username: String): String {
        val storedNickname = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getString("nick$username", "")
        if (storedNickname != "") {
            return storedNickname!!
        }
        return username
    }

    private fun logout() {
        applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).edit().putString("loginUser", "").apply()
        finish()
    }
    override fun onResume() {
        super.onResume()
        updateNickname()
        getSavedLocation()
    }
    private fun getSavedLocation() {
        if (findViewById<Switch>(R.id.switchDebugLocation).isChecked) {
            val username = getLoggedInUsername()
            val x = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getFloat("x$username", 0F).toDouble()
            val y = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getFloat("y$username", 0F).toDouble()
            val loc = Location("")
            loc.latitude = x
            loc.longitude = y
            lastLocation = loc
            loadReminderInfo()
        } else {
            if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if (location != null) {
                        Log.i("geo", "current location is ${location.latitude}, ${location.longitude}")
                        lastLocation = location
                        loadReminderInfo()
                    } else {
                        Log.i("geo", "no location received from fused location provider")
                    }
                }
        }
    }
    private fun refreshListView() {
        if (shownListOfReminders.isNotEmpty()) {
            val adaptor = ReminderAdaptor(applicationContext, shownListOfReminders)
            listView.adapter = adaptor
        } else {
            listView.adapter = null
            Toast.makeText(applicationContext, "No items now", Toast.LENGTH_SHORT).show()
        }

    }
    private fun loadReminderInfo() {
        val reference1 = database.getReference("data/users/" + getLoggedInUsername() + "/reminderList")
        reference1.get().addOnSuccessListener {
            listOfReminders = mutableListOf<ReminderInfo>()
            for (reminderInfo in it.children) {
                Log.i("firebase", "a child: " + reminderInfo.child("name").value as String)
                listOfReminders.add(ReminderInfo(
                        (reminderInfo.child("uid").value as Long).toInt(),
                        reminderInfo.key.toString(),
                        reminderInfo.child("name").value as String,
                        reminderInfo.child("date").value as String,
                        reminderInfo.child("time").value as String,
                        reminderInfo.child("timeInMillis").value as Long,
                        reminderInfo.child("makeNotification").value as Boolean,
                        reminderInfo.child("message").value as String,
                        reminderInfo.child("creation_time").value as String,
                        reminderInfo.child("creator_id").value as String,
                        reminderInfo.child("reminder_seen").value as Boolean,
                        reminderInfo.child("location_x").value as String,
                        reminderInfo.child("location_y").value as String
                ))
            }
            val showAll: Boolean = findViewById<Switch>(R.id.switchShowAll).isChecked
            val currentTimeInMillis = System.currentTimeMillis()
            shownListOfReminders = mutableListOf<ReminderInfo>()
            LocationServices.getGeofencingClient(applicationContext).removeGeofences(listOfGeofenceKeys)
            listOfGeofenceKeys = mutableListOf<String>()
            addNotifications() //adds non geo based notifications
            for (reminderInfo in listOfReminders) {//go through shown reminders and make geofences
                var inside = false
                if (reminderInfo.location_x != "" && reminderInfo.location_y != "") {
                    val loc = Location("")
                    loc.latitude = reminderInfo.location_x.toDouble()
                    loc.longitude = reminderInfo.location_y.toDouble()
                    Log.i("geo", "distance to ${reminderInfo.name} is ${loc.distanceTo(lastLocation).toString()}")
                    if (loc.distanceTo(lastLocation) < GEOFENCE_RADIUS) {
                        inside = true
                        if (!reminderInfo.reminder_seen && reminderInfo.makeNotification) {
                            showNofitication(applicationContext,reminderInfo.message, reminderInfo.name)
                            database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList/" + reminderInfo.key + "/reminder_seen").setValue(true)
                        }
                    }
                    else {
                        if (reminderInfo.makeNotification && reminderInfo.timeInMillis > currentTimeInMillis && !reminderInfo.reminder_seen) {
                            createGeoFence(reminderInfo, geofencingClient)
                        }
                    }
                }
                else {
                    inside = true
                }
                if (showAll || (reminderInfo.timeInMillis < currentTimeInMillis || inside)) {
                    shownListOfReminders.add(reminderInfo)
                }
            }
            refreshListView()
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
            database.goOnline()
            database.getReference("data/users/"+ getLoggedInUsername() +"/errorTime")
                .setValue(System.currentTimeMillis())
            loadReminderInfo()
        }
    }

    private fun addNotifications() {
        //set all non geo reminders here.
        WorkManager.getInstance(applicationContext).cancelAllWork()
        for (reminderInfo in listOfReminders) {
            val currentTimeInMillis = System.currentTimeMillis()
            if (reminderInfo.timeInMillis != 9223372036854775807 &&
                (reminderInfo.timeInMillis > currentTimeInMillis) &&
                (reminderInfo.makeNotification) &&
                reminderInfo.location_x == "" &&
                reminderInfo.location_y == "") {
                setReminderWithWorkManager(
                        applicationContext,
                        reminderInfo.uid!!,
                        reminderInfo.timeInMillis,
                        reminderInfo.message,
                        reminderInfo.name
                )
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createGeoFence(reminder: ReminderInfo, geofencingClient: GeofencingClient) {
        val key = reminder.key
        val location = LatLng(reminder.location_x.toDouble(), reminder.location_y.toDouble())
        val message = reminder.message
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("key", key)
            .putExtra("name", reminder.name)
            .putExtra("timeInMillis", reminder.timeInMillis)
            .putExtra("message", "Geofence alert - ${location.latitude}, ${location.longitude}" +
            "\n" + message)
            .putExtra("user", getLoggedInUsername())
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("geo", "missing permissions")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                Log.i("geo", "geofencebuilt")
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {
            Log.i("geo", "geofencebuilt")
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
    }

    companion object {
        fun showNofitication(context: Context, message: String, title: String) {

            val CHANNEL_ID = "REMINDER_APP_NOTIFICATION_CHANNEL"
            val notificationId = Random.nextInt(10, 1000) + 5
            // notificationId += Random(notificationId).nextInt(1, 500)

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle(title)
                .setContentText(message)
                .setGroup(CHANNEL_ID)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Notification chancel needed since Android 8
            val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.app_name)
            }
            notificationManager.createNotificationChannel(channel)

            notificationManager.notify(notificationId, notificationBuilder.build())

        }

        fun setReminderWithWorkManager(
                context: Context,
                uid: Int,
                timeInMillis: Long,
                message: String,
                title: String
        ) {

            val reminderParameters = Data.Builder()
                    .putString("message", message)
                    .putString("title", title)
                    .putInt("uid", uid)
                    .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()

            val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(reminderParameters)
                    .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                    .build()

            WorkManager.getInstance(context).enqueue(reminderRequest)
        }

        fun removeGeofence(context: Context, key: String) {
            LocationServices.getGeofencingClient(context).removeGeofences(mutableListOf(key))
        }
    }
}