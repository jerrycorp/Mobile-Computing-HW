package com.example.mobilecomputinghomework

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilecomputinghomework.db.ReminderInfo
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class EditActivity() : AppCompatActivity() {
    private lateinit var creation_time: String
    private lateinit var creator_id: String
    private lateinit var reminder_seen: String
    private var timeInMillis by Delegates.notNull<Long>()
    private lateinit var location_x: String
    private lateinit var location_y: String
    private lateinit var key: String
    private lateinit var message: String
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        database = Firebase.database(getString(R.string.firebase_db_url))
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }
        updateExisting()
        setUpTimePickers()
    }

    private fun updateExisting() {
        val textViewTimeEdit = findViewById<TextView>(R.id.textViewTimeEdit)
        val textViewDateEdit = findViewById<TextView>(R.id.textViewDateEdit)
        val editTextReminderName = findViewById<EditText>(R.id.editTextReminderName)
        val editTextReminderMessage = findViewById<EditText>(R.id.editTextreminderMessage)

        val key: String? = intent.getStringExtra("key")
        val name: String? = intent.getStringExtra("name")
        val date: String? = intent.getStringExtra("date")
        val time: String? = intent.getStringExtra("time")
        val timeInMillis: Long? = intent.getLongExtra("timeInMillis", 0)
        val message: String? = intent.getStringExtra("message")
        val creation_time: String? = intent.getStringExtra("creation_time")
        val creator_id: String? = intent.getStringExtra("creator_id")
        val reminder_seen: String? = intent.getStringExtra("reminder_seen")
        val location_x: String? = intent.getStringExtra("location_x")
        val location_y: String? = intent.getStringExtra("location_y")
        if (key != null && name != null && date != null && time != null && creation_time != null && creator_id != null && reminder_seen != null && location_x != null && location_y != null && message != null &&timeInMillis != null) {
            this.key = key
            this.creation_time = creation_time
            this.creator_id = creator_id
            this.reminder_seen = reminder_seen
            this.location_x = location_x
            this.location_y = location_y
            this.message = message
            this.timeInMillis = timeInMillis
            textViewTimeEdit.text = time
            textViewDateEdit.text = date
            editTextReminderName.setText(name)
            editTextReminderMessage.setText(message)
            findViewById<TextView>(R.id.textViewcreationTime).setText(creation_time)
            findViewById<TextView>(R.id.textViewcreationTime).visibility = View.VISIBLE
            findViewById<TextView>(R.id.textViewCreationTimeDescriptor).visibility = View.VISIBLE
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
            this.creator_id = getLoggedInUsername()!!
            this.reminder_seen = ""
            this.location_x = ""
            this.location_y = ""
            this.timeInMillis = System.currentTimeMillis()
        }
    }

    private fun save() {
        if (key == "") {
            this.creation_time = SimpleDateFormat("HH:mm dd.MM.yyyy").format(System.currentTimeMillis())
        }
        val textViewTimeEdit = findViewById<TextView>(R.id.textViewTimeEdit)
        val textViewDateEdit = findViewById<TextView>(R.id.textViewDateEdit)
        val editTextReminderName = findViewById<EditText>(R.id.editTextReminderName)
        val editTextReminderMessage = findViewById<EditText>(R.id.editTextreminderMessage)
        val date = textViewDateEdit.getText().toString()
        val time = textViewTimeEdit.getText().toString()
        timeInMillis = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault()).parse(time + " " + date).time
        uploadNewReminder(ReminderInfo(123,
                "",
                editTextReminderName.getText().toString(),
                date,
                time,
                timeInMillis,
                editTextReminderMessage.getText().toString(),
                creation_time,
                creator_id,
                reminder_seen,
                location_x,
                location_y
        )
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
        val cal = Calendar.getInstance()

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