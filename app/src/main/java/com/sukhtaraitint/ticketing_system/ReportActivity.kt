package com.sukhtaraitint.ticketing_system

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.CounterGroups
import com.sukhtaraitint.ticketing_system.models.Counters
import com.sukhtaraitint.ticketing_system.models.TicketSold
import com.sukhtaraitint.ticketing_system.models.TotalTicketSoldReport
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.sukhtaraitint.ticketing_system.utils.ProgressDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

var tv_report_type : TextView? = null
var tv_total_ticket : TextView? = null
var tv_total_amount : TextView? = null

var tv_total_amount_date_range : TextView? = null
var tv_total_ticket_date_range : TextView? = null
var tv_date_range_value : TextView? = null
var tv_end_date : TextView? = null
var tv_start_date : TextView? = null
var tv_total_bill : TextView? = null
var et_price : EditText? = null
var button_refresh_report : Button? = null

var ticketCountDateRange: Int? = 0
var totalTicketAmountDateRange: Double? = 0.0
var dateRangeValue: String? = "---"
var startDateTime : Long? = 0
var endDateTime : Long? = 0

var fab_add_counter : FloatingActionButton? = null
var rdg_counter_report_type : RadioGroup? = null
var ll_by_counter : LinearLayout? = null
var ll_by_counter_location : LinearLayout? = null
var spinner_counter : Spinner? = null
var spinner_counter_group : Spinner? = null

var ticketCount: Int? = 0
var totalTicketAmount: Double? = 0.0

var ticketSoldList: MutableList<TicketSold>? = mutableListOf(TicketSold())
var reportType: String? = "daily"
var user_type: String? = ""
var reportTypeWithCounterType: String? = "single_counter_wise"
var selectedCounterName: String? = ""
var selectedGroupID: String? = ""

var selectedCounterId: Int? = 0

var counterList : ArrayList<String>? = ArrayList<String>()
var counterObjList : ArrayList<Counters>? = ArrayList<Counters>()

var counterGroupList : ArrayList<String>? = ArrayList<String>()
var counterGroupObjList : ArrayList<CounterGroups>? = ArrayList<CounterGroups>()

var ticketSoldReportCounterWise: TotalTicketSoldReport? = null
var ticketSoldReportCountObj: ValueEventListener? = null
var ticketSoldReportList: MutableList<TotalTicketSoldReport>? = mutableListOf(TotalTicketSoldReport())

var selectedPos : Int = 0
var selectedPosForCounterGroup : Int = 0
var ticketSoldListener : ValueEventListener? = null

var counterWiseSellReport: HashMap<String, Int>? = null

var isDataCalled : Boolean = false

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        counterObjList!!.clear()
        counterWiseSellReport = hashMapOf<String, Int>()

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        initViews()
        loadData()
        initListeners()
    }

    private fun initViews() {
        tv_report_type = findViewById(R.id.tv_report_type)
        tv_total_ticket = findViewById(R.id.tv_total_ticket)
        tv_total_amount = findViewById(R.id.tv_total_amount)

        fab_add_counter = findViewById(R.id.fab_add_counter)
        spinner_counter = findViewById(R.id.spinner_counter)
        rdg_counter_report_type = findViewById(R.id.rdg_counter_report_type)
        ll_by_counter = findViewById(R.id.ll_by_counter)
        ll_by_counter_location = findViewById(R.id.ll_by_counter_location)
        ll_by_counter_location = findViewById(R.id.ll_by_counter_location)
        spinner_counter_group = findViewById(R.id.spinner_counter_group)

        tv_start_date = findViewById(R.id.tv_start_date)
        tv_end_date = findViewById(R.id.tv_end_date)
        tv_date_range_value = findViewById(R.id.tv_date_range_value)
        tv_total_ticket_date_range = findViewById(R.id.tv_total_ticket_date_range)
        tv_total_amount_date_range = findViewById(R.id.tv_total_amount_date_range)
        tv_total_bill = findViewById(R.id.tv_total_bill)
        et_price = findViewById(R.id.et_price)
        button_refresh_report = findViewById(R.id.button_refresh_report)
    }

    private fun loadData() {
        user_type = sharedPref?.getString("user_type", "")
        val user_id = sharedPref?.getString("user_id", "")
        val user_name = sharedPref?.getString("user_name", "")
        val name = sharedPref?.getString("name", "")
        val phone = sharedPref?.getString("phone", "")
        val location = sharedPref?.getString("location", "")

        if(user_type!!.equals("admin") || user_type!!.equals("supadmin")){
            fab_add_counter!!.visibility = View.VISIBLE
            if(user_type!!.equals("supadmin")){
                et_price!!.visibility = View.VISIBLE
            }
        }else{
            fab_add_counter!!.visibility = View.GONE
            et_price!!.visibility = View.GONE
        }

        populateCounterList()

        populateCounterGroupList()

        getTicketSoldReportList()

//        updateTodaysData("daily", reportTypeWithCounterType!!)
    }

    private fun populateCounterList() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val counterRef = database.getReference("counters")

        val counterListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counters = dataSnapshot.getValue<List<Counters>>()
                Log.d("TAG", counters?.get(0)?.name + "")

                counterList!!.clear()
                counters?.forEach {
                    if(it != null){
                        if(!it.user_name!!.equals("")){
                            counterObjList?.add(it)
                            counterList?.add("" + it.name)
                        }
                    }
                }

                if(counterList?.size!! > 0){
                    ConstantValues.counterList = counterObjList
                    counterList?.add(0, "সবগুলো কাউন্টার")
                    if (spinner_counter != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterList!!.toArray()
                        )
                        spinner_counter?.adapter = adapter

                        spinner_counter?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedPos = position;
                                selectedCounterName = counterList!![position]
                                selectedCounterId = counterObjList!!.get(position).id
//                                updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                                /*Toast.makeText(this@MainActivity,
                                    counterList!![position] + " selected.", Toast.LENGTH_SHORT).show()*/
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                // write code to perform some action
                            }
                        }
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        counterRef.addValueEventListener(counterListener)
    }

    private fun populateCounterGroupList() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val counterGroupRef = database.getReference("counter_groups")

        val counterGroupListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counters = dataSnapshot.getValue<List<CounterGroups>>()
                Log.d("TAG", counters?.get(0)?.name + "")

                counterGroupList!!.clear()
                counters?.forEach {
                    if(it != null){
                        if(!it.name!!.equals("")){
                            counterGroupObjList?.add(it)
                            counterGroupList?.add("" + it.name)
                        }
                    }
                }

                if(counterGroupList?.size!! > 0){
                    counterGroupList?.add(0, "সবগুলো কাউন্টার")
                    if (spinner_counter_group != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterGroupList!!.toArray()
                        )
                        spinner_counter_group?.adapter = adapter

                        spinner_counter_group?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedPosForCounterGroup = position;
                                selectedGroupID = counterGroupList!![position]
//                                updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                                /*Toast.makeText(this@MainActivity,
                                    counterGroupList!![position] + " selected.", Toast.LENGTH_SHORT).show()*/
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                // write code to perform some action
                            }
                        }
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        counterGroupRef.addValueEventListener(counterGroupListener)
    }

    private fun initListeners() {

        rdg_counter_report_type!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rdb_by_counter -> {
                    ll_by_counter!!.visibility = View.VISIBLE
                    ll_by_counter_location!!.visibility = View.GONE
                    reportTypeWithCounterType = "single_counter_wise"
//                    updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                }
                R.id.rdb_by_counters_location -> {
                    ll_by_counter!!.visibility = View.GONE
                    ll_by_counter_location!!.visibility = View.VISIBLE
                    reportTypeWithCounterType = "group_counter_wise"
//                    updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                }
                else -> ll_by_counter!!.visibility = View.VISIBLE
            }
        }

        fab_add_counter?.setOnClickListener {
            startActivity(Intent(this, AddNewCounterActivity::class.java))
        }

        tv_start_date!!.setOnClickListener{
            pickDateTime("start")
        }

        tv_end_date!!.setOnClickListener{
            pickDateTime("end")
        }

        button_refresh_report!!.setOnClickListener {
            updateTodaysData(reportType!!, reportTypeWithCounterType!!)
        }
    }

    private fun pickDateTime(startOrEnd : String) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)
                if(startOrEnd.equals("start")){
                    startDateTime = pickedDateTime.timeInMillis
                    tv_start_date!!.setText("" + day +"/"+ month +"/"+ year)
                }else if(startOrEnd.equals("end")){
                    endDateTime = pickedDateTime.timeInMillis
                    tv_end_date!!.setText("" + day +"/"+ month +"/"+ year)
                    reportType = "date_range"
//                    updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                }

//                tv_date_range_value!!.setText("" + tv_start_date!!.text.toString().trim() + " to " + tv_end_date!!.text.trim())
                Log.d("DateStart", "" + startDateTime)
            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_report, menu)

        var menuItem: MenuItem = menu.findItem(R.id.delete_total_report)
        var billMenuItem: MenuItem = menu.findItem(R.id.bill_generate)
        var priceMenuItem: MenuItem = menu.findItem(R.id.ticket_price)
        if(menuItem != null){
            menuItem.setVisible(false)
        }

        if(billMenuItem != null){
            billMenuItem.setVisible(false)
        }

        if(priceMenuItem != null){
            priceMenuItem.setVisible(false)
        }

        if(user_type!!.equals("supadmin")){
            if(menuItem != null){
                menuItem.setVisible(true)
            }

            if(billMenuItem != null){
                billMenuItem.setVisible(true)
            }

            if(priceMenuItem != null){
                priceMenuItem.setVisible(true)
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sync_data -> {
                // todo: implement sync functionality here.
                if(user_type!!.equals("supadmin")){
                    saveTotalSoldTicketReport()
                }
                true
            }
            R.id.daily_report -> {
                reportType = "daily"
//                updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                return true
            }
            R.id.monthly_report -> {
                reportType = "monthly"
//                updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                return true
            }
            R.id.total_report -> {
                reportType = "all"
//                updateTodaysData(reportType!!, reportTypeWithCounterType!!)
                return true
            }
            R.id.delete_total_report -> {
                showTicketSoldReportDeleteDialog("Are you sure want to delete all data?")
                return true
            }
            R.id.bill_generate -> {
                // todo: generate bill here.
                // get all the data from db
                // make pdf
                // send to transport owner

                startActivity(Intent(applicationContext, BillGenerateActivity::class.java))
                return true
            }
            R.id.ticket_price -> {
                startActivity(Intent(applicationContext, SetPriceActivity::class.java))
                return true
            }
            R.id.logout -> {
                val edit = sharedPref?.edit()
                edit?.putString("user_type", "")
                edit?.putString("user_id", "")
                edit?.putString("user_name", "")
                edit?.putString("name", "")
                edit?.putString("phone", "")
                edit?.putString("location", "")
                edit?.apply()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTicketSoldReportDeleteDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Warning")
        //set message for alert dialog
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton("Yes"){dialogInterface, which ->
            deleteTicketSoldReport()
//                    updateTodaysData("daily", reportTypeWithCounterType!!)
        }
        //performing negative action
        builder.setNegativeButton("No"){dialogInterface, which ->
            builder.create().dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun saveTotalSoldTicketReport() {
        if(ticketCount!! > 0){
            val totalTicketSoldReportObj = TotalTicketSoldReport(counterWiseSellReport, ticketCount,
                System.currentTimeMillis(),
                user_type
            )

            // Write a message to the database
            val database = Firebase.database(ConstantValues.DB_URL)
            val ticketSoldReportReference = database.getReference("daily_sell_report")
            ticketSoldReportReference.keepSynced(true)

            ticketSoldReportReference.child(createTicketSoldReportID()!!).setValue(totalTicketSoldReportObj)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Data Successfully Synced. ", Toast.LENGTH_LONG).show()
                    showTicketSoldReportDeleteDialog("Data Successfully Backed Up. You can delete data now.\n\nAre you sure want to delete all data?")
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Data Sync Failed\nPlease try again. ", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getTicketSoldReportList(){
        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldReportRef = database.getReference("daily_sell_report")
        Executors.newSingleThreadExecutor().execute(Runnable {
            runOnUiThread{
                ticketSoldReportCountObj = object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("DataSnapshot", snapshot.getValue().toString())
                        if (snapshot.getValue() != null){
                            val ticketSoldCounterSet = snapshot.getValue() as Map<kotlin.String, *>
                            var totalTicketSoldCount = 0
                            for ((key, value) in ticketSoldCounterSet) {

                                val ticketSoldMap: Map<kotlin.String, *> = value as Map<kotlin.String, *>
                                var counterSellCount = 0

                                ticketSoldReportCounterWise = TotalTicketSoldReport(
                                    hashMapOf(),
                                    ticketSoldMap.get("total_tickets").toString().toInt(),
                                    ticketSoldMap.get("date_time").toString().toLong(),
                                    ticketSoldMap.get("report_taken_by").toString())
                                ticketSoldReportList!!.add(ticketSoldReportCounterWise!!)

                                totalTicketSoldCount = totalTicketSoldCount + ticketSoldMap.get("total_tickets").toString().toInt()

                            }

                            if(ticketSoldReportList != null && ticketSoldReportList!!.size > 0){
                                ConstantValues.ticketSoldReportList =  ticketSoldReportList
                            }
                        }

                        ticketSoldReportRef.removeEventListener(ticketSoldReportCountObj!!)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                        Log.d("DataSnapshot", error.toString())
                    }

                }
                ticketSoldReportRef.addValueEventListener(ticketSoldReportCountObj!!)
            }
        })
    }

    @Throws(Exception::class)
    fun createTicketSoldReportID(): String? {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis()
    }

    private fun deleteTicketSoldReport() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldRef = database.getReference("ticket_sold")
        ticketSoldRef.setValue(null)
    }

    private fun updateTodaysData(reportType: String, reportTypeWithCounterType: String){

        if(!isDataCalled){
            var dialog = ProgressDialog.progressDialog(this)
            isDataCalled = true;

            totalTicketAmount = 0.0
            ticketCount = 0

            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
            tv_total_ticket?.setText(
                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                    "" + ticketCount
                )
            )

            val database = Firebase.database(ConstantValues.DB_URL)
            val ticketSoldRef = database.getReference("ticket_sold")

            if(!isFinishing){
                dialog.show()
            }

            Executors.newSingleThreadExecutor().execute(Runnable {
                runOnUiThread{

                    // Get Post object and use the values to update the UI
                    totalTicketAmount = 0.0
                    ticketCount = 0

                    ticketSoldListener = object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("DataSnapshot", snapshot.getValue().toString())
                            var totalTicketSold : TicketSold? = null
                            ticketSoldList?.clear()

                            if (snapshot.getValue() != null){
                                val ticketSoldCounterSet = snapshot.getValue() as Map<String, *>
                                for ((key, value) in ticketSoldCounterSet) {
                                    val ticketSoldMap: Map<String, *> = value as Map<String, *>
                                    var counterSellCount = 0
                                    for ((key1, value1) in ticketSoldMap) {
                                        val ticketSoldSingleMap: Map<String, *> = value1 as Map<String, *>

                                        totalTicketSold = TicketSold(ticketSoldSingleMap.get("id").toString().toInt(),
                                            ticketSoldSingleMap.get("group_counter_id").toString().toInt(), ticketSoldSingleMap.get("from_counter_id").toString(),
                                            ticketSoldSingleMap.get("to_counter_id").toString(), ticketSoldSingleMap.get("price_total").toString(),
                                            ticketSoldSingleMap.get("total_tickets").toString().toInt(), ticketSoldSingleMap.get("date_time").toString().toLong(),
                                            ticketSoldSingleMap.get("sold_by_counter_id").toString())
                                        ticketSoldList!!.add(totalTicketSold!!)
                                        counterSellCount = counterSellCount + ticketSoldSingleMap.get("total_tickets").toString().toInt()


                                        if(reportTypeWithCounterType!!.equals("single_counter_wise")){
                                            if(totalTicketSold != null){
                                                val cal = Calendar.getInstance()
                                                cal.timeInMillis = totalTicketSold.date_time!!

                                                val todaysCalendar = Calendar.getInstance()
                                                todaysCalendar.timeInMillis = System.currentTimeMillis()

                                                val selectedCounterIDInt = getSelectedCounterId(selectedCounterName)

                                                // get daily reports
                                                if(reportType.equals("daily")){
                                                    var date = Date(System.currentTimeMillis())
                                                    val timeZoneDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                                    var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))
                                                    tv_report_type!!.setText("দৈনিক - ${mobileDateTime}")

                                                    if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)){
                                                        if(selectedCounterName != null && selectedCounterName!!.equals("") ||
                                                            selectedCounterName != null && selectedCounterName!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.from_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType.equals("monthly")){

                                                    val month: String = todaysCalendar.getDisplayName(
                                                        Calendar.MONTH,
                                                        Calendar.LONG,
                                                        Locale.getDefault()
                                                    )

                                                    val currentYear : Int = todaysCalendar.get(Calendar.YEAR)
                                                    tv_report_type!!.setText("মাসিক ${month}-${currentYear}")
                                                    if(todaysCalendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH)){
                                                        if(selectedCounterName != null && selectedCounterName!!.equals("") ||
                                                            selectedCounterName != null && selectedCounterName!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.from_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType.equals("all")){
                                                    tv_report_type!!.setText("সব")
                                                    if(selectedCounterName != null && selectedCounterName!!.equals("") ||
                                                        selectedCounterName != null && selectedCounterName!!.equals("সবগুলো কাউন্টার")){

                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                        tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                        tv_total_ticket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount
                                                            )
                                                        )
                                                    }

                                                    if(selectedCounterName!!.equals(counterList!!.get(selectedPos))){
                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                        tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                        tv_total_ticket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount
                                                            )
                                                        )
                                                    }
                                                }

                                                if(reportType.equals("date_range")){
                                                    if(startDateTime!! <= totalTicketSold.date_time!! && endDateTime!! >= totalTicketSold.date_time!!){
                                                        if(selectedCounterName != null && selectedCounterName!!.equals("") ||
                                                            selectedCounterName != null && selectedCounterName!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.from_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }else if(reportTypeWithCounterType!!.equals("group_counter_wise")){
                                            if(totalTicketSold != null){
                                                val cal = Calendar.getInstance()
                                                cal.timeInMillis = totalTicketSold.date_time!!

                                                val todaysCalendar = Calendar.getInstance()
                                                todaysCalendar.timeInMillis = System.currentTimeMillis()

                                                val selectedCounterIDInt = getSelectedCounterGroupId(selectedGroupID)

                                                // get daily reports
                                                if(reportType.equals("daily")){
                                                    var date = Date(System.currentTimeMillis())
                                                    val timeZoneDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                                    var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))
                                                    tv_report_type!!.setText("দৈনিক - ${mobileDateTime}")

                                                    if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)){
                                                        if(selectedGroupID != null && selectedGroupID!!.equals("") ||
                                                            selectedGroupID != null && selectedGroupID!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.group_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType.equals("monthly")){

                                                    val month: String = todaysCalendar.getDisplayName(
                                                        Calendar.MONTH,
                                                        Calendar.LONG,
                                                        Locale.getDefault()
                                                    )

                                                    val currentYear : Int = todaysCalendar.get(Calendar.YEAR)
                                                    tv_report_type!!.setText("মাসিক ${month}-${currentYear}")
                                                    if(todaysCalendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH)){
                                                        if(selectedGroupID != null && selectedGroupID!!.equals("") ||
                                                            selectedGroupID != null && selectedGroupID!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.group_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType.equals("all")){
                                                    tv_report_type!!.setText("সব")
                                                    if(selectedGroupID != null && selectedGroupID!!.equals("") ||
                                                        selectedGroupID != null && selectedGroupID!!.equals("সবগুলো কাউন্টার")){

                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                        tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                        tv_total_ticket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount
                                                            )
                                                        )
                                                    }

                                                    if(selectedGroupID!!.equals(counterGroupList!!.get(
                                                            selectedPosForCounterGroup))){
                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                        tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                        tv_total_ticket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount
                                                            )
                                                        )
                                                    }
                                                }

                                                if(reportType.equals("date_range")){
                                                    if(startDateTime!! <= totalTicketSold.date_time!! && endDateTime!! >= totalTicketSold.date_time!!){
                                                        if(selectedGroupID != null && selectedGroupID!!.equals("") ||
                                                            selectedGroupID != null && selectedGroupID!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.group_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount = totalTicketAmount!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount = ticketCount!! + totalTicketSold.total_tickets!!.toInt()

                                                            tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount) + " টাকা")
                                                            tv_total_ticket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    counterWiseSellReport!!.put(key, counterSellCount)

                                }
                            }

                            calculateBill()
                            ticketSoldRef.removeEventListener(ticketSoldListener!!)

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    }

                    dialog.hide()
                    isDataCalled = false

                    calculateBill()
                    ticketSoldRef.addValueEventListener(ticketSoldListener!!)
                }
            })
        }
    }

    private fun calculateBill() {
        val totalPrice = ticketCount!! * et_price!!.text.toString().trim().toDouble()
        tv_total_bill!!.setText("সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(""+ticketCount) + "\n" +
        "টোটাল বিলঃ " + engNumToBangNum("" + totalPrice + " টাকা"))
    }

    private fun getSelectedCounterId(selectedCounterName: String?): String {
        if(counterObjList != null && counterObjList!!.size > 0){
            for (counterObj in counterObjList!!){
                if(counterObj.name!!.equals(selectedCounterName)){
                    return ""+counterObj.id!!.toInt()
                }
            }
        }

        return ""
    }

    private fun getSelectedCounterGroupId(selectedCounterName: String?): String {
        if(counterGroupObjList != null && counterGroupObjList!!.size > 0){
            for (counterObj in counterGroupObjList!!){
                if(counterObj.name!!.equals(selectedCounterName)){
                    return ""+counterObj.id!!.toInt()
                }
            }
        }

        return ""
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