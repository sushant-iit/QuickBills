package com.sushant.quickbills.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_new_bill.*

class NewBillActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_bill)

        customer_name.text = intent.extras!!.getString("currCustomerName")
        customer_mobile_view_id.text = intent.extras!!.getString("currCustomerMobile")
        customer_address_text_view_id.text = intent.extras!!.getString("currCustomerAddress")
    }
}