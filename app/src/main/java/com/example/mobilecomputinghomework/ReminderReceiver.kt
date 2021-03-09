package com.example.mobilecomputinghomework

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class ReminderReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        // Retrieve data from intent
        val uid = intent?.getIntExtra("uid", 0)
        val text = intent?.getStringExtra("message")
        val title = intent?.getStringExtra("title")


        ReminderList.showNofitication(context!!,text!!, title!!)
    }
}