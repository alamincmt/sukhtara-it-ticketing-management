package com.sukhtaraitint.ticketing_system

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sukhtaraitint.ticketing_system.models.Admins
import com.sukhtaraitint.ticketing_system.models.Counters
import com.sukhtaraitint.ticketing_system.utils.ConstantValues

var btn_signin : Button? = null;
var et_password : EditText? = null;
var et_username : EditText? = null;
var chkbx_login_as_admin : CheckBox? = null;

var loginAsAdmin : Boolean = false
var userName : String? = null
var password : String? = null
var adminObj : Admins? = null
var counterObj : Counters? = null

private var PRIVATE_MODE = 0
private val PREF_NAME = "ticket-management"
var sharedPref: SharedPreferences? = null;

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        loginAsAdmin = false;

        sharedPref = getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        val user_name = sharedPref?.getString("user_name", "")
        val user_type = sharedPref?.getString("user_type", "")
        if(user_name != null && !user_name.equals("")){
            if(user_type != null && user_type.equals("admin")){
                startActivity(Intent(applicationContext, ReportActivity::class.java))
            }else{
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }

            finish()
        }

        initViews()
        initListeners()

//        addNewCounter(1, "১নং কাউন্টার", "counter1", "counter1@123", "Narayanganj", "+8801738001777")

    }

    fun addNewCounter(id: Int, name: String, username: String, password: String, location : String, phone : String) {
        val counter = Counters(id, 1, name, username, password, location, phone)
        // Write a message to the database
        val database = Firebase.database(ConstantValues.DB_URL)
        val myRef = database.getReference("counters")

        myRef.child(id.toString()).setValue(counter)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Admin Added Successfully. ", Toast.LENGTH_LONG)
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Admin Add Failed. ", Toast.LENGTH_LONG)
            }
    }

    private fun initListeners() {
        btn_signin?.setOnClickListener {
            userName = et_username?.text.toString().trim()
            password = et_password?.text.toString().trim()

            if(userName != null && !userName.equals("") && password != null && !password.equals("")){
                val database = Firebase.database(ConstantValues.DB_URL)
                val edit = sharedPref?.edit()
                if(loginAsAdmin){
                    val adminRef = database.getReference("admin")

                    val adminListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val admins = dataSnapshot.getValue<List<Admins>>()
                            Log.d("TAG", admins?.get(0)?.name + "")

                            admins?.forEach {
                                if(it != null){
                                    if(it.user_name!!.equals(userName) && it.password!!.equals(password)){
                                        adminObj = it
                                    }
                                }
                            }

                            if(adminObj != null){
                                edit?.putString("user_type", "admin");
                                edit?.putString("user_id", ""+ adminObj?.id);
                                edit?.putString("user_name", adminObj?.user_name);
                                edit?.putString("name", adminObj?.name);
                                edit?.putString("phone", adminObj?.phone);
                                edit?.putString("location", adminObj?.location);
                                edit?.apply()

                                startActivity(Intent(applicationContext, ReportActivity::class.java))
                                finish()
                            }else{
                                Toast.makeText(applicationContext, "সঠিক তথ্য দিয়ে পুনরায় চেষ্টা করুন", Toast.LENGTH_LONG).show();
                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                        }
                    }
                    adminRef.addValueEventListener(adminListener)
                }else{
                    val counterRef = database.getReference("counters")

                    val counterListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val counters = dataSnapshot.getValue<List<Counters>>()
                            Log.d("TAG", counters?.get(0)?.name + "")

                            counters?.forEach {
                                if(it != null){
                                    if(it.user_name!!.equals(userName) && it.password!!.equals(password)){
                                        counterObj = it
                                    }
                                }
                            }

                            if(counterObj != null){
                                edit?.putString("user_type", "counter")
                                edit?.putString("group_counter_id", ""+ counterObj?.group_id)
                                edit?.putString("user_id", ""+ counterObj?.id)
                                edit?.putString("user_name", counterObj?.user_name)
                                edit?.putString("name", counterObj?.name)
                                edit?.putString("phone", counterObj?.phone)
                                edit?.putString("location", counterObj?.location)

                                edit?.putLong("TimeMillisForSerialNo", System.currentTimeMillis())
                                edit?.putInt("SerialNo", 0)
                                edit?.apply()
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            }else{
                                Toast.makeText(applicationContext, "সঠিক তথ্য দিয়ে পুনরায় চেষ্টা করুন", Toast.LENGTH_LONG).show();
                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
                        }
                    }
                    counterRef.addValueEventListener(counterListener)
                }
            }else{
                Toast.makeText(applicationContext, "সঠিক তথ্য দিয়ে পুনরায় চেষ্টা করুন", Toast.LENGTH_LONG).show();
            }
        }

        chkbx_login_as_admin?.setOnCheckedChangeListener { compoundButton, b ->
            loginAsAdmin = b
        }
    }

    private fun initViews() {
        et_username = findViewById(R.id.et_username)
        et_password = findViewById(R.id.et_password)
        btn_signin = findViewById(R.id.btn_signin)
        chkbx_login_as_admin = findViewById(R.id.chkbx_login_as_admin)
    }
}