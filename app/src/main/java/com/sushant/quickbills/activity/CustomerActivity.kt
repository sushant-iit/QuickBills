package com.sushant.quickbills.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.*
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.activity_customer.*
import kotlinx.android.synthetic.main.pop_up_add_customer.view.*
import kotlinx.android.synthetic.main.pop_up_delete.view.*
import kotlinx.android.synthetic.main.pop_up_edit_customer.view.*


class CustomerActivity : AppCompatActivity(), CustomersAdapter.OnClickListener,
    SearchView.OnQueryTextListener {
    private val layoutManager = LinearLayoutManager(this)
    private var customersAdapter: CustomersAdapter? = null
    private var dialogBuilder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null
    private var auth: FirebaseAuth = Firebase.auth
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)

        //This is to set the recycler view
        val currUserId = auth.currentUser!!.uid
        val query: Query = Firebase.database.reference
            .child(CUSTOMERS_FIELD).child(currUserId).orderByChild(
                CUSTOMERS_NAME_FIELD
            )
        val options: FirebaseRecyclerOptions<Customer> = FirebaseRecyclerOptions.Builder<Customer>()
            .setQuery(query, Customer::class.java)
            .build()
        customersAdapter = CustomersAdapter(this, options, this)
        customer_list_recycler_view_id.adapter = customersAdapter
        customer_list_recycler_view_id.layoutManager = layoutManager

        //This is to add_customer_pop_up
        add_customer_button_id.setOnClickListener {
            showAddCustomerPopUp()
        }

    }

    override fun onStart() {
        customersAdapter!!.startListening()
        super.onStart()
    }

    override fun onStop() {
        customersAdapter!!.startListening()
        super.onStop()
    }

    //This is to add search bar to our customer_activity
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem: MenuItem = menu!!.findItem(R.id.search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    //New Functions
    private fun showAddCustomerPopUp() {
        val view = layoutInflater.inflate(R.layout.pop_up_add_customer, null, false)
        val customerName = view.entered_customer_name_pop_up
        val customerMobile = view.entered_customer_mobile_pop_up
        val customerAddress = view.entered_customer_address_pop_u
        val submitBtn = view.add_customer_pop_up_button

        submitBtn.setOnClickListener {

            if (customerName.text.toString().trim().isEmpty()) {
                customerName.error = "Please enter customer's name!!"
                customerName.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.PHONE.matcher(customerMobile.text).matches()) {
                customerMobile.error = "Please enter customer's mobile no!!"
                customerMobile.requestFocus()
                return@setOnClickListener
            }

            if (customerAddress.text.toString().trim().isEmpty()) {
                customerAddress.error = "Please enter customer's address!!"
                customerAddress.requestFocus()
                return@setOnClickListener
            }

            val newCustomer = Customer(
                customerMobile.text.toString(),
                customerName.text.toString(),
                customerAddress.text.toString()
            )

            dialog!!.dismiss()
            val database = Firebase.database.reference
            database.child(CUSTOMERS_FIELD).child(auth.currentUser!!.uid)
                .push().setValue(newCustomer).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Toast.makeText(this, "Customer Added", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
                }
            customersAdapter!!.notifyDataSetChanged()
        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

    override fun showEditCustomerPopUp(
        customerReference: DatabaseReference,
        currCustomer: Customer
    ) {
        val view = layoutInflater.inflate(R.layout.pop_up_edit_customer, null, false)
        val customerName = view.entered_edit_customer_name_pop_up
        val customerMobile = view.entered_edit_customer_mobile_pop_up
        val customerAddress = view.entered_edit_customer_address_pop_up
        val submitBtn = view.edit_customer_pop_up_button

        customerName.setText(currCustomer.name)
        customerMobile.setText(currCustomer.number)
        customerAddress.setText(currCustomer.address)

        submitBtn.setOnClickListener {

            if (customerName.text.toString().trim().isEmpty()) {
                customerName.error = "Please enter customer's name!!"
                customerName.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.PHONE.matcher(customerMobile.text).matches()) {
                customerMobile.error = "Please enter customer's mobile no!!"
                customerMobile.requestFocus()
                return@setOnClickListener
            }

            if (customerAddress.text.toString().trim().isEmpty()) {
                customerAddress.error = "Please enter customer's address!!"
                customerAddress.requestFocus()
                return@setOnClickListener
            }

            val newCustomer = Customer(
                customerMobile.text.toString(),
                customerName.text.toString(),
                customerAddress.text.toString()
            )

            dialog!!.dismiss()
            customerReference.setValue(newCustomer).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_SHORT).show()
                customersAdapter!!.notifyDataSetChanged()
            }

        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()

    }

    override fun showDeleteCustomerPopUp(
        customerReference: DatabaseReference,
        currCustomer: Customer
    ) {
        val view = layoutInflater.inflate(R.layout.pop_up_delete, null, false)
        val cancelDelBtn = view.cancel_delete_pop_up_btn_id
        val proceedDelBtn = view.proceed_delete_pop_up_btn_id

        cancelDelBtn.setOnClickListener {
            dialog!!.dismiss()
        }

        proceedDelBtn.setOnClickListener {
            customerReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
            dialog!!.dismiss()
            customersAdapter!!.notifyDataSetChanged()

        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    //Searching Functionality Implementation
    override fun onQueryTextChange(newText: String?): Boolean {
        timer?.cancel()
        //Wait for some time after user stops typing
        timer = object : CountDownTimer(1300, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val newQuery: Query = Firebase.database.reference
                    .child(CUSTOMERS_FIELD).child(auth.currentUser!!.uid)
                    .orderByChild(USER_NAME_FIELD).startAt(newText)
                    .endAt(newText + "\uf8ff")
                val newOptions: FirebaseRecyclerOptions<Customer> =
                    FirebaseRecyclerOptions.Builder<Customer>()
                        .setQuery(newQuery, Customer::class.java)
                        .build()
                customersAdapter!!.updateOptions(newOptions)
            }
        }.start()
        return true
    }

}