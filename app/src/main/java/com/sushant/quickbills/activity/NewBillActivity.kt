package com.sushant.quickbills.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.*
import com.sushant.quickbills.model.Bill
import com.sushant.quickbills.model.Customer
import com.sushant.quickbills.model.Item
import com.sushant.quickbills.utils.isNetworkAvailable
import kotlinx.android.synthetic.main.activity_new_bill.*
import kotlinx.android.synthetic.main.pop_up_delete.view.*
import kotlinx.android.synthetic.main.pop_up_exit_confirm.view.*
import kotlinx.android.synthetic.main.pop_up_new_particular.view.*


@Suppress("LABEL_NAME_CLASH")
class NewBillActivity : AppCompatActivity(), RecyclerParticularsAdapter.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var database: DatabaseReference
    private val itemList = arrayListOf<Item>()
    private val count = mutableListOf<Int>()
    private var autoCompleteParticularNameAdapter: AutoCompleteParticularNameAdapter? = null
    private lateinit var autoCompleteParticularName: AutoCompleteTextView
    private lateinit var autoCompleteParticularQty: AutoCompleteTextView
    private lateinit var currCustomer: Customer
    private lateinit var currCustomerId: String
    private lateinit var recyclerParticularsAdapter: RecyclerParticularsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val particularList = arrayListOf<Bill.ParticularItem>()
    private var selectedItem: Item? = null
    private var selectedQty: Double? = null
    private var totalAmount: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_bill)

        //Initialise the variables:-----------------------------------------------------------------
        //Dialog Builder
        val view = layoutInflater.inflate(R.layout.pop_up_new_particular, null, false)
        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder.create()
        autoCompleteParticularName = view.choose_new_particular_name_pop_up
        autoCompleteParticularQty = view.choose_new_particular_qty_pop_up

        //Important
        database = Firebase.database.reference
        auth = Firebase.auth
        currCustomer = Customer(
            intent.extras!!.getString("currCustomerMobile").toString(),
            intent.extras!!.getString("currCustomerName").toString(),
            intent.extras!!.getString("currCustomerAddress").toString()
        )
        currCustomerId = intent.extras!!.getString("currCustomerId").toString()
        for (i in 1..100)   //For suggestions in quantity
            count.add(i)

        //Setting Up Adapters
        //for Recycler View
        recyclerParticularsAdapter = RecyclerParticularsAdapter(this, particularList, this)
        layoutManager = LinearLayoutManager(this)
        particularsRecyclerView.layoutManager = layoutManager
        particularsRecyclerView.adapter = recyclerParticularsAdapter
        recyclerParticularsAdapter.notifyDataSetChanged()
        //for Quantity
        val autoCompleteParticularQtyAdapter =
            ArrayAdapter(
                this,
                R.layout.row_auto_complete,
                R.id.row_auto_complete_text_id,
                count
            )
        autoCompleteParticularQty.setAdapter(autoCompleteParticularQtyAdapter)
        //for autoCompleteParticularName
        //Setting Empty for now.. will be initialized once the data is available
        autoCompleteParticularNameAdapter =
            AutoCompleteParticularNameAdapter(this, arrayListOf())
        autoCompleteParticularName.setAdapter(autoCompleteParticularNameAdapter)
        //------------------------------------------------------------------------------------------

        //Setting up the view:
        customer_name.text = currCustomer.name
        customer_mobile_view_id.text = currCustomer.number
        customer_address_text_view_id.text = currCustomer.address

        //Set up click listeners
        addNewParticularBtn.setOnClickListener {
            val itemRate = view.display_new_particular_rate_pop_up
            val itemQty = view.choose_new_particular_qty_pop_up
            val itemAmount = view.display_new_particular_amount_pop_up
            val selectBtn = view.add_new_particular_pop_up_button

            //Reset the current data
            selectedItem = null
            selectedQty = null

            //When user clicks any quantity suggestions, autofill the form and update the UI
            autoCompleteParticularQty.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    return
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    selectedQty =
                        if (itemQty.text.isEmpty()) 0.0 else itemQty.text.toString().toDouble()
                    //If the user already entered the item calculate the amount and proceed
                    if (selectedItem != null && selectedQty!! >= 0) {
                        val amount = selectedQty!! * selectedItem!!.price!!
                        val amountText = "\u20B9 $amount"
                        itemAmount.text = amountText
                        //Making sure user atleast fills the information once
                        selectBtn.isEnabled = true
                    }
                    return
                }

                override fun afterTextChanged(s: Editable?) {
                    return
                }

            })

            //When user clicks any item suggestions, autofill the form and disable the input
            autoCompleteParticularName.setOnItemClickListener { _, _, position, _ ->
                if (autoCompleteParticularNameAdapter != null)
                    selectedItem = autoCompleteParticularNameAdapter?.getItem(position)!!
                if (this.selectedItem != null) {
                    autoCompleteParticularName.isEnabled = false
                    val rate = "\u20B9 ${selectedItem!!.price}"
                    autoCompleteParticularName.setText(selectedItem!!.name)
                    itemRate.text = rate
                }
                //If the user already entered the quantity calculate the amount and proceed
                if (selectedQty != null && this.selectedItem != null && selectedQty!! >= 0) {
                    val amount = selectedItem!!.price?.times(selectedQty!!)
                    val amountText = "\u20B9 ${amount.toString()}"
                    itemAmount.text = amountText
                    selectBtn.isEnabled = true
                }
            }


            //When user clicks proceed, then add to the current particular list
            selectBtn.setOnClickListener {
                if (selectedItem == null) {
                    autoCompleteParticularName.error = "Please choose an item!!"
                    autoCompleteParticularName.requestFocus()
                    return@setOnClickListener
                }
                if (selectedQty == null || selectedQty!! <= 0) {
                    autoCompleteParticularQty.error = "Please choose a valid quantity!!"
                    autoCompleteParticularQty.requestFocus()
                    return@setOnClickListener
                }
                //If everything is Ok, then add the particular and notify the recycler adapter that data is changed
                val amount = selectedItem!!.price!! * selectedQty!!
                val newParticularItem = Bill.ParticularItem(
                    selectedItem!!.name!!,
                    selectedItem!!.price!!,
                    selectedQty!!,
                    amount
                )
                particularList.add(newParticularItem)
                totalAmount += amount
                bill_amount.text = getString(R.string.rupee, totalAmount)
                recyclerParticularsAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }

            //Before showing the dialog, resetting it:-
            autoCompleteParticularQty.text = null
            autoCompleteParticularName.text = null
            itemRate.text = null
            itemQty.text = null
            itemAmount.text = null
            autoCompleteParticularName.isEnabled = true
            autoCompleteParticularName.requestFocus()
            dialog.show()
        }
    }

    override fun onStart() {
        //Get all the data from the server on start
        database.child(ITEMS_FIELD).child(auth.currentUser!!.uid).orderByChild(
            ITEMS_NAME_FIELD
        ).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                itemList.clear()
                val data = task.result!!.value as HashMap<*, *>
                for (item in data.values) {
                    val currItem = item as HashMap<*, *>
                    val newItem = Item(
                        currItem[ITEMS_NAME_FIELD] as String,
                        currItem[ITEMS_PRICE_FIELD].toString().toDouble(),
                        currItem[SEARCH_KEY].toString()
                    )
                    itemList.add(newItem)
                }
                autoCompleteParticularNameAdapter!!.updateData(itemList)
            }
        }
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.new_bill_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //For saving the bill:
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save_bill) {
            val newBill = Bill(
                currCustomer,
                currCustomerId,
                -System.currentTimeMillis(), //Negation as I want data to be sorted in reverse
                particularList,
                totalAmount
            )
            database.child(BILLS_FIELD).child(auth.currentUser!!.uid).push()
                .setValue(newBill).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Bill created successfully...", Toast.LENGTH_LONG)
                            .show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failure: Bill Creation", Toast.LENGTH_LONG).show()
                    }
                }
            if (!isNetworkAvailable(this)) {
                Toast.makeText(this, "Network not available...", Toast.LENGTH_SHORT)
                    .show()
                Toast.makeText(
                    this,
                    "Bill created locally! Sync when available",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //Checking once for confirmation before exit so that user doesn't accidentally lose the data:
    override fun onBackPressed() {
        val view = layoutInflater.inflate(R.layout.pop_up_exit_confirm, null, false)
        val exitDialog = AlertDialog.Builder(this).setView(view).create()
        val exitBtn = view.exit_pop_up_btn_id
        exitDialog.show()
        exitBtn.setOnClickListener {
            exitDialog.dismiss()
            finish()
        }
    }

    override fun deleteParticular(position: Int) {
        val view = layoutInflater.inflate(R.layout.pop_up_delete, null, false)
        val deleteDialog = AlertDialog.Builder(this).setView(view).create()
        val deleteBtn = view.proceed_delete_pop_up_btn_id
        val cancelBtn = view.cancel_delete_pop_up_btn_id
        deleteBtn.setOnClickListener {
            val particularAmountToBeDeleted = particularList[position].itemAmount
            if (particularAmountToBeDeleted != null) {
                totalAmount -= particularAmountToBeDeleted
            }
            bill_amount.text = getString(R.string.rupee, totalAmount)
            particularList.removeAt(position)
            recyclerParticularsAdapter.notifyDataSetChanged()
            deleteDialog.dismiss()
        }
        cancelBtn.setOnClickListener {
            deleteDialog.dismiss()
        }
        deleteDialog.show()
    }

}