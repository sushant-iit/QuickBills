package com.sushant.quickbills.activity

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.AutoCompleteItemAdapter
import com.sushant.quickbills.data.ITEMS_FIELD
import com.sushant.quickbills.data.ITEMS_NAME_FIELD
import com.sushant.quickbills.model.Customer
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.activity_new_bill.*
import kotlinx.android.synthetic.main.pop_up_new_particular.view.*

class NewBillActivity : AppCompatActivity() {
    private lateinit var currCustomer: Customer
    private lateinit var currCustomerId: String
    private lateinit var dialogBuilder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val count = mutableListOf<Int>()
    private val itemList = arrayListOf<Item>()
    private var autoCompleteParticularNameAdapter: AutoCompleteItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_bill)

        //Initialising the variables:
        currCustomer = Customer(
            intent.extras!!.getString("currCustomerMobile").toString(),
            intent.extras!!.getString("currCustomerName").toString(),
            intent.extras!!.getString("currCustomerAddress").toString()
        )
        database = Firebase.database.reference
        auth = Firebase.auth
        //For autocomplete suggestions for quantity
        for (i in 1..100)
            count.add(i)

        //Setting up the view:
        customer_name.text = currCustomer.name
        customer_mobile_view_id.text = currCustomer.number
        customer_address_text_view_id.text = currCustomer.address

        //Fetching full items for better response and autocomplete view
        val itemRef = database.child(ITEMS_FIELD).child(auth.currentUser!!.uid).orderByChild(
            ITEMS_NAME_FIELD
        )
        val itemListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                for (child in snapshot.children) {
                    val item = child.getValue(Item::class.java)
                    if (item != null)
                        itemList.add(item)
                    if (autoCompleteParticularNameAdapter != null)
                        autoCompleteParticularNameAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Error", "loadItemList:onCancelled")
            }
        }
        itemRef.addValueEventListener(itemListener)

        //Setting up click listeners
        addNewParticularBtn.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.pop_up_new_particular, null, false)
            val autoCompleteParticularQty = view.choose_new_particular_qty_pop_up
            val autoCompleteItemName = view.choose_new_particular_name_pop_up
            val itemRate = view.display_new_particular_rate_pop_up
            val itemQty = view.choose_new_particular_qty_pop_up
            val itemAmount = view.display_new_particular_amount_pop_up
            val selectBtn = view.add_new_particular_pop_up_button

            //Setting up autocomplete adapter for Quantity
            val autoCompleteParticularQtyAdapter =
                ArrayAdapter(
                    this,
                    R.layout.row_auto_complete,
                    R.id.row_auto_complete_text_id,
                    count
                )
            autoCompleteParticularQty.setAdapter(autoCompleteParticularQtyAdapter)

            //Setting up autocomplete adapter for Item Name
            autoCompleteParticularNameAdapter = AutoCompleteItemAdapter(this, ArrayList(itemList))
            autoCompleteParticularNameAdapter!!.notifyDataSetChanged()
            autoCompleteItemName.setAdapter(autoCompleteParticularNameAdapter)

            //When user clicks any suggestions, autofill the form and disable the form
            autoCompleteItemName.setOnItemClickListener { _, _, position, _ ->
                val selectedItem = autoCompleteParticularNameAdapter?.getItem(position)
                if (selectedItem != null) {
                    autoCompleteItemName.isEnabled = false
                    val rate = "\u20B9 ${selectedItem.price}"
                    autoCompleteItemName.setText(selectedItem.name)
                    itemRate.setText(rate)
                }
                //If the user already entered the quantity calculate the amount and proceed
                if(!itemQty.text.isEmpty() && itemQty.text.toString().toLongOrNull()!=null){
                    val qty = itemQty.text.toString().toLong()
                    val amount = selectedItem!!.price?.times(qty)
                    itemAmount.setText(amount.toString())
                }
            }


            //Display the pop-up
            dialogBuilder = AlertDialog.Builder(this).setView(view)
            dialog = dialogBuilder.create()
            dialog.show()
        }
    }
}