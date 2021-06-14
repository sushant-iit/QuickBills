package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
import com.sushant.quickbills.data.*
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.pop_up_choose_customer.view.*


@Suppress("LABEL_NAME_CLASH")
class Dashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userName: String
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private val customerList = arrayListOf<Customer>()
    private var autoCompleteCustomerAdapter : AutoCompleteCustomerAdapter ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Initialising Variables
        auth = Firebase.auth
        database = Firebase.database.reference

        //Setting up dashboard---------------------------

        //User Listener
        val currUserRef = database.child("Users").child(auth.currentUser!!.uid).child(
            USER_NAME_FIELD
        )
        val userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userName = snapshot.getValue<String>().toString()
                user_name_id.text = userName.split(' ')[0]
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Error", "loadPost:onCancelled", error.toException())
            }
        }
        currUserRef.addValueEventListener(userListener)


        //Customer Listener for autocomplete suggestions and fast response
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
                    if(autoCompleteCustomerAdapter!=null)
                        autoCompleteCustomerAdapter!!.notifyDataSetChanged()
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
            val selectBtn = view.choose_customer_pop_up_button

            //Setting up autocomplete suggestions for new bill pop-up
            autoCompleteCustomerAdapter =
                AutoCompleteCustomerAdapter(this, ArrayList(customerList))
            autoCompleteCustomerAdapter!!.notifyDataSetChanged()
            autoCompleteCustomerName.setAdapter(autoCompleteCustomerAdapter)

            //When user clicks any suggestions, autofill the form
            autoCompleteCustomerName.setOnItemClickListener { _, _, position, _ ->
                val selectedCustomer = autoCompleteCustomerAdapter?.getItem(position)
                if (selectedCustomer != null) {
                    autoCompleteCustomerName.setText(selectedCustomer.name)
                    customerNumber.setText(selectedCustomer.number)
                }
            }

            //When customer clicks select, verify that the data is correct and proceed
            selectBtn.setOnClickListener {

                if (autoCompleteCustomerName.text.toString().trim().isEmpty()) {
                    autoCompleteCustomerName.error = "Please enter customer's name!!"
                    autoCompleteCustomerName.requestFocus()
                    return@setOnClickListener
                }

                if (!Patterns.PHONE.matcher(customerNumber.text)
                        .matches() || customerNumber.text.toString().toBigIntegerOrNull() == null
                ) {
                    customerNumber.error = "Please enter customer's mobile no!!"
                    customerNumber.requestFocus()
                    return@setOnClickListener
                }

                //Get current user and check and proceed:-
                Firebase.database.reference.child(CUSTOMERS_FIELD).child(auth.currentUser!!.uid)
                    .orderByChild(
                        CUSTOMER_NUMBER_FIED
                    ).equalTo(customerNumber.text.toString()).get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //If no value is returned, means incorrect entry
                            if (task.result == null || task.result!!.value == null) {
                                customerNumber.error = "No record found!!"
                                return@addOnCompleteListener
                            }


                            val retrievedData = task.result!!.value as HashMap<*, *>
                            val retrievedCustomer =
                                retrievedData.values.elementAt(0) as HashMap<*, *>

                            //Check for incorrect name
                            if (!retrievedCustomer[CUSTOMERS_NAME_FIELD]!!.toString()
                                    .contentEquals(autoCompleteCustomerName.text.toString())
                            ) {
                                autoCompleteCustomerName.error = "Typo in the name!!"
                                return@addOnCompleteListener
                            }
                            //If everything is Ok, then send for new bill activity
                            val intent = Intent(this, NewBillActivity::class.java)
                            intent.putExtra(
                                "currCustomerId",
                                retrievedData.keys.elementAt(0).toString()
                            )
                            intent.putExtra(
                                "currCustomerName",
                                retrievedCustomer[CUSTOMERS_NAME_FIELD].toString()
                            )
                            intent.putExtra(
                                "currCustomerMobile",
                                retrievedCustomer[CUSTOMER_NUMBER_FIED].toString()
                            )
                            intent.putExtra(
                                "currCustomerAddress",
                                retrievedCustomer[CUSTOMER_ADDRESS_FIELD].toString()
                            )
                            startActivity(intent)
                            dialog.dismiss()
                        } else {
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
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