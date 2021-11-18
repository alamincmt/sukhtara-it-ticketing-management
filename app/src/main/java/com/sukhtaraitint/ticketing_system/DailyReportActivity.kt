package com.sukhtaraitint.ticketing_system

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.TicketSold
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.sukhtaraitint.ticketing_system.utils.ProgressDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

var tv_report_type_ : TextView? = null
var tv_total_ticket_ : TextView? = null
var tv_total_amount_ : TextView? = null

var ticketCount_: Int? = 0
var totalTicketAmount_: Double? = 0.0

var reportType_: String? = "daily"
var user_id: String? = "0"
var ticketSoldListenerObj : ValueEventListener? = null

class DailyReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_report)

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        initViews()
        loadData()
    }

    private fun initViews() {
        tv_report_type_ = findViewById(R.id.tv_report_type)
        tv_total_ticket_ = findViewById(R.id.tv_total_ticket)
        tv_total_amount_ = findViewById(R.id.tv_total_amount)
    }

    private fun loadData() {
        val user_type = sharedPref?.getString("user_type", "")
        user_id = sharedPref?.getString("user_id", "")
        val user_name = sharedPref?.getString("user_name", "")
        val name = sharedPref?.getString("name", "")
        val phone = sharedPref?.getString("phone", "")
        val location = sharedPref?.getString("location", "")

        updateTodaysData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sync_data -> {
                // todo: implement sync functionality here.

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateTodaysData(){

        var dialog = ProgressDialog.progressDialog(this)

        totalTicketAmount_ = 0.0
        ticketCount_ = 0

        tv_total_amount_?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount_) + " টাকা")
        tv_total_ticket_?.setText(
            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                "" + ticketCount_
            )
        )

        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldRef = database.getReference("ticket_sold")

        if(!isFinishing){
            dialog.show()
        }

        Executors.newSingleThreadExecutor().execute(Runnable {
            runOnUiThread{
                ticketSoldListenerObj = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get Post object and use the values to update the UI
                        totalTicketAmount_ = 0.0
                        ticketCount_ = 0
                        for (snapshot in dataSnapshot.children) {
                            val totalTicketSold = snapshot.getValue(TicketSold::class.java)
                            if(totalTicketSold != null){
                                val cal = Calendar.getInstance()
                                cal.timeInMillis = totalTicketSold.date_time!!

                                val todaysCalendar = Calendar.getInstance()
                                todaysCalendar.timeInMillis = System.currentTimeMillis()

                                // get daily reports
                                if(reportType_.equals("daily")){
                                    var date = Date(System.currentTimeMillis())
                                    val timeZoneDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                    var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))
                                    tv_report_type_!!.setText("দৈনিক - ${mobileDateTime}")

                                    if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)){
                                        if(user_id!!.equals(totalTicketSold.from_counter_id.toString())){
                                            // get total ticket sold + total ammount
                                            totalTicketAmount_ = totalTicketAmount_!! + totalTicketSold.price_total!!.toDouble()
                                            ticketCount_ = ticketCount_!! + totalTicketSold.total_tickets!!.toInt()

                                            tv_total_amount_?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount_) + " টাকা")
                                            tv_total_ticket_?.setText(
                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                    "" + ticketCount_
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        ticketSoldRef.removeEventListener(ticketSoldListenerObj!!)
                        dialog.hide()

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        ticketSoldRef.removeEventListener(ticketSoldListenerObj!!)
                        dialog.hide()
                        // Getting Post failed, log a message
                        Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                    }
                }
                ticketSoldRef.addValueEventListener(ticketSoldListenerObj!!)
            }
            /*
            requireActivity().runOnUiThread {
               // update views / ui if you are in a fragment
            }
            */
        })
    }

    fun engNumToBangNum(i: String): String? {
        val valueOf = i
        var str = ""
        for (i2 in 0 until valueOf.length) {
            str =
                if (valueOf[i2] == '1') str + "১" else if (valueOf[i2] == '2') str + "২" else if (valueOf[i2] == '3') str + "৩" else if (valueOf[i2] == '4') str + "৪" else if (valueOf[i2] == '5') str + "৫" else if (valueOf[i2] == '6') str + "৬" else if (valueOf[i2] == '7') str + "৭" else if (valueOf[i2] == '8') str + "৮" else if (valueOf[i2] == '9') str + "৯" else if (valueOf[i2] == '0') str + "০" else str + valueOf[i2]
        }
        return str
    }
}