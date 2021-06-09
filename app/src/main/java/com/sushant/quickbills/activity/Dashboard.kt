package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.AutoCompleteCustomerAdapter
import com.sushant.quickbills.data.CUSTOMERS_FIELD
import com.sushant.quickbills.data.CUSTOMERS_NAME_FIELD
import com.sushant.quickbills.data.USER_NAME_FIELD
import com.sushant.quickbills.model.Customer
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.pop_up_choose_customer.view.*


class Dashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userName: String
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog

    //This is done for autocomplete suggestions. For efficiency, I will start listening to the changes
    //in the dashboard where more likely is that data will not be changed and when we go to customer
    //and price activity, I will stop listening temporarily when many changes can happen, but on pause,
    //I can again start listening to decrease bandwidth consumption

    private val customerList = arrayListOf<Customer>()
    private val itemList = arrayListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Initialising Variables
        auth = Firebase.auth
        database = Firebase.database.reference

        //Setting up dashboard

        //User Listener
        val currUserRef = database.child("Users").child(auth.currentUser!!.uid).child(
            USER_NAME_FIELD
        )
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userName = snapshot.getValue<String>().toString()
                user_name_id.text = userName
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Error", "loadPost:onCancelled", error.toException())
            }
        }
        currUserRef.addValueEventListener(userListener)

        //Customer And Item Listener for autocomplete suggestions and fast response
        val currCustomerRef =
            database.child(CUSTOMERS_FIELD).child(auth.currentUser!!.uid).orderByChild(
                CUSTOMERS_NAME_FIELD
            )
        val customerListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                customerList.clear()
                for (child in snapshot.children) {
                    val customer = child.getValue(Customer::class.java)
                    if (customer != null)
                        customerList.add(customer)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Error", "loadCustomerList:onCancelled", error.toException())
            }
        }
        currCustomerRef.addValueEventListener(customerListener)


        //Setting up click listeners
        customer_card_id.setOnClickListener {
            startActivity(Intent(this, CustomerActivity::class.java))
        }
        item_card_id.setOnClickListener {
            startActivity(Intent(this, ItemActivity::class.java))
        }
        new_bill_card_id.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.pop_up_choose_customer, null, false)
            val autoCompleteCustomerName = view.choose_customer_name_pop_up
            val customerNumber = view.choose_customer_mobile_pop_up

            //Setting up autocomplete suggestions for new bill pop-up
            val autoCompleteCustomerAdapter =
                AutoCompleteCustomerAdapter(this, ArrayList(customerList))
            autoCompleteCustomerName.setAdapter(autoCompleteCustomerAdapter)
            //When user clicks any suggestions, autofill the form
            autoCompleteCustomerName.setOnItemClickListener { _, _, position, _ ->
                val selectedCustomer = autoCompleteCustomerAdapter.getItem(position)
                if (selectedCustomer != null) {
                    autoCompleteCustomerName.setText(selectedCustomer.name)
                    customerNumber.setText(selectedCustomer.number)
                }
            }

            dialogBuilder = AlertDialog.Builder(this).setView(view)
            dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //For Logging the current user out:
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}