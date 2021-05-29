package com.sushant.quickbills.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_dashboard.*

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        new_bill_card_id.setOnClickListener(){
            startActivity(Intent(this, CustomerActivity::class.java))
        }
    }
    //TODO("Link is only added for temporary functionality")

}