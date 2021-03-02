package com.example.mobilecomputinghomework

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext,workerParameters) {

    override fun doWork(): Result {
        val text = inputData.getString("message")
        val title = inputData.getString("title")
        ReminderList.showNofitication(applicationContext, text!!, title!!)
        return   Result.success()
    }
}