package com.example.mobilecomputinghomework.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "reminderInfo")
data class ReminderInfo(
        @PrimaryKey(autoGenerate = true) var uid: Int?,
        @ColumnInfo(name = "name") var name: String,
        @ColumnInfo(name = "accountNumber") var accountNumber: String,
        @ColumnInfo(name = "date") var date: String,
        @ColumnInfo(name = "amount") var amount: String
)