package com.sushant.quickbills.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
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
import com.sushant.quickbills.data.RecyclerItemsAdapter
import com.sushant.quickbills.data.SEARCH_KEY
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.android.synthetic.main.pop_up_add_item.view.*
import kotlinx.android.synthetic.main.pop_up_delete.view.*
import kotlinx.android.synthetic.main.pop_up_edit_item.view.*
import java.util.*

class ItemActivity : AppCompatActivity(), RecyclerItemsAdapter.OnClickListener,
    SearchView.OnQueryTextListener {
    private var dialogBuilder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null
    private val database = Firebase.database.reference
    private val auth = Firebase.auth
    private var itemsAdapter: RecyclerItemsAdapter? = null
    private val layoutManager = LinearLayoutManager(this)
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        //Setting Up the Recycler View:
        val currUserId = auth.currentUser!!.uid
        val query: Query =
            Firebase.database.reference.child(ITEMS_FIELD).child(currUserId)
                .orderByChild(ITEMS_NAME_FIELD)
        val options: FirebaseRecyclerOptions<Item> = FirebaseRecyclerOptions.Builder<Item>()
            .setQuery(query, Item::class.java)
            .build()
        itemsAdapter = RecyclerItemsAdapter(this, options, this)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchItem: MenuItem = menu!!.findItem(R.id.search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    //This is to create items:-
    private fun showAddItemPopUp() {
        val view = layoutInflater.inflate(R.layout.pop_up_add_item, null, false)
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
            database.child(ITEMS_FIELD).child(auth.currentUser!!.uid).push()
                .setValue(newItem).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        Toast.makeText(this, "Item Added", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
                }
            itemsAdapter!!.notifyDataSetChanged()
        }

        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

    //This is to edit items:-
    override fun showEditItemPopUp(itemReference: DatabaseReference, currItem: Item) {
        val view = layoutInflater.inflate(R.layout.pop_up_edit_item, null, false)
        val itemName = view.entered_edited_item_name_pop_up
        val itemPrice = view.entered_edited_item_price_pop_up
        val submitBtn = view.edit_item_pop_up_button

        itemName.setText(currItem.name.toString())
        itemPrice.setText(currItem.price.toString())

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

            val updatedItem = Item(
                itemName.text.toString(),
                itemPrice.text.toString().toDouble(),
                itemName.text.toString().replace(" ", "").lowercase()
            )

            dialog!!.dismiss()
            itemReference.setValue(updatedItem).addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show()
            }

        }
        dialogBuilder = AlertDialog.Builder(this).setView(view)
        dialog = dialogBuilder!!.create()
        dialog!!.show()
    }

    //This is to delete items:-
    override fun showDeleteItemPopUp(itemReference: DatabaseReference, currItem: Item) {
        val view = layoutInflater.inflate(R.layout.pop_up_delete, null, false)
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        val searchString = newText!!.replace(" ", "").lowercase()
        timer?.cancel()
        //Wait for some time after user stops typing, then execute the query:
        timer = object : CountDownTimer(1300, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                val newQuery: Query =
                    Firebase.database.reference.child(ITEMS_FIELD).child(auth.currentUser!!.uid)
                        .orderByChild(SEARCH_KEY)
                        .startAt(searchString)
                        .endAt(searchString + "\uf8ff")
                val newOptions: FirebaseRecyclerOptions<Item> =
                    FirebaseRecyclerOptions.Builder<Item>()
                        .setQuery(newQuery, Item::class.java)
                        .build()
                itemsAdapter!!.updateOptions(newOptions)
                itemsAdapter!!.notifyDataSetChanged()
            }
        }.start()
        return true
    }


}
