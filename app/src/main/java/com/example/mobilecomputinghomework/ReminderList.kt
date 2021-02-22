package com.example.mobilecomputinghomework

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ReminderList : AppCompatActivity() {
    private lateinit var listView: ListView
    override fun onBackPressed() {
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_list)
        updateNickname()
        val listView = findViewById<ListView>(R.id.reminderList)

        val database = Firebase.database(getString(R.string.firebase_db_url))
        val reference = database.getReference("data/stringList")
        reference.push().setValue("hello")

        val prods = listOf("hello", "bye")
        listView.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, prods)
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(applicationContext,ProfileActivity::class.java))
        }
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
}