package com.sushant.quickbills.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.sushant.quickbills.R
import com.sushant.quickbills.data.CustomerListAdapter
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.activity_customer.*
import kotlinx.android.synthetic.main.customer_row.*
import java.util.zip.Inflater

class CustomerActivity : AppCompatActivity() {
    val layoutManager = LinearLayoutManager(this)
    var customerListAdapter: CustomerListAdapter? = null
    val customerList: ArrayList<Customer> = arrayListOf()
    private  var dialogBuilder : AlertDialog.Builder ?= null
    private  var dialog : AlertDialog ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        //This is to set the recycler view
        customerListAdapter = CustomerListAdapter(customerList, this)
        customer_list_recycler_view_id.adapter = customerListAdapter
        customer_list_recycler_view_id.layoutManager = layoutManager
        customerListAdapter?.notifyDataSetChanged()

        //This is to add_customer_pop_up
        new_customer_card_id.setOnClickListener{
            showAddCustomerPopUp()
        }

        //TODO("Remember to fetch data from the database. Here customerList is populated for testing purposes only")
        val customer1 = Customer(9430591471, "Sanjay Prasad", "Madhubani")
        for(i in 1..100)
            customerList.add(customer1)

    }

    //This is to add search bar to our customer_activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customer_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //This is to show pop-up add_customer_pop_up
    fun showAddCustomerPopUp(){
        val view = layoutInflater.inflate(R.layout.add_customer_pop_up, null, false)
        val customerName = customer_name_id.text
        val customerMobile = customer_mobile_id.text
        val customerAddress = customer_address_id.text
        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

}