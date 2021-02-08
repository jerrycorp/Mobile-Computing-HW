package com.example.mobilecomputinghomework

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val username = applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
        findViewById<TextView>(R.id.nameInProfile).setText(username)

        updateProfile(username!!)

        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            var updated = false
            val storedNickname = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getString("nick$username", "")
            val storedFavouriteNumber = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getString("favn$username", "")
            val storedPassword = applicationContext.getSharedPreferences(
                    "com.example.mobilecomputinghomework",
                    Context.MODE_PRIVATE).getString("pass$username", "")
            val nickname = findViewById<EditText>(R.id.nickname).getText().toString()
            val favouriteNumber = findViewById<EditText>(R.id.favouriteNumber).getText().toString()
            val newPassword = findViewById<EditText>(R.id.newPassword).getText().toString()

            if (nickname != "" && storedNickname != nickname) {
                updated = true
                applicationContext.getSharedPreferences(
                    "com.example.mobilecomputinghomework",
                    Context.MODE_PRIVATE).edit().putString("nick$username", nickname).apply()
            }
            if (favouriteNumber != "" && storedFavouriteNumber != favouriteNumber) {
                updated = true
                applicationContext.getSharedPreferences(
                    "com.example.mobilecomputinghomework",
                    Context.MODE_PRIVATE).edit().putString("favn$username", favouriteNumber).apply()
            }
            if (newPassword != "" && storedPassword != newPassword) {
                updated = true
                applicationContext.getSharedPreferences(
                        "com.example.mobilecomputinghomework",
                        Context.MODE_PRIVATE).edit().putString("nick$username", newPassword).apply()
                findViewById<EditText>(R.id.newPassword).setText("")
            }
            if (updated) {
                Toast.makeText(
                        applicationContext,
                        "User information updated",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateProfile(username: String) {
        val nickname = applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("nick$username", "")
        if (nickname != "") {
            findViewById<EditText>(R.id.nickname).setText(nickname)
        }
        val favouriteNumber = applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("favn$username", "")
        if (favouriteNumber != "") {
            findViewById<EditText>(R.id.favouriteNumber).setText(favouriteNumber)
        }
    }
}