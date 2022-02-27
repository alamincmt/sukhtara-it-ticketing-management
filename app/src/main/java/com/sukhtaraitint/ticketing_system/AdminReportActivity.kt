package com.sukhtaraitint.ticketing_system

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.adapters.CounterGroupAdapter
import com.sukhtaraitint.ticketing_system.adapters.CounterGroupReportAdapter
import com.sukhtaraitint.ticketing_system.listeners.OnItemClickListener
import com.sukhtaraitint.ticketing_system.models.*
import com.sukhtaraitint.ticketing_system.receivers.AlarmBroadcastReceiver
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.sukhtaraitint.ticketing_system.utils.ProgressDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

var tvReportDate : TextView? = null
var tvTotalTicket : TextView? = null
var tvTotalAmmount : TextView? = null

var startDateTime_ : Long? = 0
var endDateTime_ : Long? = 0

var fab_add_counter_ : FloatingActionButton? = null
private var rcvCounterGroup: RecyclerView? = null
private var counterGroupReportAdapter: CounterGroupReportAdapter? = null

var ticketCount__: Int? = 0
var totalTicketAmount__: Double? = 0.0

var ticketSoldList_: MutableList<TicketSold>? = mutableListOf(TicketSold())
var reportType__: String? = "daily"
var user_type__: String? = ""
var reportTypeWithCounterType__: String? = "single_counter_wise"
var selectedCounterName__: String? = ""
var selectedGroupID__: String? = ""

var selectedCounterId__: Int? = 0

var counterList__ : ArrayList<String>? = ArrayList<String>()
var counterObjList__ : ArrayList<Counters>? = ArrayList<Counters>()

var counterGroupList__ : ArrayList<String>? = ArrayList<String>()
var counterGroupObjList__ : ArrayList<CounterGroupsReport>? = ArrayList<CounterGroupsReport>()

var ticketSoldReportCounterWise__: TotalTicketSoldReport? = null
var ticketSoldReportCountObj__: ValueEventListener? = null
var ticketSoldReportList__: MutableList<TotalTicketSoldReport>? = mutableListOf(TotalTicketSoldReport())

var selectedPos__ : Int = 0
var selectedPosForCounterGroup__ : Int = 0
var ticketSoldListener__ : ValueEventListener? = null

var counterWiseSellReport__: HashMap<String, Int>? = null

var isDataCalled__ : Boolean = false

var counterGroupWiseTicketCount: HashMap<String, Int> = HashMap<String, Int>()
var counterGroupWiseTicketPrice: HashMap<String, Double> = HashMap<String, Double>()

class AdminReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_report)
        counterObjList__!!.clear()
        counterWiseSellReport__ = hashMapOf<String, Int>()

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        initViews()
        loadData()
        initListeners()
    }

    private fun initViews() {
        tvReportDate = findViewById(R.id.tvReportDate)
        tvTotalAmmount = findViewById(R.id.tvTotalAmmount)
        tvTotalTicket = findViewById(R.id.tvTotalTicket)

        fab_add_counter_ = findViewById(R.id.fab_add_counter)
        rcvCounterGroup = findViewById(R.id.rcv_counter_groups)
    }

    private fun loadData() {
        user_type__ = sharedPref?.getString("user_type", "")
        val user_id = sharedPref?.getString("user_id", "")
        val user_name = sharedPref?.getString("user_name", "")
        val name = sharedPref?.getString("name", "")
        val phone = sharedPref?.getString("phone", "")
        val location = sharedPref?.getString("location", "")

        populateCounterList()

        populateCounterGroupList()

        getTicketSoldReportList()

        updateTodaysData("daily", reportTypeWithCounterType__!!)

        updateGroupWiseReportData()

        updateCounterGroupsData()
    }

    private fun updateGroupWiseReportData() {
        var layoutManager = GridLayoutManager(applicationContext, 2)
        rcvCounterGroup?.layoutManager = layoutManager
        counterGroupReportAdapter = CounterGroupReportAdapter(applicationContext)
        rcvCounterGroup?.adapter = counterGroupReportAdapter

        counterGroupReportAdapter?.setDataList(counterGroupObjList__!!)
        var onItemClickListener: OnItemClickListener = object : OnItemClickListener {
            override fun itemClick(position: Int) {
//                toCounterID = "" + counterGroupObjList!!.get(position).id
//                toCounter = counterGroupObjList!!.get(position).name
//                getSelectedCounterTicketPrice(counter_group_id!!.toInt(), counterGroupObjList!!.get(position).id)
//
//                if(counter_group_id!!.toInt() != counterGroupObjList!!.get(position).id){
//                    printTicketAndSave()
//                }else{
//                    Toast.makeText(applicationContext!!, "গন্তব্য স্থান সঠিক নয় ।", Toast.LENGTH_LONG).show()
//                }
            }
        }

        counterGroupReportAdapter?.setOnItemClickListener(onItemClickListener)

        updateCounterGroupsData()
    }

    private fun populateCounterList() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val counterRef = database.getReference("counters")

        val counterListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counters = dataSnapshot.getValue<List<Counters>>()
                Log.d("TAG", counters?.get(0)?.name + "")

                counterList__!!.clear()
                counters?.forEach {
                    if(it != null){
                        if(!it.user_name!!.equals("")){
                            counterObjList__?.add(it)
                            counterList__?.add("" + it.name)
                        }
                    }
                }

                if(counterList__?.size!! > 0){
                    ConstantValues.counterList = counterObjList__
                    counterList__?.add(0, "সবগুলো কাউন্টার")
                    if (spinner_counter != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterList__!!.toArray()
                        )
                        spinner_counter?.adapter = adapter

                        spinner_counter?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedPos__ = position;
                                selectedCounterName__ = counterList__!![position]
                                selectedCounterId__ = counterObjList__!!.get(position).id
//                                updateTodaysData(reportType__!!, reportTypeWithCounterType__!!)
                                /*Toast.makeText(this@MainActivity,
                                    counterList__!![position] + " selected.", Toast.LENGTH_SHORT).show()*/
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

                counterGroupList__!!.clear()
                counters?.forEach {
                    if(it != null){
                        if(!it.name!!.equals("")){
                            var counterGroupsReport = CounterGroupsReport(it.id!!, it.name, 0, 0.0)
                            counterGroupObjList__?.add(counterGroupsReport)
                            counterGroupList__?.add("" + it.name)
                        }
                    }
                }

                if(counterGroupList__?.size!! > 0){
                    counterGroupReportAdapter!!.setDataList(counterGroupObjList__!!)
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

        fab_add_counter_?.setOnClickListener {
            startActivity(Intent(this, AddNewCounterActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_report_admin, menu)

        /*var menuItem: MenuItem = menu.findItem(R.id.delete_total_report)
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
        }*/

        /*if(user_type__!!.equals("supadmin")){
            if(menuItem != null){
                menuItem.setVisible(true)
            }

            if(billMenuItem != null){
                billMenuItem.setVisible(true)
            }

            if(priceMenuItem != null){
                priceMenuItem.setVisible(true)
            }
        }*/

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            /*R.id.sync_data -> {
                // todo: implement sync functionality here.
                if(user_type__!!.equals("supadmin")){
                    saveTotalSoldTicketReport()
                }
                true
            }
            R.id.daily_report -> {
                reportType__ = "daily"
                updateTodaysData(reportType__!!, reportTypeWithCounterType__!!)
                return true
            }
            R.id.add_others_amount -> {
                return true
            }
            R.id.monthly_report -> {
                reportType__ = "monthly"
                updateTodaysData(reportType__!!, reportTypeWithCounterType__!!)
                return true
            }
            R.id.total_report -> {
                reportType__ = "all"
                updateTodaysData(reportType__!!, reportTypeWithCounterType__!!)
                return true
            }
            R.id.delete_total_report -> {
//                showTicketSoldReportDeleteDialog("Are you sure want to delete all data?")
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
            }*/
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
//                    updateTodaysData("daily", reportTypeWithCounterType__!!)
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
        if(ticketCount__!! > 0){
            val totalTicketSoldReportObj = TotalTicketSoldReport(counterWiseSellReport__, ticketCount__,
                System.currentTimeMillis(),
                user_type__
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
                ticketSoldReportCountObj__ = object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("DataSnapshot", snapshot.getValue().toString())
                        if (snapshot.getValue() != null){
                            val ticketSoldCounterSet = snapshot.getValue() as Map<kotlin.String, *>
                            var totalTicketSoldCount = 0
                            for ((key, value) in ticketSoldCounterSet) {

                                val ticketSoldMap: Map<kotlin.String, *> = value as Map<kotlin.String, *>
                                var counterSellCount = 0

                                ticketSoldReportCounterWise__ = TotalTicketSoldReport(
                                    hashMapOf(),
                                    ticketSoldMap.get("total_tickets").toString().toInt(),
                                    ticketSoldMap.get("date_time").toString().toLong(),
                                    ticketSoldMap.get("report_taken_by").toString())
                                ticketSoldReportList__!!.add(ticketSoldReportCounterWise__!!)

                                totalTicketSoldCount = totalTicketSoldCount + ticketSoldMap.get("total_tickets").toString().toInt()

                            }

                            if(ticketSoldReportList__ != null && ticketSoldReportList__!!.size > 0){
                                ConstantValues.ticketSoldReportList =  ticketSoldReportList__
                            }
                        }

                        ticketSoldReportRef.removeEventListener(ticketSoldReportCountObj__!!)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                        Log.d("DataSnapshot", error.toString())
                    }

                }
                ticketSoldReportRef.addValueEventListener(ticketSoldReportCountObj__!!)
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

    private fun updateTodaysData(reportType__: String, reportTypeWithCounterType__: String){

        counterGroupWiseTicketPrice.clear()
        counterGroupWiseTicketCount.clear()

//        if(!isDataCalled__){
            var dialog = ProgressDialog.progressDialog(this)
            isDataCalled__ = true;

            totalTicketAmount__ = 0.0
            ticketCount__ = 0

            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
            tvTotalTicket?.setText(
                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                    "" + ticketCount__
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
                    totalTicketAmount__ = 0.0
                    ticketCount__ = 0

                    ticketSoldListener__ = object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("DataSnapshot", snapshot.getValue().toString())
                            var totalTicketSold : TicketSold? = null
                            ticketSoldList_?.clear()

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
                                        ticketSoldList_!!.add(totalTicketSold!!)
                                        counterSellCount = counterSellCount + ticketSoldSingleMap.get("total_tickets").toString().toInt()


                                        var date = Date(System.currentTimeMillis())
                                        val timeZoneDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                        var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))
                                        tvReportDate!!.setText("দৈনিক - ${mobileDateTime}")

                                        if(counterGroupWiseTicketCount.containsKey(ticketSoldSingleMap.get("group_counter_id").toString())){
                                            counterGroupWiseTicketCount.put(ticketSoldSingleMap.get("group_counter_id").toString(), counterGroupWiseTicketCount.get(ticketSoldSingleMap.get("group_counter_id").toString())!! + (ticketSoldSingleMap.get("total_tickets").toString().toInt()))
                                            counterGroupWiseTicketPrice.put(ticketSoldSingleMap.get("group_counter_id").toString(), counterGroupWiseTicketPrice.get(ticketSoldSingleMap.get("group_counter_id").toString())!! + (ticketSoldSingleMap.get("price_total").toString().toDouble()))
                                        }else{
                                            counterGroupWiseTicketCount.put(ticketSoldSingleMap.get("group_counter_id").toString(), ticketSoldSingleMap.get("total_tickets").toString().toInt())
                                            counterGroupWiseTicketPrice.put(ticketSoldSingleMap.get("group_counter_id").toString(), ticketSoldSingleMap.get("price_total").toString().toDouble())
                                        }

                                        // get total ticket sold + total ammount
                                        totalTicketAmount__ = totalTicketAmount__!! + ticketSoldSingleMap.get("price_total").toString().toDouble()
                                        ticketCount__ = ticketCount__!! + ticketSoldSingleMap.get("total_tickets").toString().toInt()

                                        tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                        tvTotalTicket?.setText(
                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                "" + ticketCount__
                                            )
                                        )

                                        /*if(reportTypeWithCounterType__!!.equals("single_counter_wise")){
                                            if(totalTicketSold != null){
                                                val cal = Calendar.getInstance()
                                                cal.timeInMillis = totalTicketSold.date_time!!

                                                val todaysCalendar = Calendar.getInstance()
                                                todaysCalendar.timeInMillis = System.currentTimeMillis()

                                                val selectedCounterIDInt = getSelectedCounterId(selectedCounterName__)

                                                // get daily reports
                                                if(reportType__.equals("daily")){
                                                    var date = Date(System.currentTimeMillis())
                                                    val timeZoneDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                                    var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))
                                                    tvReportDate!!.setText("দৈনিক - ${mobileDateTime}")

                                                    if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)){
                                                        if(selectedCounterName__ != null && selectedCounterName__!!.equals("") ||
                                                            selectedCounterName__ != null && selectedCounterName__!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.from_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType__.equals("monthly")){

                                                    val month: String = todaysCalendar.getDisplayName(
                                                        Calendar.MONTH,
                                                        Calendar.LONG,
                                                        Locale.getDefault()
                                                    )

                                                    val currentYear : Int = todaysCalendar.get(Calendar.YEAR)
                                                    tvReportDate!!.setText("মাসিক ${month}-${currentYear}")
                                                    if(todaysCalendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH)){
                                                        if(selectedCounterName__ != null && selectedCounterName__!!.equals("") ||
                                                            selectedCounterName__ != null && selectedCounterName__!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.from_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType__.equals("all")){
                                                    tvReportDate!!.setText("সব")
                                                    if(selectedCounterName__ != null && selectedCounterName__!!.equals("") ||
                                                        selectedCounterName__ != null && selectedCounterName__!!.equals("সবগুলো কাউন্টার")){

                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                        tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                        tvTotalTicket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount__
                                                            )
                                                        )
                                                    }

                                                    if(selectedCounterName__!!.equals(counterList__!!.get(selectedPos__))){
                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                        tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                        tvTotalTicket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount__
                                                            )
                                                        )
                                                    }
                                                }

                                                if(reportType__.equals("date_range")){
                                                    if(startDateTime_!! <= totalTicketSold.date_time!! && endDateTime_!! >= totalTicketSold.date_time!!){
                                                        if(selectedCounterName__ != null && selectedCounterName__!!.equals("") ||
                                                            selectedCounterName__ != null && selectedCounterName__!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.from_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }else if(reportTypeWithCounterType__!!.equals("group_counter_wise")){
                                            if(totalTicketSold != null){
                                                val cal = Calendar.getInstance()
                                                cal.timeInMillis = totalTicketSold.date_time!!

                                                val todaysCalendar = Calendar.getInstance()
                                                todaysCalendar.timeInMillis = System.currentTimeMillis()

                                                val selectedCounterIDInt = getSelectedCounterGroupId(selectedGroupID__)

                                                // get daily reports
                                                if(reportType__.equals("daily")){
                                                    var date = Date(System.currentTimeMillis())
                                                    val timeZoneDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                                    var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))
                                                    tvReportDate!!.setText("দৈনিক - ${mobileDateTime}")

                                                    if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)){
                                                        if(selectedGroupID__ != null && selectedGroupID__!!.equals("") ||
                                                            selectedGroupID__ != null && selectedGroupID__!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.group_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType__.equals("monthly")){

                                                    val month: String = todaysCalendar.getDisplayName(
                                                        Calendar.MONTH,
                                                        Calendar.LONG,
                                                        Locale.getDefault()
                                                    )

                                                    val currentYear : Int = todaysCalendar.get(Calendar.YEAR)
                                                    tvReportDate!!.setText("মাসিক ${month}-${currentYear}")
                                                    if(todaysCalendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH)){
                                                        if(selectedGroupID__ != null && selectedGroupID__!!.equals("") ||
                                                            selectedGroupID__ != null && selectedGroupID__!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.group_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }
                                                    }
                                                }

                                                // get daily reports
                                                if(reportType__.equals("all")){
                                                    tvReportDate!!.setText("সব")
                                                    if(selectedGroupID__ != null && selectedGroupID__!!.equals("") ||
                                                        selectedGroupID__ != null && selectedGroupID__!!.equals("সবগুলো কাউন্টার")){

                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                        tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                        tvTotalTicket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount__
                                                            )
                                                        )
                                                    }

                                                    if(selectedGroupID__!!.equals(counterGroupList__!!.get(
                                                            selectedPosForCounterGroup__))){
                                                        // get total ticket sold + total ammount
                                                        totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                        ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                        tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                        tvTotalTicket?.setText(
                                                            "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                "" + ticketCount__
                                                            )
                                                        )
                                                    }
                                                }

                                                if(reportType__.equals("date_range")){
                                                    if(startDateTime_!! <= totalTicketSold.date_time!! && endDateTime_!! >= totalTicketSold.date_time!!){
                                                        if(selectedGroupID__ != null && selectedGroupID__!!.equals("") ||
                                                            selectedGroupID__ != null && selectedGroupID__!!.equals("সবগুলো কাউন্টার")){

                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }

                                                        if(selectedCounterIDInt!!.equals(totalTicketSold.group_counter_id.toString())){
                                                            // get total ticket sold + total ammount
                                                            totalTicketAmount__ = totalTicketAmount__!! + totalTicketSold.price_total!!.toDouble()
                                                            ticketCount__ = ticketCount__!! + totalTicketSold.total_tickets!!.toInt()

                                                            tvTotalAmmount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmount__) + " টাকা")
                                                            tvTotalTicket?.setText(
                                                                "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                                                    "" + ticketCount__
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }*/
                                    }

                                    updateCounterGroupsData()
                                    counterWiseSellReport__!!.put(key, counterSellCount)

                                }
                            }

//                            calculateBill()
                            ticketSoldRef.removeEventListener(ticketSoldListener__!!)

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    }

                    dialog.hide()
                    isDataCalled__ = false

//                    calculateBill()
                    ticketSoldRef.addValueEventListener(ticketSoldListener__!!)

                    updateCounterGroupsData()
                }
            })
//        }
    }

    private fun updateCounterGroupsData() {
        if(counterGroupObjList__!!.size > 0){
            for (i in counterGroupObjList__!!.indices) {
                val counterGroupsReport = counterGroupObjList__!!.get(i)

                if(counterGroupWiseTicketPrice.containsKey(""+counterGroupsReport.id)){
                    counterGroupsReport.total_ticket_sold_price = counterGroupWiseTicketPrice.get(""+counterGroupsReport.id)!!
                    counterGroupsReport.total_ticket_sold_count = counterGroupWiseTicketCount.get(""+counterGroupsReport.id)!!

                    counterGroupObjList__!!.set(i, counterGroupsReport)
                }
            }

            counterGroupReportAdapter!!.setDataList(counterGroupObjList__!!)
        }
    }

//    private fun calculateBill() {
//        val totalPrice = ticketCount__!! * et_price!!.text.toString().trim().toDouble()
//        tv_total_bill!!.setText("সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(""+ticketCount__) + "\n" +
//        "টোটাল বিলঃ " + engNumToBangNum("" + totalPrice + " টাকা"))
//    }

    private fun getSelectedCounterId(selectedCounterName__: String?): String {
        if(counterObjList__ != null && counterObjList__!!.size > 0){
            for (counterObj in counterObjList__!!){
                if(counterObj.name!!.equals(selectedCounterName__)){
                    return ""+counterObj.id!!.toInt()
                }
            }
        }

        return ""
    }

    private fun getSelectedCounterGroupId(selectedCounterName__: String?): String {
        if(counterGroupObjList__ != null && counterGroupObjList__!!.size > 0){
            for (counterObj in counterGroupObjList__!!){
                if(counterObj.name!!.equals(selectedCounterName__)){
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