package com.sushant.quickbills

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //TODO("Remember to change this function of login button")
        submitBtn.setOnClickListener {
            val myIntent = Intent(
                this,
                Dashboard::class.java
            )
            startActivity(myIntent)
        }
    }
}