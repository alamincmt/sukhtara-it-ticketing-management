package com.sukhtaraitint.ticketing_system

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

var btn_verify : Button? = null;

class VerifyOTPActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_varifi_o_t_p)

        initViews();
        initListeners();
    }

    private fun initViews() {
        btn_verify = findViewById(R.id.btn_verify)
    }

    private fun initListeners() {
        btn_verify?.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}