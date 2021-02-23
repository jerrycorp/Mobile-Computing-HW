package com.example.mobilecomputinghomework

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import java.util.*
import java.text.SimpleDateFormat
import com.example.mobilecomputinghomework.db.ReminderInfo
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EditActivity() : AppCompatActivity() {
    private lateinit var key: String
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        database = Firebase.database(getString(R.string.firebase_db_url))
        setUpTimePickers()
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        updateExisting()
    }

    private fun updateExisting() {
        val textViewTimeEdit = findViewById<TextView>(R.id.textViewTimeEdit)
        val textViewDateEdit = findViewById<TextView>(R.id.textViewDateEdit)
        val editTextReminderName = findViewById<EditText>(R.id.editTextReminderName)
        val key: String? = intent.getStringExtra("key")
        val name: String? = intent.getStringExtra("name")
        val date: String? = intent.getStringExtra("date")
        val time: String? = intent.getStringExtra("time")
        if (key != null && name != null && date != null && time != null) {
            this.key = key
            textViewTimeEdit.text = time
            textViewDateEdit.text = date
            editTextReminderName.setText(name)
            val deleteBtn = findViewById<Button>(R.id.btnDeleteReminder)
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.setOnClickListener {
                val reference = database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList/" + key)
                reference.removeValue()
                finish()
            }
        }
        else {
            this.key = ""
        }
    }

    private fun save() {

        val textViewTimeEdit = findViewById<TextView>(R.id.textViewTimeEdit)
        val textViewDateEdit = findViewById<TextView>(R.id.textViewDateEdit)
        val editTextReminderName = findViewById<EditText>(R.id.editTextReminderName)
        uploadNewReminder(ReminderInfo(123,"", editTextReminderName.getText().toString(),
                textViewDateEdit.getText().toString(),
                textViewTimeEdit.getText().toString())
        )
        finish()
    }

    private fun uploadNewReminder(reminderInfo: ReminderInfo) {
        if (key != "") {
            val reference = database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList/" + key)
            reference.setValue(reminderInfo)
        }
        else {
            val reference = database.getReference("data/users/"+ getLoggedInUsername() +"/reminderList")
            reference.push().setValue(reminderInfo)
        }
    }

    private fun setUpTimePickers() {
        //Time
        val textViewTimeEdit = findViewById<TextView>(R.id.textViewTimeEdit)
        textViewTimeEdit.text = SimpleDateFormat("HH:mm").format(System.currentTimeMillis())

        textViewTimeEdit.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                textViewTimeEdit.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        //date picker
        val textViewDateEdit = findViewById<TextView>(R.id.textViewDateEdit)
        textViewDateEdit.text = SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis())
        var cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
            textViewDateEdit.text = sdf.format(cal.time)

        }

        textViewDateEdit.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    private fun getLoggedInUsername(): String? {
        return applicationContext.getSharedPreferences(
            "com.example.mobilecomputinghomework",
            Context.MODE_PRIVATE).getString("loginUser", "")
    }
}