package com.example.mobilecomputinghomework

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mobilecomputinghomework.databinding.ActivityReminderListBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.mobilecomputinghomework.db.ReminderInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class ReminderList : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var binding: ActivityReminderListBinding
    private lateinit var database: FirebaseDatabase
    private var listOfReminders = mutableListOf<ReminderInfo>()
    private var shownListOfReminders = mutableListOf<ReminderInfo>()
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onBackPressed() {
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        listView = binding.reminderList
        updateNickname()
        database = Firebase.database(getString(R.string.firebase_db_url))
        loadReminderInfo()
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnNewReminder).setOnClickListener {
            startActivity(Intent(applicationContext, EditActivity::class.java))
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
            loadReminderInfo()
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
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
        loadReminderInfo()
    }

    private fun refreshListView() {
        if (shownListOfReminders.isNotEmpty()) {
            val adaptor = ReminderAdaptor(applicationContext, shownListOfReminders)
            listView.adapter = adaptor
        } else {
            listView.adapter = null
            Toast.makeText(applicationContext, "No items now", Toast.LENGTH_SHORT).show()
        }
        //var refreshTask = LoadReminderInfoEntries()
        //refreshTask.execute()

    }
    inner class LoadReminderInfoEntries : AsyncTask<String?, String?, List<ReminderInfo>>() {
        override fun doInBackground(vararg params: String?): List<ReminderInfo> {

            //val reference1 = database.getReference("data/"+ getLoggedInUsername() +"/reminderList")
            //reference1.get().addOnSuccessListener {
            //    Log.i("firebase", "Got value ${it.key}")
            //}.addOnFailureListener{
            //    Log.e("firebase", "Error getting data", it)
            //}
            //return listOf(ReminderInfo(123,"name", "date", "time"))
            return shownListOfReminders
        }

        override fun onPostExecute(reminderInfos: List<ReminderInfo>?) {
            super.onPostExecute(reminderInfos)
            if (reminderInfos != null) {
                if (reminderInfos.isNotEmpty()) {
                    val adaptor = ReminderAdaptor(applicationContext, reminderInfos)
                    listView.adapter = adaptor
                } else {
                    listView.adapter = null
                    Toast.makeText(applicationContext, "No items now", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun loadReminderInfo() {
        try {
            val reference1 = database.getReference("data/users/" + getLoggedInUsername() + "/reminderList")
            reference1.get().addOnSuccessListener {
                listOfReminders = mutableListOf<ReminderInfo>()
                for (reminderInfo in it.children) {
                    Log.i("firebase", "a child" + reminderInfo.child("name").value as String)
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
                            reminderInfo.child("reminder_seen").value as String,
                            reminderInfo.child("location_x").value as String,
                            reminderInfo.child("location_y").value as String
                    ))
                }
                val showAll: Boolean = findViewById<Switch>(R.id.switchShowAll).isChecked
                val currentTimeInMillis = System.currentTimeMillis()
                shownListOfReminders = mutableListOf<ReminderInfo>()
                for (reminderInfo in listOfReminders) {
                    Log.i("time", currentTimeInMillis.toString() + " " + reminderInfo.timeInMillis.toString())
                    if (showAll or (reminderInfo.timeInMillis < currentTimeInMillis)) {
                        Log.i("time", "show this thing " + reminderInfo.name)
                        shownListOfReminders.add(reminderInfo)
                    } else {
                        Log.i("time", "don't show this thing " + reminderInfo.name)
                    }
                }
                refreshListView()
                addNotifications()
            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }
        }catch (e: java.lang.Exception){
            database.goOnline()
            loadReminderInfo()
        }
    }

    private fun addNotifications() {
        WorkManager.getInstance(applicationContext).cancelAllWork()
        for (reminderInfo in listOfReminders) {
            val currentTimeInMillis = System.currentTimeMillis()
            if ((reminderInfo.timeInMillis > currentTimeInMillis) and (reminderInfo.makeNotification)) {
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

    companion object {
        //val paymenthistoryList = mutableListOf<PaymentInfo>()

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

        fun cancelReminder(context: Context, pendingIntentId: Int) {

            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent =
                    PendingIntent.getBroadcast(
                            context,
                            pendingIntentId,
                            intent,
                            PendingIntent.FLAG_ONE_SHOT
                    )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }
}