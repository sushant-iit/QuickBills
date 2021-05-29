package com.sushant.quickbills.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.recyclerview.widget.LinearLayoutManager
import com.sushant.quickbills.R
import com.sushant.quickbills.data.CustomerListAdapter
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.activity_customer.*

class CustomerActivity : AppCompatActivity() {
    val layoutManager = LinearLayoutManager(this)
    var customerListAdapter: CustomerListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)


        //TODO("Remember to fetch data from the database. Here customerList is populated for testing purposes only")
        val customer1 = Customer(9430591471, "Sanjay Prasad", "Madhubani")
        var customerList: ArrayList<Customer> =
            arrayListOf(
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1,
                customer1
            )
        customerListAdapter = CustomerListAdapter(customerList!!, this)
        Log.d("Debug", customerList!!.size.toString())
        customer_list_recycler_view_id.adapter = customerListAdapter
        customer_list_recycler_view_id.layoutManager = layoutManager
        customerListAdapter?.notifyDataSetChanged()

    }

    //This is to add search bar to our customer_activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customer_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //This function is to set up recycle view
    private fun setRecycleView(customerList: ArrayList<Customer>) {

    }
}