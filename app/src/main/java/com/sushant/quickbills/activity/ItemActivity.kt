package com.sushant.quickbills.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.ITEMS_FIELD
import com.sushant.quickbills.data.ITEMS_NAME_FIELD
import com.sushant.quickbills.data.ItemsAdapter
import com.sushant.quickbills.data.USER_DATA_FIELD
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.android.synthetic.main.add_item_pop_up.view.*
import kotlinx.android.synthetic.main.delete_item_pop_up.view.*

class ItemActivity : AppCompatActivity(), ItemsAdapter.OnClickListener {
    private var dialogBuilder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null
    private val database = Firebase.database.reference
    private val auth = Firebase.auth
    private var itemsAdapter: ItemsAdapter? = null
    private val layoutManager = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        //Setting Up the Recycler View:
        val currUserId = auth.currentUser!!.uid
        val query: Query =
            Firebase.database.reference.child(USER_DATA_FIELD).child(currUserId).child(
                ITEMS_FIELD
            ).orderByChild(ITEMS_NAME_FIELD)
        val options: FirebaseRecyclerOptions<Item> = FirebaseRecyclerOptions.Builder<Item>()
            .setQuery(query, Item::class.java)
            .build()
        itemsAdapter = ItemsAdapter(this, options, this)
        item_list_recycler_view_id.adapter = itemsAdapter
        item_list_recycler_view_id.layoutManager = layoutManager

        //This is to show add item pop up
        add_item_button_id.setOnClickListener {
            showAddItemPopUp()
        }
    }

    override fun onStart() {
        itemsAdapter!!.startListening()
        super.onStart()
    }

    override fun onStop() {
        itemsAdapter!!.stopListening()
        super.onStop()
    }

    //This is to create items:-
    private fun showAddItemPopUp() {
        val view = layoutInflater.inflate(R.layout.add_item_pop_up, null, false)
        val itemName = view.entered_item_name_pop_up
        val itemPrice = view.entered_item_price_pop_up
        val submitBtn = view.add_item_pop_up_button

        submitBtn.setOnClickListener {

            if (itemName.text.toString().trim().isEmpty()) {
                itemName.error = "Please enter item name!!"
                itemName.requestFocus()
                return@setOnClickListener
            }

            if (itemPrice.text.toString().isEmpty() || itemPrice.text.toString()
                    .toDoubleOrNull() == null
            ) {
                itemPrice.error = "Please enter item price!!"
                itemPrice.requestFocus()
                return@setOnClickListener
            }

            val newItem = Item(
                itemName.text.toString(),
                itemPrice.text.toString().toDouble(),
                itemName.text.toString().replace(" ", "").lowercase()
            )

            dialog!!.dismiss()
            database.child(USER_DATA_FIELD).child(auth.currentUser!!.uid).child(ITEMS_FIELD).push()
                .setValue(newItem).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
                }
//            itemsAdapter.notify()
        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

    override fun showEditCustomerPopUp(itemReference: DatabaseReference, currItem: Item) {

    }

    override fun showDeleteCustomerPopUp(itemReference: DatabaseReference, currItem: Item) {
        val view = layoutInflater.inflate(R.layout.delete_item_pop_up, null, false)
        val cancelDelBtn = view.cancel_delete_pop_up_btn_id
        val proceedDelBtn = view.proceed_delete_pop_up_btn_id

        cancelDelBtn.setOnClickListener {
            dialog!!.dismiss()
        }

        proceedDelBtn.setOnClickListener {
            itemReference.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
            }
            dialog!!.dismiss()
            itemsAdapter!!.notifyDataSetChanged()

        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }


}
