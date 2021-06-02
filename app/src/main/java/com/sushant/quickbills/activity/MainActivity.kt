package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO("Remember to change this function of login and signUp button")
        log_in_btn_id.setOnClickListener {
            val myIntent = Intent(
                this,
                LoginActivity::class.java
            )
            startActivity(myIntent)
        }
        sign_up_btn_id.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}