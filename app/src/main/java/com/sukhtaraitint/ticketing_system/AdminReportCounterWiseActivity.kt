package com.sukhtaraitint.ticketing_system

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.adapters.CounterReportAdapter
import com.sukhtaraitint.ticketing_system.listeners.OnItemClickListener
import com.sukhtaraitint.ticketing_system.models.*
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.sukhtaraitint.ticketing_system.utils.ProgressDialog
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private var rcvCounterReport: RecyclerView? = null
private var counterReportAdapter: CounterReportAdapter? = null

var ticketCount___: Int? = 0
var totalTicketAmount___: Double? = 0.0

var ticketSoldList__: MutableList<TicketSold>? = mutableListOf(TicketSold())
var reportType___: String? = ""
var reportTypeWithCounterType___: String? = "single_counter_wise"
var selectedCounterName___: String? = ""

var selectedCounterId___: Int? = 0

var counterList___ : ArrayList<String>? = ArrayList<String>()
var counterObjList___ : ArrayList<CounterReport>? = ArrayList<CounterReport>()

var counterGroupList___ : ArrayList<String>? = ArrayList<String>()
var counterGroupObjList___ : ArrayList<CounterReport>? = ArrayList<CounterReport>()

var ticketSoldReportCounterWise___: TotalTicketSoldReport? = null
var ticketSoldReportCountObj___: ValueEventListener? = null
var ticketSoldReportList___: MutableList<TotalTicketSoldReport>? = mutableListOf(TotalTicketSoldReport())

var selectedPos___ : Int = 0
var ticketSoldListener___ : ValueEventListener? = null

var counterWiseSellReport___: HashMap<String, Int>? = null

var isDataCalled___ : Boolean = false

var counterGroupId: Int? = 0

class AdminReportCounterWiseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_report_counter_wise)
        counterObjList___!!.clear()
        counterWiseSellReport___ = hashMapOf<String, Int>()

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        counterGroupId = intent!!.getIntExtra("counter_group_id", 0)

        initViews()
        loadData()
        initListeners()
    }

    private fun initViews() {
        rcvCounterReport = findViewById(R.id.rcv_counter_report)
    }

    private fun loadData() {
        reportType___ = sharedPref?.getString("user_type", "")
        val user_id = sharedPref?.getString("user_id", "")
        val user_name = sharedPref?.getString("user_name", "")
        val name = sharedPref?.getString("name", "")
        val phone = sharedPref?.getString("phone", "")
        val location = sharedPref?.getString("location", "")

        populateCounterList()

        getTicketSoldReportList()

        updateTodaysData("daily", reportTypeWithCounterType___!!)

        updateCounterReportUI()
    }

    private fun updateCounterReportUI() {
        var layoutManager = GridLayoutManager(applicationContext, 2)
        rcvCounterReport?.layoutManager = layoutManager
        counterReportAdapter = CounterReportAdapter(applicationContext)
        rcvCounterReport?.adapter = counterReportAdapter

        counterReportAdapter?.setDataList(counterObjList___!!)
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

        counterReportAdapter?.setOnItemClickListener(onItemClickListener)
    }

    private fun populateCounterList() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val counterRef = database.getReference("counters")

        val counterListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counters = dataSnapshot.getValue<List<Counters>>()
                Log.d("TAG", counters?.get(0)?.name + "")

                counterList___!!.clear()
                counters?.forEach {
                    if(it != null){
                        if(!it.user_name!!.equals("")){
                            if(it.group_id!! == counterGroupId){
                                var counterReport = CounterReport(it.id, it.name, 0, 0.0)
                                counterObjList___?.add(counterReport)
                                counterList___?.add("" + it.name)
                            }
                        }
                    }
                }

                if(counterObjList___!!.size > 0){
                    counterReportAdapter!!.setDataList(counterObjList___!!)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        counterRef.addValueEventListener(counterListener)
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

        /*if(reportType___!!.equals("supadmin")){
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
                if(reportType___!!.equals("supadmin")){
                    saveTotalSoldTicketReport()
                }
                true
            }
            R.id.daily_report -> {
                reportType__ = "daily"
                updateTodaysData(reportType__!!, reportTypeWithCounterType___!!)
                return true
            }
            R.id.add_others_amount -> {
                return true
            }
            R.id.monthly_report -> {
                reportType__ = "monthly"
                updateTodaysData(reportType__!!, reportTypeWithCounterType___!!)
                return true
            }
            R.id.total_report -> {
                reportType__ = "all"
                updateTodaysData(reportType__!!, reportTypeWithCounterType___!!)
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
//                    updateTodaysData("daily", reportTypeWithCounterType___!!)
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
        if(ticketCount___!! > 0){
            val totalTicketSoldReportObj = TotalTicketSoldReport(counterWiseSellReport___, ticketCount___,
                System.currentTimeMillis(),
                reportType___
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
                ticketSoldReportCountObj___ = object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.d("DataSnapshot", snapshot.getValue().toString())
                        if (snapshot.getValue() != null){
                            val ticketSoldCounterSet = snapshot.getValue() as Map<kotlin.String, *>
                            var totalTicketSoldCount = 0
                            for ((key, value) in ticketSoldCounterSet) {

                                val ticketSoldMap: Map<kotlin.String, *> = value as Map<kotlin.String, *>
                                var counterSellCount = 0

                                ticketSoldReportCounterWise___ = TotalTicketSoldReport(
                                    hashMapOf(),
                                    ticketSoldMap.get("total_tickets").toString().toInt(),
                                    ticketSoldMap.get("date_time").toString().toLong(),
                                    ticketSoldMap.get("report_taken_by").toString())
                                ticketSoldReportList___!!.add(ticketSoldReportCounterWise___!!)

                                totalTicketSoldCount = totalTicketSoldCount + ticketSoldMap.get("total_tickets").toString().toInt()

                            }

                            if(ticketSoldReportList___ != null && ticketSoldReportList___!!.size > 0){
                                ConstantValues.ticketSoldReportList =  ticketSoldReportList___
                            }
                        }

                        ticketSoldReportRef.removeEventListener(ticketSoldReportCountObj___!!)

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                        Log.d("DataSnapshot", error.toString())
                    }

                }
                ticketSoldReportRef.addValueEventListener(ticketSoldReportCountObj___!!)
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

    private fun updateTodaysData(reportType__: String, reportTypeWithCounterType___: String){
            var dialog = ProgressDialog.progressDialog(this)
            isDataCalled___ = true;

            totalTicketAmount___ = 0.0
            ticketCount___ = 0

            val database = Firebase.database(ConstantValues.DB_URL)
            val ticketSoldRef = database.getReference("ticket_sold")

            if(!isFinishing){
                dialog.show()
            }

            Executors.newSingleThreadExecutor().execute(Runnable {
                runOnUiThread{

                    // Get Post object and use the values to update the UI
                    totalTicketAmount___ = 0.0
                    ticketCount___ = 0

                    ticketSoldListener___ = object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Log.d("DataSnapshot", snapshot.getValue().toString())
                            var totalTicketSold : TicketSold? = null
                            ticketSoldList__?.clear()

                            if (snapshot.getValue() != null){
                                val ticketSoldCounterSet = snapshot.getValue() as Map<String, *>
                                for ((key, value) in ticketSoldCounterSet) {
                                    val ticketSoldMap: Map<String, *> = value as Map<String, *>
                                    var counterSellCount = 0
                                    var counterSellPrice = 0.0
                                    var ticketSoldSingleMap: Map<String, *>? = null
                                    for ((key1, value1) in ticketSoldMap) {
                                        ticketSoldSingleMap = value1 as Map<String, *>

                                        if(ticketSoldSingleMap.get("group_counter_id").toString().toInt() == counterGroupId){
                                            totalTicketSold = TicketSold(ticketSoldSingleMap.get("id").toString().toInt(),
                                                ticketSoldSingleMap.get("group_counter_id").toString().toInt(), ticketSoldSingleMap.get("from_counter_id").toString(),
                                                ticketSoldSingleMap.get("to_counter_id").toString(), ticketSoldSingleMap.get("price_total").toString(),
                                                ticketSoldSingleMap.get("total_tickets").toString().toInt(), ticketSoldSingleMap.get("date_time").toString().toLong(),
                                                ticketSoldSingleMap.get("sold_by_counter_id").toString())
                                            ticketSoldList__!!.add(totalTicketSold!!)
                                            counterSellCount = counterSellCount + ticketSoldSingleMap.get("total_tickets").toString().toInt()
                                            counterSellPrice = counterSellPrice + ticketSoldSingleMap.get("price_total").toString().toDouble()
                                        }else{
                                            break
                                        }
                                    }

                                    if(ticketSoldSingleMap!!.get("group_counter_id").toString().toInt() == counterGroupId){
                                        updateCounterReportData(counterGroupId!!, counterSellCount, counterSellPrice)
                                    }

                                }
                            }

//                            calculateBill()
                            ticketSoldRef.removeEventListener(ticketSoldListener___!!)

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    }

                    dialog.hide()
                    isDataCalled___ = false

//                    calculateBill()
                    ticketSoldRef.addValueEventListener(ticketSoldListener___!!)
                }
            })
//        }
    }

    private fun updateCounterReportData(counterId: Int, totalTicketSellCount: Int, totalTicketSellPrice: Double) {
        if(counterObjList___!!.size > 0){
            for (i in counterObjList___!!.indices) {
                val counterReport = counterObjList___!!.get(i)

                if(counterReport.id == counterId){
                    counterReport.total_ticket_sold_price = totalTicketSellPrice
                    counterReport.total_ticket_sold_count = totalTicketSellCount

                    counterObjList___!!.set(i, counterReport)
                }
            }

            counterReportAdapter!!.setDataList(counterObjList___!!)
        }
    }

//    private fun calculateBill() {
//        val totalPrice = ticketCount___!! * et_price!!.text.toString().trim().toDouble()
//        tv_total_bill!!.setText("সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(""+ticketCount___) + "\n" +
//        "টোটাল বিলঃ " + engNumToBangNum("" + totalPrice + " টাকা"))
//    }

    private fun getSelectedCounterId(selectedCounterName___: String?): String {
        if(counterObjList___ != null && counterObjList___!!.size > 0){
            for (counterObj in counterObjList___!!){
                if(counterObj.name!!.equals(selectedCounterName___)){
                    return ""+counterObj.id!!.toInt()
                }
            }
        }

        return ""
    }

    private fun getSelectedCounterGroupId(selectedCounterName___: String?): String {
        if(counterGroupObjList___ != null && counterGroupObjList___!!.size > 0){
            for (counterObj in counterGroupObjList___!!){
                if(counterObj.name!!.equals(selectedCounterName___)){
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