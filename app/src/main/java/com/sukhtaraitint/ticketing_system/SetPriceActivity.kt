package com.sukhtaraitint.ticketing_system

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.CounterGroups
import com.sukhtaraitint.ticketing_system.models.CounterWiseTicketPrice
import com.sukhtaraitint.ticketing_system.models.Counters
import com.sukhtaraitint.ticketing_system.utils.ConstantValues

class SetPriceActivity : AppCompatActivity() {

    private var spinner_from: Spinner? = null
    private var spinner_to: Spinner? = null
    private var et_price: EditText? = null
    private var button_save_price: Button? = null

    var ticket_price : String? = null
    var fromCounterId: String? = null
    var toCounterId: String? = null
    var counterWiseTicketPriceId = 1
    var counterWiseTicketPriceLastId = 1

    var counterWisePriceList : ArrayList<CounterWiseTicketPrice>? = ArrayList<CounterWiseTicketPrice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_price)

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        initViews()
        et_price!!.setSelection(et_price!!.text.length)
        populateCounterGroupList()
        initListeners()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initViews() {
        et_price = findViewById(R.id.et_price)
        button_save_price = findViewById(R.id.button_save_price);
        spinner_from = findViewById(R.id.spinner_from)
        spinner_to = findViewById(R.id.spinner_to)
    }

    fun addOrUpdatePrice(id: String, ticketPrice: Double, fromCounterId: String, toCounterId: String, priceAddedBy: String) {
        val counterWisePrice = CounterWiseTicketPrice(id, fromCounterId, toCounterId, ticket_price+"", priceAddedBy)
        // Write a message to the database
        val database = Firebase.database(ConstantValues.DB_URL)
        val myRef = database.getReference("counter_wise_price")

        myRef.child(id).setValue(counterWisePrice)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Price Updated Successfully. ", Toast.LENGTH_LONG).show()
//                et_price!!.setText("")
//                spinner_from!!.setSelection(0)
//                spinner_to!!.setSelection(0)
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Failed to save data. ", Toast.LENGTH_LONG).show()
            }
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
                    if (spinner_from != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterGroupList!!.toArray()
                        )
                        spinner_from?.adapter = adapter

                        spinner_from?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedPosForCounterGroup = position;
                                fromCounterId = ""+counterGroupObjList!!.get(position).id
                                /*Toast.makeText(this@MainActivity,
                                    counterGroupList!![position] + " selected.", Toast.LENGTH_SHORT).show()*/
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                                // write code to perform some action
                            }
                        }
                    }

                    if (spinner_to != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterGroupList!!.toArray()
                        )
                        spinner_to?.adapter = adapter

                        spinner_to?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedPosForCounterGroup = position;
                                toCounterId = ""+counterGroupObjList!!.get(position).id

                                if(fromCounterId != null && !fromCounterId.equals("") &&
                                    toCounterId != null && !toCounterId.equals("")){
                                        getCounterWiseTicketPrice()
                                    }
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

    private fun getCounterWiseTicketPrice() {
        val database = Firebase.database(ConstantValues.DB_URL)
        val counterWisePriceRef = database.getReference("counter_wise_price")

        val counterGroupListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val counterWisePrice = dataSnapshot.getValue<List<CounterWiseTicketPrice>>()

                counterWisePriceList!!.clear()
                counterWiseTicketPriceId = 0
                counterWisePrice?.forEach {
                    if(it != null){
                        if(!it.price!!.equals("")){
                            counterWisePriceList?.add(it)
                            counterWiseTicketPriceId = it.id!!.toInt()
                        }

                        counterWiseTicketPriceLastId = it.id!!.toInt()
                    }
                }

                updatePrice()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        counterWisePriceRef.addValueEventListener(counterGroupListener)
    }

    private fun updatePrice() {
        if(counterWisePriceList?.size!! > 0){
            if(fromCounterId != null && !fromCounterId.equals("") &&
                toCounterId != null && !toCounterId.equals("")){
                counterWisePriceList!!.forEach {
                    if(it.from_counter_id.equals(fromCounterId) && it.to_counter_id.equals(toCounterId)){
                        et_price!!.setText(it.price + "")
                    }
                }
            }
        }
    }

    private fun initListeners() {
        button_save_price?.setOnClickListener {
            ticket_price = et_price?.text.toString().trim()

            if(ticket_price != null && !ticket_price.equals("") &&
                fromCounterId != null && !fromCounterId.equals("") &&
                toCounterId != null && !toCounterId.equals("")){
                val user_id = sharedPref?.getString("user_id", "")

                if(counterWiseTicketPriceId != 0){
                    addOrUpdatePrice(counterWiseTicketPriceId.toString(), ticket_price!!.toDouble(), fromCounterId!!, toCounterId!!, user_id!!)
                }else{
                    counterWiseTicketPriceId += counterWiseTicketPriceId + 1
                    addOrUpdatePrice(counterWiseTicketPriceLastId.toString(), ticket_price!!.toDouble(), fromCounterId!!, toCounterId!!, user_id!!)
                }
            }else{
                Toast.makeText(applicationContext, "সঠিক তথ্য দিয়ে পুনরায় চেষ্টা করুন", Toast.LENGTH_LONG).show();
            }
        }
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}