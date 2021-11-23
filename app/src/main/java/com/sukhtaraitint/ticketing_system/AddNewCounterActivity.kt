package com.sukhtaraitint.ticketing_system

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.CounterGroups
import com.sukhtaraitint.ticketing_system.models.Counters
import com.sukhtaraitint.ticketing_system.utils.ConstantValues
import java.util.ArrayList

class AddNewCounterActivity : AppCompatActivity() {


    var btn_save : Button? = null
    var et_username : EditText? = null
    var et_password : EditText? = null
    var et_counter_name : EditText? = null
    var et_counter_location : EditText? = null
    var et_counter_phone_number : EditText? = null

    var spinner_group : Spinner? = null

    var userName : String? = null
    var password : String? = null
    var counterName : String? = null
    var counterLocation : String? = null
    var counterPhoneNumber : String? = null

    var counterID : Int? = 1

    var selectedGroupID: String? = "1"
    var selectedPosForCounterGroup : Int = 0;
    var counterGroupList : ArrayList<String>? = ArrayList<String>()
    var counterGroupObjList : ArrayList<CounterGroups>? = ArrayList<CounterGroups>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_counter)

        val toolbar = findViewById(R.id.toolbar) as androidx.appcompat.widget.Toolbar?
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            // show back button on toolbar
            // on back button press, it will navigate to parent activity
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val database = Firebase.database(ConstantValues.DB_URL)
        val myRef = database.getReference("counters")
        val query: Query = myRef.orderByKey().limitToLast(1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.children != null) {
                    for (child in dataSnapshot.children) {
                        if (child.key != null) {
                            counterID = child.child("id").value.toString().toInt()
                        } else {
                            counterID = 0
                        }
                    }
                } else {
                    counterID = 0
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // TODO: Handle errors.
                counterID = 0
                Log.d("User key", "Failed")
            }
        })

        initViews()
        populateCounterGroupList()
        initListeners()

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun initViews() {
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        et_counter_name = findViewById(R.id.et_counter_name)
        et_counter_location = findViewById(R.id.et_counter_location)
        et_counter_phone_number = findViewById(R.id.et_counter_phone_number)
        spinner_group = findViewById(R.id.spinner_group)

        btn_save = findViewById(R.id.btn_save)
    }

    fun addNewCounter(id: Int, name: String, username: String, password: String, location : String, phone : String) {
        val counter = Counters(id, selectedGroupID!!.toInt(), name, username, password, location, phone)
        // Write a message to the database
        val database = Firebase.database(ConstantValues.DB_URL)
        val myRef = database.getReference("counters")

        myRef.child(id.toString()).setValue(counter)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Counter Added Successfully. ", Toast.LENGTH_LONG).show()
                et_username!!.setText("")
                et_password!!.setText("")
                et_counter_name!!.setText("")
                et_counter_location!!.setText("")
                et_counter_phone_number!!.setText("")
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
                    if (spinner_group != null) {
                        val adapter = ArrayAdapter(
                            applicationContext,
                            R.layout.counter_items, counterGroupList!!.toArray()
                        )
                        spinner_group?.adapter = adapter

                        spinner_group?.onItemSelectedListener = object :
                            AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View, position: Int, id: Long
                            ) {
                                selectedPosForCounterGroup = position;
                                selectedGroupID = ""+counterGroupObjList!!.get(position).id
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
        btn_save?.setOnClickListener {
            userName = et_username?.text.toString().trim()
            password = et_password?.text.toString().trim()
            counterName = et_counter_name?.text.toString().trim()
            counterLocation = et_counter_location?.text.toString().trim()
            counterPhoneNumber = et_counter_phone_number?.text.toString().trim()

            if(userName != null && !userName.equals("") &&
                password != null && !password.equals("") &&
                counterName != null && !counterName.equals("") &&
                counterLocation != null && !counterLocation.equals("") &&
                counterPhoneNumber != null && !counterPhoneNumber.equals("")){
                    counterID = counterID!! + 1
                addNewCounter(counterID!!, counterName!!, userName!!, password!!, counterLocation!!, counterPhoneNumber!!)
            }else{
                Toast.makeText(applicationContext, "সঠিক তথ্য দিয়ে পুনরায় চেষ্টা করুন", Toast.LENGTH_LONG).show();
            }
        }

        chkbx_login_as_admin?.setOnCheckedChangeListener { compoundButton, b ->
            loginAsAdmin = b
        }
    }
}