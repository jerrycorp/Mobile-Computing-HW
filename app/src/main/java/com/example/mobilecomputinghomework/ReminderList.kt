package com.example.mobilecomputinghomework

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.mobilecomputinghomework.databinding.ActivityReminderListBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.mobilecomputinghomework.db.ReminderInfo
import com.google.firebase.database.FirebaseDatabase

class ReminderList : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var binding: ActivityReminderListBinding
    private lateinit var database: FirebaseDatabase
    private var listOfReminders = mutableListOf<ReminderInfo>()
    override fun onBackPressed() {
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        listView = binding.reminderList

        //setContentView(R.layout.activity_reminder_list)
        updateNickname()
        //val listView = findViewById<ListView>(R.id.reminderList)

        database = Firebase.database(getString(R.string.firebase_db_url))
        loadReminderInfo()

        val reference = database.getReference("data/stringList")
        reference.push().setValue("hello")

         var ri = ReminderInfo(123,"note", "date", "time")
        val reference1 = database.getReference("data/"+ getLoggedInUsername() +"/reminderList")
        reference1.push().setValue(ri)


        //val prods = listOf("hello", "bye")
        //listView.adapter = ArrayAdapter<String>(this,
        //    android.R.layout.simple_list_item_1, prods)
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(applicationContext,ProfileActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnNewReminder).setOnClickListener {
            startActivity(Intent(applicationContext, EditActivity::class.java))
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
        refreshListView()
    }

    private fun refreshListView() {
        var refreshTask = LoadReminderInfoEntries()
        refreshTask.execute()

    }
    inner class LoadReminderInfoEntries : AsyncTask<String?, String?, List<ReminderInfo>>() {
        override fun doInBackground(vararg params: String?): List<ReminderInfo> {
            //
            //var reminderInfos = listOf()
            //val reference1 = database.getReference("data/"+ getLoggedInUsername() +"/reminderList")
            //reference1.get().addOnSuccessListener {
            //    Log.i("firebase", "Got value ${it.value} ${it.children}")
            //}.addOnFailureListener{
            //    Log.e("firebase", "Error getting data", it)
            //}
            //return listOf(ReminderInfo(123,"name", "date", "time"))
            return listOfReminders
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
        val reference1 = database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList")
        reference1.get().addOnSuccessListener {
            for (reminderInfo in it.children) {
                listOfReminders.add(ReminderInfo(
                    (reminderInfo.child("uid").value as Long).toInt(),
                    reminderInfo.child("name").value as String,
                    reminderInfo.child("date").value as String,
                    reminderInfo.child("time").value as String
                ))
            }
            refreshListView()
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }
}