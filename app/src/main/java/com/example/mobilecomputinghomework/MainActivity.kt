package com.example.mobilecomputinghomework

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            Log.d("Lab","Login Button Clicked")

            startActivity(
                Intent(applicationContext,RemainderList::class.java)
            )
        }
    }
}