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
import com.sukhtaraitint.ticketing_system.models.Admins
import com.sukhtaraitint.ticketing_system.models.Counters
import com.sukhtaraitint.ticketing_system.models.TicketSold
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var PRIVATE_MODE = 0
    private val PREF_NAME = "ticket-management"
    var sharedPref: SharedPreferences? = null

    var serialNo: Int? = 1
    var ticketCount: Int? = 1
    var totalTicketAmount: Double? = 50.0
    var perTicketPrice: Double? = 50.0
    var ticketCountToday: Int? = 0

    var totalTicketAmountToday: Double? = 0.0
    var fromCounter: String? = ""

    var counter_group_id: String? = "0"

    var toCounter: String? = ""

    var ticketSoldId: Int? = 2
    var toCounterID: String? = ""
    var fromCounterID: String? = ""

    var isNeedToSaveData : Boolean? = false;

    var tv_username : TextView? = null
    var tv_phonenumber : TextView? = null

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
    private var mWebView: WebView? = null
    var printMe : PrintMe? = null
    var ticketSoldObjct : TicketSold? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Firebase.database.setPersistenceEnabled(true)
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
        loadData()
        initListeners()
    }

    /*private fun syncOfflineData() {

        val ticketDao = TicketManagementDatabase.invoke(applicationContext).tickSoldDao()
        val tickSolds: List<TicketSold> = ticketDao.getAll()
        if(isInternetOn(applicationContext)){
            tickSolds.forEach(){
                if(it != null){
                    isNeedToSaveData = true;
                    getTicketSoldID(ticketSoldId!!, it.from_counter_id!!,
                        it.to_counter_id!!,
                        it.price_total!!,
                        it.total_tickets!!,
                        it.date_time!!)

                    ticketSoldObjct = it
                    ticketDao.delete(ticketSoldObjct!!)
                }
            }
        }
    }*/

    private fun initListeners() {

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

        ll_print?.setOnClickListener {
            val randomValues = List(1) { Random.nextInt(0, 10000) }
            var date = Date(System.currentTimeMillis())
            val timeZoneDate = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            var mobileDateTime = engNumToBangNum(timeZoneDate.format(date))

            val randomTicketID = engNumToBangNum("" + randomValues.get(0))
            val contactMobiles = engNumToBangNum("01915150908\n01913260001,01831301012");
            val totalTicketAmountBn = engNumToBangNum("" + totalTicketAmount)

            if(!fromCounterID.equals("") && !toCounterID.equals("")){

                printMe?.sendTextToPrinter("উৎসব ট্রান্সপোর্ট লিঃ\n", 28f, true, true, 1)
                serialNo = sharedPref!!.getInt("SerialNo", 0)
                serialNo = serialNo!! + 1


                val user_id = sharedPref?.getString("user_id", "")

                val edit = sharedPref?.edit()
                edit?.putInt("SerialNo", serialNo!!)
                edit?.apply()

                var serialNoBN = engNumToBangNum("" + serialNo)
                var ticketCountBN = engNumToBangNum("" + ticketCount)
                var spannable : SpannableString
                if(user_id!!.equals("8")){
                    spannable = SpannableString("সিরিয়াল নংঃ ${serialNoBN}\nতারিখঃ ${mobileDateTime}\n${fromCounter} টু ${toCounter}\nটিকেট সংখ্যাঃ ${ticketCountBN}টি\nভাড়াঃ ${totalTicketAmountBn} টাকা\n\nঅভিযোগ/রিজার্ভঃ${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0, // start
                        21, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }else{
                    spannable = SpannableString("সিরিয়াল নংঃ ${serialNoBN}\nতারিখঃ ${mobileDateTime}\n${fromCounter} টু ${toCounter}\nটিকেট সংখ্যাঃ ${ticketCountBN}টি\nভাড়াঃ ${totalTicketAmountBn} টাকা(প্রতি টিকেটে \nটোল ৫ টাকা)\n\nঅভিযোগ/রিজার্ভঃ${contactMobiles}\n\nSoft By: sukhtaraintltd.com\n01714070437\n")
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
//                    "<html><body style=\"background-color: #FFFFFF;\"><h4 style=\"color:black;\">উৎসব ট্রান্সপোর্ট লিঃ</h4><b style=\"color:black;\">সিরিয়াল নংঃ ${randomTicketID}</b><p style=\"color:black;\">তারিখঃ ${mobileDateTime}<br/>ভাড়াঃ ${totalTicketAmountBn} টাকা<br/>হইতেঃ ${fromCounter}<br/>গন্তব্যঃ ${toCounter}<br/>অভিযোগ/রিজার্ভঃ ${contactMobiles}</p></body></html>"
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
            perTicketPrice = 40.0
            totalTicketAmount = 40.0
        }

        tv_ticket_count?.setText(engNumToBangNum("" + ticketCount))
        tv_total_vara?.setText(engNumToBangNum("" + totalTicketAmount) + " টাকা")

        val database = Firebase.database(ConstantValues.DB_URL)
        val counterRef = database.getReference("counters")
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

//        updateTodaysData()
    }

    private fun updateTodaysData(){
        val database = Firebase.database(ConstantValues.DB_URL)
        val ticketSoldRef = database.getReference("ticket_sold")

//        val counterTicketSold = ticketSoldRef.child("from_counter_id").child(fromCounterID!!)


        val ticketSoldListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                /*for (snapshot in dataSnapshot.children) {
                    val totalTicketSold = dataSnapshot.child(snapshot.key!!).value
                    println(totalTicketSold)
                }*/

//                val totalTicketSold = dataSnapshot.getValue<List<TicketSold>>()
//                Log.d("TAG", totalTicketSold?.get(0)?.from_counter_id + "")

                totalTicketAmountToday = 0.0
                ticketCountToday = 0

                for (snapshot in dataSnapshot.children) {
                    val totalTicketSold = snapshot.getValue(TicketSold::class.java)
                    if(totalTicketSold != null){
                        if(!totalTicketSold.from_counter_id!!.equals("") && totalTicketSold.from_counter_id!!.equals(fromCounterID)){
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = totalTicketSold.date_time!!

                            val todaysCalendar = Calendar.getInstance()
                            todaysCalendar.timeInMillis = System.currentTimeMillis()

                            if(todaysCalendar.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)){
                                // get total ticket sold + total ammount
                                totalTicketAmountToday = totalTicketAmountToday!! + totalTicketSold.price_total!!.toDouble()
                                ticketCountToday = ticketCountToday!! + totalTicketSold.total_tickets!!.toInt()

                                tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmountToday) + " টাকা")
                                tv_total_ticket?.setText(
                                    "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                        "" + ticketCountToday
                                    )
                                )
                            }/*else{
                                // get total ticket sold + total ammount
                                totalTicketAmountToday = 0.0
                                ticketCountToday = 0

                                tv_total_amount?.setText("সর্বমোট দামঃ " + engNumToBangNum("" + totalTicketAmountToday) + " টাকা")
                                tv_total_ticket?.setText(
                                    "সর্বমোট টিকেট সংখ্যাঃ " + engNumToBangNum(
                                        "" + ticketCountToday
                                    )
                                )
                            }*/
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        ticketSoldRef.addValueEventListener(ticketSoldListener)
    }

    private fun doWebViewPrint(htmlDocument: String) {
        // Create a WebView object specifically for printing
        val webView = WebView(this@MainActivity)
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

            override fun onPageFinished(view: WebView, url: String) {
                Log.i("TAG", "page finished loading $url")
                createWebPrintJob(view)
                mWebView = null
            }
        }

        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView
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
        totalTicketAmount = ticketCount!! * perTicketPrice!!
        tv_ticket_count?.setText(engNumToBangNum("" + ticketCount))
        tv_total_vara?.setText(engNumToBangNum("" + totalTicketAmount) + " টাকা")
//        tv_total_amount?.setText("সর্বমোট দামঃ " + totalTicketAmount + " টাকা")
//        tv_total_ticket?.setText("সর্বমোট টিকেট সংখ্যাঃ " + ticketCount)
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

        tv_counter_from = findViewById(R.id.tv_counter_from)
        tv_total_vara = findViewById(R.id.tv_total_vara)

        spinner_to = findViewById(R.id.spinner_to)
    }

    fun addNewAdmin(
        id: Int,
        name: String,
        username: String,
        password: String,
        location: String,
        phone: String
    ) {
        val admin = Admins(id, name, username, password, location, phone)
        // Write a message to the database
        val database = Firebase.database(ConstantValues.DB_URL)
        val myRef = database.getReference("admin")
        myRef.keepSynced(true)

        myRef.child(id.toString()).setValue(admin)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Admin Added Successfully. ", Toast.LENGTH_LONG)
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Admin Add Failed. ", Toast.LENGTH_LONG)
            }
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
        val myRef = database.getReference("ticket_sold")
        myRef.keepSynced(true)

        myRef.child(createTransactionID()!!).setValue(ticketSold)
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
                    Toast.makeText(applicationContext, "ইন্টারনেট কানেকশন দিয়ে পুনরায় চেষ্টা করুন ।", Toast.LENGTH_LONG).show()
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
                if (valueOf[i2] == '1') str + "১" else if (valueOf[i2] == '2') str + "২" else if (valueOf[i2] == '3') str + "৩" else if (valueOf[i2] == '4') str + "৪" else if (valueOf[i2] == '5') str + "৫" else if (valueOf[i2] == '6') str + "৬" else if (valueOf[i2] == '7') str + "৭" else if (valueOf[i2] == '8') str + "৮" else if (valueOf[i2] == '9') str + "৯" else if (valueOf[i2] == '0') str + "০" else str + valueOf[i2]
        }
        return str
    }

    fun isInternetOn(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}