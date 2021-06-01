package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //TODO("Implement Login Authentication")
        log_in_proceed_btn.setOnClickListener { startActivity(Intent(this, Dashboard::class.java)) }
    }
}