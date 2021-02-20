package com.example.mobilecomputinghomework

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RemainderList : AppCompatActivity() {
    override fun onBackPressed() {
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remainder_list)

        val username = applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
        findViewById<TextView>(R.id.name).setText(getName(username!!))

        val database = Firebase.database(getString(R.string.firebase_db_url))
        val reference = database.getReference("data/stringList")
        reference.push().setValue("hello")

        val reminderList = findViewById<ListView>(R.id.reminderList)
        val prods = listOf("hello", "bye")
        reminderList.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1, prods)
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(applicationContext,ProfileActivity::class.java))
        }
    }

    private fun getName(username: String): String {
        val storedNickname = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getString("nick$username", "")
        if (storedNickname != "") {
            return storedNickname!!
        }
        return username
        return "hi"
    }

    private fun logout() {
        applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).edit().putString("loginUser", "").apply()
        finish()
    }
}