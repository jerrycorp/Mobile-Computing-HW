package com.example.mobilecomputinghomework

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            Log.d("Lab","Login Button Clicked")
            val username = findViewById<EditText>(R.id.username).getText().toString()
            val password = findViewById<EditText>(R.id.password).getText().toString()
            val pass = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getString("pass$username", "")
            if (pass == "") {
                Toast.makeText(
                    applicationContext,
                    "Username $username doesn't exist",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (pass == password) {
                login(username)
                Toast.makeText(
                    applicationContext,
                    "login succesful",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                Toast.makeText(
                    applicationContext,
                    "wrong password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            Log.d("Lab","Login Button Clicked")
            val username = findViewById<EditText>(R.id.username).getText().toString()
            val password = findViewById<EditText>(R.id.password).getText().toString()
            val pass = applicationContext.getSharedPreferences(
                "com.example.mobilecomputinghomework",
                Context.MODE_PRIVATE).getString("pass$username", "")
            if (username == "") {
                Toast.makeText(
                    applicationContext,
                    "You need to write an username",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (password.length < 5) {
                Toast.makeText(
                    applicationContext,
                    "You need to write a password that's at least 5 characters long",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else if (pass == ""){
                applicationContext.getSharedPreferences(
                    "com.example.mobilecomputinghomework",
                    Context.MODE_PRIVATE).edit().putString("pass$username", password).apply()
                login(username)
                Toast.makeText(
                    applicationContext,
                    "Succesfully registered and logged in",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                Toast.makeText(
                    applicationContext,
                    "Username already exists",
                    Toast.LENGTH_SHORT
                ).show()
            }
            //startActivity(
            //    Intent(applicationContext,RemainderList::class.java)
            //)
        }
        checkLoginUser()
    }

    private fun login(username: String) {
        applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).edit().putString("loginUser", username).apply()
        findViewById<EditText>(R.id.password).setText("")
        startActivity(Intent(applicationContext,RemainderList::class.java))
    }

    private fun checkLoginUser() {
        val loginStatus = applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
        if (loginStatus != "") startActivity(Intent(applicationContext,RemainderList::class.java))
    }
}