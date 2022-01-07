package com.sukhtaraitint.ticketing_system

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.PaperRollUsed
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import java.util.*

class PaperRollEmptyActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "ticket-management"
    private val TAG = PaperRollEmptyActivity::class.java.name
    var sharedPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paper_roll)

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }


        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val edit = sharedPref?.edit()
        val todaysCalendar = Calendar.getInstance()
        todaysCalendar.timeInMillis = System.currentTimeMillis()
        var usedRollCountKey: String? = ""+todaysCalendar.get(Calendar.DAY_OF_MONTH + Calendar.MONTH + Calendar.YEAR)
        var totalRollCountToday: Int? = sharedPref?.getInt(usedRollCountKey, 0)
        totalRollCountToday = totalRollCountToday?.plus(1)

        edit?.putInt("UsedRollCount", totalRollCountToday!!.toInt())
        edit?.apply()

        val database = Firebase.database(ConstantValues.DB_URL)
        val usedPaperRollReference = database.getReference("used_paper_roll_count")
        usedPaperRollReference.keepSynced(true)

        val user_id = sharedPref?.getString("user_id", "")


        var paperRollUsed = PaperRollUsed(user_id, totalRollCountToday, "" + todaysCalendar.timeInMillis)
        usedPaperRollReference.child(usedRollCountKey + "_" + user_id).setValue(paperRollUsed)
                .addOnSuccessListener {
//                Toast.makeText(applicationContext, "Operation Successful. ", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Operation Failed. ", Toast.LENGTH_LONG).show()
                }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}