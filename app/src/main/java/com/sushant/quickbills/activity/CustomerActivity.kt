package com.sushant.quickbills.activity

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.sushant.quickbills.R
import com.sushant.quickbills.data.CustomerListAdapter
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.activity_customer.*
import kotlinx.android.synthetic.main.add_customer_pop_up.view.*

class CustomerActivity : AppCompatActivity(), CustomerListAdapter.customerAdapterClickListener {
    private val layoutManager = LinearLayoutManager(this)
    private var customerListAdapter: CustomerListAdapter? = null
    private val customerList: ArrayList<Customer> = arrayListOf()
    private var dialogBuilder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        //This is to set the recycler view
        customerListAdapter = CustomerListAdapter(customerList, this, this)
        customer_list_recycler_view_id.adapter = customerListAdapter
        customer_list_recycler_view_id.layoutManager = layoutManager
        customerListAdapter?.notifyDataSetChanged()

        //This is to add_customer_pop_up
        new_customer_card_id.setOnClickListener {
            showAddCustomerPopUp()
        }

        //TODO("Remember to fetch data from the database. Here customerList is populated for testing purposes only")
        val customer1 = Customer(9430591471, "Sanjay Prasad", "Madhubani")
        for (i in 1..5)
            customerList.add(customer1)

    }

    //This is to add search bar to our customer_activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.customer_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //This is to show pop-up add_customer_pop_up
    private fun showAddCustomerPopUp() {
        val view = layoutInflater.inflate(R.layout.add_customer_pop_up, null, false)
        val customerName = view.entered_customer_name_pop_up
        val customerMobile = view.entered_customer_mobile_pop_up
        val customerAddress = view.entered_customer_address_pop_u
        val submitBtn = view.add_customer_pop_up_button

        submitBtn.setOnClickListener {
            val newCustomer = Customer(
                customerMobile.text.toString().toLong(),
                customerName.text.toString(),
                customerAddress.text.toString()
            )
            //TODO("Call the database function to add a new customer, temporary method is created now")
            dialog!!.dismiss()
            customerList.add(newCustomer)
            customerListAdapter!!.notifyDataSetChanged()
        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

    override fun showEditCustomerPopUp(position: Int) {
        Toast.makeText(this, customerList[position].Name, Toast.LENGTH_LONG).show()
//        TODO("Not yet implemented")
    }

    override fun showDeleteCustomerPopUp(position: Int) {
        Toast.makeText(this, "Deletion", Toast.LENGTH_LONG).show()
//        TODO("Not yet implemented")
    }

}