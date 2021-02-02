package com.mobicomp2020.mobilecomputinghomework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mobilecomputinghomework.db.ReminderDao
import com.example.mobilecomputinghomework.db.ReminderInfo

@Database(entities = arrayOf(ReminderInfo::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}

