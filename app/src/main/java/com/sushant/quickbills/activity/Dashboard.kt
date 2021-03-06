package com.sushant.quickbills.activity

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
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
    private lateinit var customerDialog: AlertDialog
    private val customerList = arrayListOf<Customer>()
    private var autoCompleteCustomerAdapter: AutoCompleteCustomerAdapter? = null
    private lateinit var autoCompleteCustomerName : AutoCompleteTextView
    private lateinit var mySharedPref : SharedPreferences
    private lateinit var menuItemTheme : MenuItem
    private lateinit var menuItemLogOut: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Initialising Variables
        auth = Firebase.auth
        database = Firebase.database.reference
        autoCompleteCustomerAdapter =
            AutoCompleteCustomerAdapter(this, ArrayList(customerList))
        val view = layoutInflater.inflate(R.layout.pop_up_choose_customer, null, false)
        autoCompleteCustomerName = view.choose_customer_name_pop_up
        customerDialog = AlertDialog.Builder(this).setView(view).create()
        mySharedPref = getSharedPreferences(PREFS_NAME, 0)

        //Set up Adapters:-
        autoCompleteCustomerName.setAdapter(autoCompleteCustomerAdapter)

        //Setting up click listeners
        customer_card_id.setOnClickListener {
            startActivity(Intent(this, CustomerActivity::class.java))
        }
        item_card_id.setOnClickListener {
            startActivity(Intent(this, ItemActivity::class.java))
        }
        all_bills_card_id.setOnClickListener{
            startActivity(Intent(this, AllBillsActivity::class.java))
        }
        my_account_card_id.setOnClickListener {
            startActivity(Intent(this, MyAccountActivity::class.java))
        }
        new_bill_card_id.setOnClickListener {
            val customerNumber = view.choose_customer_mobile_pop_up
            val selectBtn = view.choose_customer_pop_up_button

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
                            customerDialog.dismiss()
                        } else {
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            customerNumber.text = null
            autoCompleteCustomerName.text = null
            customerDialog.show()
        }

    }

    //Fetch the data from server and store it:-
    override fun onStart() {
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
                }
                autoCompleteCustomerAdapter!!.updateData(customerList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Error", "loadCustomerList:onCancelled", error.toException())
            }
        }
        currCustomerRef.addValueEventListener(customerListener)
        super.onStart()
    }

    override fun onResume() {
        userName = auth.currentUser!!.displayName.toString().split(" ")[0]
        user_name_id.text = userName
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        menuItemTheme = menu!!.findItem(R.id.set_theme)
        menuItemLogOut = menu.findItem(R.id.log_out)
        return super.onCreateOptionsMenu(menu)
    }

    //For Logging the current user out:
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.log_out) {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if (item.itemId == R.id.set_theme) {
            val mySharedPreferencesEditor = mySharedPref.edit()
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                mySharedPreferencesEditor.putInt(NIGHT_MODE_ON, 0)
            else
                mySharedPreferencesEditor.putInt(NIGHT_MODE_ON, 1)
            mySharedPreferencesEditor.apply()
            //Disable theme clicking now:-
            menuItemTheme.isEnabled = false
            menuItemLogOut.isEnabled = false    /* To avoid unexpected crashes */
            Toast.makeText(this, "Restart the App to see the changes...", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    //Handling views on screen rotation
    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d("Triggered:", "true")
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            mflow.setMaxElementsWrap(3)
        else
            mflow.setMaxElementsWrap(2)
        super.onConfigurationChanged(newConfig)
    }

}