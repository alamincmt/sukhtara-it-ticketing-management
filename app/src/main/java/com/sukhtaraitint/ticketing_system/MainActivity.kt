package com.sukhtaraitint.ticketing_system

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Bundle
import android.print.*
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ahmedelsayed.sunmiprinterutill.PrintMe
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import com.sunmi.peripheral.printer.WoyouConsts
import woyou.aidlservice.jiuiv5.ICallback
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import android.content.IntentFilter
import woyou.aidlservice.jiuiv5.IWoyouService

import android.os.IBinder

import android.content.ComponentName

import android.content.ServiceConnection
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sukhtaraitint.ticketing_system.adapters.CounterGroupAdapter
import com.sukhtaraitint.ticketing_system.listeners.OnItemClickListener
import com.sukhtaraitint.ticketing_system.models.*
import com.sukhtaraitint.ticketing_system.receivers.PrinterStatusReceiver


class MainActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "ticket-management"
    private val TAG = MainActivity::class.java.name
    var sharedPref: SharedPreferences? = null

    var serialNo: Int? = 1
    var ticketCount: Int? = 1
    var totalTicketAmount: Int? = 45
    var perTicketPrice: Int? = 45
    var ticketCountToday: Int? = 0

    var totalTicketAmountToday: Double? = 0.0
    var fromCounter: String? = ""

    var counter_group_id: String? = "0"

    var toCounter: String? = ""

    var ticketSoldId: Int? = 2
    var toCounterID: String? = ""
    var fromCounterID: String? = ""

    var isStudentFareEnabled : Boolean? = false
    var fair_percentage: Int? = 50

    var isTollAmountEnabled : Boolean? = false
    var toll_amount: Int? = 0

    var tv_username : TextView? = null
    var tv_phonenumber : TextView? = null

    var btn_daily_report : Button? = null

    var tv_counter_from : TextView? = null
    var spinner_to : Spinner? = null

    var tv_ticket_plus : TextView? = null
    var tv_ticket_count : TextView? = null
    var tv_minus : TextView? = null
    var tv_total_ticket : TextView? = null
    var tv_total_amount : TextView? = null
    var tv_total_vara : TextView? = null

    var ll_print : LinearLayout? = null

    var counterList : ArrayList<String>? = ArrayList<String>()

    private var chbx_student_fare: CheckBox? = null
    private var ll_student_fare: LinearLayout? = null

    private var rcvCounterGroup: RecyclerView? = null
    private var counterGroupAdapter: CounterGroupAdapter? = null

    var printMe : PrintMe? = null
    var ticketSoldObjct : TicketSold? = null

    var counterGroupList : ArrayList<String>? = ArrayList<String>()
    var counterGroupObjList : ArrayList<CounterGroups>? = ArrayList<CounterGroups>()

    private var woyouService: IWoyouService? = null
    private val printerStatusReceiver: PrinterStatusReceiver = PrinterStatusReceiver()

    private val connService: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            woyouService = null
            Log.d(TAG, "Service disconnected")
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            woyouService = IWoyouService.Stub.asInterface(service)
            Log.d(TAG, "Service connected")
        }
    }

    val OUT_OF_PAPER_ACTION = "woyou.aidlservice.jiuv5.OUT_OF_PAPER_ACTION"
    val ERROR_ACTION = "woyou.aidlservice.jiuv5.ERROR_ACTION"
    val NORMAL_ACTION = "woyou.aidlservice.jiuv5.NORMAL_ACTION"
    val COVER_OPEN_ACTION = "woyou.aidlservice.jiuv5.COVER_OPEN_ACTION"
    val COVER_ERROR_ACTION = "woyou.aidlservice.jiuv5.COVER_ERROR_ACTION"
    val KNIFE_ERROR_1_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_1"
    val KNIFE_ERROR_2_ACTION = "woyou.aidlservice.jiuv5.KNIFE_ERROR_ACTION_2"
    val OVER_HEATING_ACITON = "woyou.aidlservice.jiuv5.OVER_HEATING_ACITON"
    val FIRMWARE_UPDATING_ACITON = "woyou.aidlservice.jiuv5.FIRMWARE_UPDATING_ACITON"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        Firebase.database.setPersistenceEnabled(true)
        printMe = PrintMe(this)

        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val edit = sharedPref?.edit()
        var timeInMillis = sharedPref!!.getLong("TimeMillisForSerialNo", 0)

        val calSharedPrefValue = Calendar.getInstance()
        calSharedPrefValue.timeInMillis = timeInMillis

        val todaysCalendar = Calendar.getInstance()
        todaysCalendar.timeInMillis = System.currentTimeMillis()

        if(timeInMillis.toInt() == 0){
            timeInMillis = System.currentTimeMillis()
            edit?.putLong("TimeMillisForSerialNo", timeInMillis)
            edit?.apply()
        }else{
            if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == calSharedPrefValue.get(Calendar.DAY_OF_MONTH)){
                serialNo = sharedPref!!.getInt("SerialNo", 0)
            }else{
                timeInMillis = System.currentTimeMillis()
                edit?.putLong("TimeMillisForSerialNo", timeInMillis)
                edit?.apply()

                serialNo = 0
                edit?.putInt("SerialNo", serialNo!!)
                edit?.apply()
            }
        }

        serialNo = sharedPref!!.getInt("SerialNo", 0)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

//        syncOfflineData()

        initViews()
        populateCounterGroupList()
        loadData()
        initListeners()
    }

    private fun initListeners() {

        val intent = Intent()
        intent.setPackage("woyou.aidlservice.jiuiv5")
        intent.action = "woyou.aidlservice.jiuiv5.IWoyouService"

        applicationContext.startService(intent)
        applicationContext.bindService(intent, connService, BIND_AUTO_CREATE)

        val mFilter = IntentFilter()
        mFilter.addAction(OUT_OF_PAPER_ACTION)
        mFilter.addAction(ERROR_ACTION)
        mFilter.addAction(NORMAL_ACTION)
        mFilter.addAction(COVER_OPEN_ACTION)
        mFilter.addAction(COVER_ERROR_ACTION)
        mFilter.addAction(KNIFE_ERROR_1_ACTION)
        mFilter.addAction(KNIFE_ERROR_2_ACTION)
        mFilter.addAction(OVER_HEATING_ACITON)
        mFilter.addAction(FIRMWARE_UPDATING_ACITON)

        applicationContext.registerReceiver(printerStatusReceiver, mFilter)

        tv_ticket_plus?.setOnClickListener{
            ticketCount = ticketCount!! + 1;
            calculatePrice()
        }

        tv_minus?.setOnClickListener{
            if(ticketCount!! > 1){
                ticketCount = ticketCount!! - 1;
                calculatePrice()
            }
        }

        chbx_student_fare!!.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener,
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: RadioGroup?, p1: Int) {
                TODO("Not yet implemented")
            }

            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                isStudentFareEnabled = p1
                calculatePrice()
            }

        })

        btn_daily_report?.setOnClickListener {
            startActivity(Intent(applicationContext, DailyReportActivity::class.java))
            /*if(isInternetOn(applicationContext)){
                startActivity(Intent(applicationContext, DailyReportActivity::class.java))
            }else{
                Toast.makeText(applicationContext, "??????????????????????????? ????????????????????? ???????????? ?????????????????? ?????????????????? ???????????? ???", Toast.LENGTH_LONG).show()
            }*/
        }

        ll_print?.setOnClickListener {
            var date = Date(System.currentTimeMillis())
            val timeZoneDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))

            val contactMobiles = engNumToBangNum("01915150908\n01913260001,01831301012");
            val totalTicketAmountBn = engNumToBangNum("" + totalTicketAmount)

            if(!fromCounterID.equals("") && !toCounterID.equals("")){

                printMe?.sendTextToPrinter("???????????? ???????????????????????????????????? ?????????\n", 28f, true, true, 1)
                serialNo = sharedPref!!.getInt("SerialNo", 0)
                serialNo = serialNo!! + 1


                val user_id = sharedPref?.getString("user_id", "")

                val edit = sharedPref?.edit()
                edit?.putInt("SerialNo", serialNo!!)
                edit?.apply()

                var serialNoBN = engNumToBangNum("" + serialNo)
                var ticketCountBN = engNumToBangNum("" + ticketCount)
                var spannable : SpannableString
                if(counter_group_id!!.equals("7")){
                    spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n??????????????? ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0, // start
                        21, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }else{
                    spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n(???????????? ?????? ???????????? + ????????? ??? ???????????? ??????????????? ???????????????) = ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0, // start
                        21, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }


                printMe?.sendTextToPrinter(spannable.toString(), 26f, false, false, 2)
                tv_ticket_count?.setText(engNumToBangNum("" + ticketCount))

                ticketSoldId = ticketSoldId!! + 1;
                addTicketSold(
                    ticketSoldId!!,
                    counter_group_id!!.toInt(),
                    "" + fromCounterID,
                    "" + toCounterID,
                    totalTicketAmount.toString(),
                    ticketCount!!.toInt(),
                    System.currentTimeMillis()
                )

                ticketCount = 1
                calculatePrice()

//
//                val htmlDocument =
//                    "<html><body style=\"background-color: #FFFFFF;\"><h4 style=\"color:black;\">???????????? ???????????????????????????????????? ?????????</h4><b style=\"color:black;\">????????????????????? ????????? ${randomTicketID}</b><p style=\"color:black;\">?????????????????? ${mobileDateTime}<br/>??????????????? ${totalTicketAmountBn} ????????????<br/>??????????????? ${fromCounter}<br/>???????????????????????? ${toCounter}<br/>??????????????????/???????????????????????? ${contactMobiles}</p></body></html>"
//                doWebViewPrint(htmlDocument)
            }else{
                return@setOnClickListener;
            }
        }
    }

    private fun loadData() {
        val user_type = sharedPref?.getString("user_type", "")
        val user_id = sharedPref?.getString("user_id", "")
        val user_name = sharedPref?.getString("user_name", "")
        val name = sharedPref?.getString("name", "")
        counter_group_id = sharedPref?.getString("group_counter_id", "")
        fromCounter = name
        fromCounterID = user_id
        val phone = sharedPref?.getString("phone", "")
        val location = sharedPref?.getString("location", "")

        tv_username?.setText(name)
        tv_phonenumber?.setText(phone)
        tv_counter_from?.setText(name)

        if(counter_group_id!!.equals("7")){
            perTicketPrice = 40
            totalTicketAmount = 40
        }

        tv_ticket_count?.setText(engNumToBangNum("" + ticketCount))
        tv_total_vara?.setText(engNumToBangNum("" + totalTicketAmount) + " ????????????")

        val database = Firebase.database(ConstantValues.DB_URL)
        val counterRef = database.getReference("counters")
        val studentFairRef = database.getReference("student_fair")
        val tollPriceRef = database.getReference("toll_price")

        counterRef.keepSynced(true)

        val counterListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counters = dataSnapshot.getValue<List<Counters>>()
                Log.d("TAG", counters?.get(0)?.name + "")

                counters?.forEach() {
                    if(it != null){
                        if(!it.user_name!!.equals("")){
                            counterList?.add("" + it.name)
                        }
                    }
                }

                if(counterList?.size!! > 0){
                    if (spinner_to != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterList!!.toArray()
                        )
                        spinner_to?.adapter = adapter

                        spinner_to?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                toCounter = counterList!![position]

                                if(counters != null && counters.get(position) != null){
                                    toCounterID = ""+counters.get(position).id
                                    getSelectedCounterTicketPrice(counter_group_id!!.toInt(), counters.get(position).group_id)
                                }
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

        val studentFairListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
//                val studentFairStatus = dataSnapshot.getValue<List<Counters>>()
                val studentFairMap: Map<kotlin.String, *> = dataSnapshot.getValue() as Map<kotlin.String, *>
                Log.d("TAG", " value is: "+ studentFairMap.get("fair_active_status"))

                if(studentFairMap.get("fair_active_status")!!.equals("active")){
                    fair_percentage = studentFairMap.get("fair_percentage").toString().toInt()
                    ll_student_fare!!.visibility = View.VISIBLE
                }else{
                    fair_percentage = 0
                    ll_student_fare!!.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        studentFairRef.addValueEventListener(studentFairListener)

        val tollPriceListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val tollMap: Map<kotlin.String, *> = dataSnapshot.getValue() as Map<kotlin.String, *>
                Log.d("TAG", " value is: "+ tollMap.get("active_status"))

                if(tollMap.get("active_status")!!.equals("active")){
                    toll_amount = tollMap.get("toll_price").toString().toInt()
                }else{
                    toll_amount = 0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }

        tollPriceRef.addValueEventListener(tollPriceListener)

//        updateTodaysData()
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
                counterGroupObjList!!.clear()
                counters?.forEach {
                    if(it != null){
                        if(!it.name!!.equals("")){
                            counterGroupObjList?.add(it)
                            counterGroupList?.add("" + it.name)
                        }
                    }
                }

                if(counterGroupList?.size!! > 0){

                    var layoutManager = GridLayoutManager(applicationContext, 2)
                    rcvCounterGroup?.layoutManager = layoutManager
                    counterGroupAdapter = CounterGroupAdapter(applicationContext)
                    rcvCounterGroup?.adapter = counterGroupAdapter

                    counterGroupAdapter?.setDataList(counterGroupObjList!!)
                    var onItemClickListener: OnItemClickListener = object : OnItemClickListener{
                        override fun itemClick(position: Int) {
                            toCounterID = "" + counterGroupObjList!!.get(position).id
                            toCounter = counterGroupObjList!!.get(position).name
                            getSelectedCounterTicketPrice(counter_group_id!!.toInt(), counterGroupObjList!!.get(position).id)

                            if(counter_group_id!!.toInt() != counterGroupObjList!!.get(position).id){
                                printTicketAndSave()
                            }else{
                                Toast.makeText(applicationContext!!, "????????????????????? ??????????????? ???????????? ?????? ???", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    counterGroupAdapter?.setOnItemClickListener(onItemClickListener)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        counterGroupRef.addValueEventListener(counterGroupListener)
    }

    private fun printTicketAndSave(){
        var date = Date(System.currentTimeMillis())
        val timeZoneDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))

        val contactMobiles = engNumToBangNum("01915150908\n01913260001,01831301012")

        val totalTicketAmountBn = engNumToBangNum("" + totalTicketAmount)

        if(!fromCounterID.equals("") && !toCounterID.equals("")){

            printMe?.sendTextToPrinter("???????????? ???????????????????????????????????? ?????????\n", 28f, true, true, 1)
            serialNo = sharedPref!!.getInt("SerialNo", 0)
            serialNo = serialNo!! + 1


            val user_id = sharedPref?.getString("user_id", "")

            val edit = sharedPref?.edit()
            edit?.putInt("SerialNo", serialNo!!)
            edit?.apply()

            var serialNoBN = engNumToBangNum("" + serialNo)
            var ticketCountBN = engNumToBangNum("" + ticketCount)
            var spannable : SpannableString

            if(toll_amount!! == 0){
                spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n(???????????? "+ engNumToBangNum(""+perTicketPrice!!) +" ???????????? ??????????????? ???????????????) = ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, // start
                    21, // end
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }else{
                spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n(???????????? "+ engNumToBangNum(""+perTicketPrice!!) +" ???????????? + ????????? "+ engNumToBangNum(""+toll_amount!!) + " ???????????? ??????????????? ???????????????) = ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, // start
                    21, // end
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }

            // todo: for utshob counter need to enable this block of code.
            /*if(counter_group_id!!.equals("7")){
                spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n??????????????? ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, // start
                    21, // end
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }else{
                if(toll_amount!! == 0){
                    spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n(???????????? "+ engNumToBangNum(""+perTicketPrice!!) +" ???????????? ??????????????? ???????????????) = ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0, // start
                        21, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }else{
                    spannable = SpannableString("????????????????????? ????????? ${serialNoBN}\n?????????????????? ${mobileDateTime}\n${fromCounter} ?????? ${toCounter}\n??????????????? ????????????????????? ${ticketCountBN}??????\n(???????????? "+ engNumToBangNum(""+perTicketPrice!!) +" ???????????? + ????????? "+ engNumToBangNum(""+toll_amount!!) + " ???????????? ??????????????? ???????????????) = ${totalTicketAmountBn} ????????????\n\n??????????????????/????????????????????????${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0, // start
                        21, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            }*/


            printMe?.sendTextToPrinter(spannable.toString(), 26f, false, false, 2)
            tv_ticket_count?.setText(engNumToBangNum("" + ticketCount))

            ticketSoldId = ticketSoldId!! + 1;
            addTicketSold(
                ticketSoldId!!,
                counter_group_id!!.toInt(),
                "" + fromCounterID,
                "" + toCounterID,
                totalTicketAmount.toString(),
                ticketCount!!.toInt(),
                System.currentTimeMillis()
            )

            isStudentFareEnabled = false
            chbx_student_fare!!.isChecked = false
            ticketCount = 1
            calculatePrice()
        }
    }

    private fun getSelectedCounterTicketPrice(fromCounterGroupId: Int?, toCounterGroupId: Int?) {
        val database = Firebase.database(ConstantValues.DB_URL)
        val counterWisePriceRef = database.getReference("counter_wise_price")

        val counterGroupListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counterWisePrice = dataSnapshot.getValue<List<CounterWiseTicketPrice>>()
                if(counterWisePrice != null && counterWisePrice.size > 0){
                    counterWisePrice?.forEach {
                        if(it != null){
                            if(!it.price!!.equals("") &&
                                it.from_counter_id!!.toInt() == fromCounterGroupId &&
                                it.to_counter_id!!.toInt() == toCounterGroupId){
                                perTicketPrice = it.price!!.toInt()
                                totalTicketAmount = perTicketPrice!!

                                calculatePrice()
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
        counterWisePriceRef.addValueEventListener(counterGroupListener)
    }

    private fun createWebPrintJob(webView: WebView) {

        // Get a PrintManager instance
        (this@MainActivity.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->

            val jobName = "${getString(R.string.app_name)} Document"

            // Get a print adapter instance
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Create a print job with name and adapter instance
            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            ).also { printJob ->

                // todo: Save the job object for later status checking
//                printJobs += printJob
                Log.d("TAG", printJob.toString())
            }
        }
    }

    private fun calculatePrice(){
        if(isStudentFareEnabled!!){
            totalTicketAmount = (ticketCount!! * perTicketPrice!!)
            var totalTicketAmountFair = (((ticketCount!!.toDouble() * perTicketPrice!!.toDouble())/100) * fair_percentage!!.toDouble()).toInt()
            totalTicketAmount = totalTicketAmount!! - totalTicketAmountFair
        }else{
            totalTicketAmount = ticketCount!! * perTicketPrice!!
        }
        tv_ticket_count?.setText(engNumToBangNum("" + ticketCount))
        tv_total_vara?.setText(engNumToBangNum("" + totalTicketAmount) + " ????????????")
//        tv_total_amount?.setText("????????????????????? ???????????? " + totalTicketAmount + " ????????????")
//        tv_total_ticket?.setText("????????????????????? ??????????????? ????????????????????? " + ticketCount)
    }

    private fun initViews() {
        tv_username = findViewById(R.id.tv_username);
        tv_phonenumber = findViewById(R.id.tv_phonenumber);

        tv_ticket_plus = findViewById(R.id.tv_ticket_plus)
        tv_ticket_count = findViewById(R.id.tv_ticket_count)
        tv_minus = findViewById(R.id.tv_minus)
        tv_total_ticket = findViewById(R.id.tv_total_ticket)
        tv_total_amount = findViewById(R.id.tv_total_amount)

        ll_print = findViewById(R.id.ll_print)
        btn_daily_report = findViewById(R.id.btn_daily_report)

        tv_counter_from = findViewById(R.id.tv_counter_from)
        tv_total_vara = findViewById(R.id.tv_total_vara)

        spinner_to = findViewById(R.id.spinner_to)
        rcvCounterGroup = findViewById(R.id.rcv_counter_groups)
        chbx_student_fare = findViewById(R.id.chbx_student_fare)
        ll_student_fare = findViewById(R.id.ll_student_fare)
    }

    fun addTicketSold(
        id: Int,
        counter_group_id: Int,
        from_counter_id: String,
        to_counter_id: String,
        price_total: String,
        total_tickets: Int,
        date_time: Long
    ) {
        val ticketSold = TicketSold(
            id,
            counter_group_id!!.toInt(),
            from_counter_id,
            to_counter_id,
            totalTicketAmount.toString(),
            ticketCount,
            System.currentTimeMillis(),
            from_counter_id
        )

        // Write a message to the database
        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldReference = database.getReference("ticket_sold")
        ticketSoldReference.keepSynced(true)
        val ticketSoldCounterRef = ticketSoldReference.child(from_counter_id)
        ticketSoldCounterRef.keepSynced(true)

        ticketSoldCounterRef.child(createTransactionID()!!).setValue(ticketSold)
            .addOnSuccessListener {
//                Toast.makeText(applicationContext, "Operation Successful. ", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Operation Failed. ", Toast.LENGTH_LONG).show()
            }
    }

    @Throws(Exception::class)
    fun createTransactionID(): String? {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sync_data -> {
                Toast.makeText(applicationContext, "sync data to server.", Toast.LENGTH_LONG).show()
                true
            }
            R.id.daily_report -> {
                if(isInternetOn(applicationContext)){
                    startActivity(Intent(applicationContext, DailyReportActivity::class.java))
                }else{
                    Toast.makeText(applicationContext, "??????????????????????????? ????????????????????? ???????????? ?????????????????? ?????????????????? ???????????? ???", Toast.LENGTH_LONG).show()
                }
                true
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

    fun engNumToBangNum(i: String): String? {
        val valueOf = i
        var str = ""
        for (i2 in 0 until valueOf.length) {
            str =
                if (valueOf[i2] == '1') str + "???" else if (valueOf[i2] == '2') str + "???" else if (valueOf[i2] == '3') str + "???" else if (valueOf[i2] == '4') str + "???" else if (valueOf[i2] == '5') str + "???" else if (valueOf[i2] == '6') str + "???" else if (valueOf[i2] == '7') str + "???" else if (valueOf[i2] == '8') str + "???" else if (valueOf[i2] == '9') str + "???" else if (valueOf[i2] == '0') str + "???" else str + valueOf[i2]
        }
        return str
    }

    fun isInternetOn(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}