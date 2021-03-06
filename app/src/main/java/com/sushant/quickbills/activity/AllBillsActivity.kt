package com.sushant.quickbills.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.*
import com.sushant.quickbills.model.Bill
import com.sushant.quickbills.model.DateItem
import com.sushant.quickbills.model.ListItem
import com.sushant.quickbills.utils.ConverterTime
import com.sushant.quickbills.utils.createOrShowBillPDF
import kotlinx.android.synthetic.main.activity_all_bills.*
import kotlinx.android.synthetic.main.pop_up_delete.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

class AllBillsActivity : AppCompatActivity(), RecyclerBillsAdapter.OnClickListener {
    private val database = Firebase.database.reference
    private val auth = Firebase.auth
    private val billsAtDate: LinkedHashMap<String, ArrayList<Bill>> = LinkedHashMap()
    private val consolidatedList = arrayListOf<ListItem>()
    private val layoutManager = LinearLayoutManager(this)
    private val recyclerBillsAdapter = RecyclerBillsAdapter(this, consolidatedList, this)
    private val datePickerBuilder = MaterialDatePicker.Builder.dateRangePicker()
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_bills)

        //setting up recycler view
        all_bills_recycler_view.layoutManager = layoutManager
        all_bills_recycler_view.adapter = recyclerBillsAdapter
        recyclerBillsAdapter.notifyDataSetChanged()
    }

    //Retrieve the data from the database and store it in the hashmap according to the parameter
    override fun onStart() {
        super.onStart()
        updateAllBillsData(0, 0)
    }

    private fun updateAllBillsData(startDate: Long, endDate: Long) {
        var currBillsQuery = database.child(BILLS_FIELD).child(auth.currentUser!!.uid).orderByChild(
            BILLS_TIME_FIELD
        )
        currBillsQuery = if (startDate != 0L && endDate != 0L) {
            currBillsQuery.startAt(-endDate.toDouble()).endAt(-startDate.toDouble())
        } else
            currBillsQuery
        val billListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                billsAtDate.clear()
                for (child in snapshot.children) {
                    val currBill = child.getValue(Bill::class.java)
                    currBill!!.key = child.key
                    //convert time to date format
                    //Negative sign as curr time is store as negative long for decreasing order
                    val dateObj = Date(-currBill.purchasedAt!!.toLong())
                    val dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateObj)
                    //Add data to the hashmap
                    if (billsAtDate.containsKey(dateString))
                        billsAtDate[dateString]!!.add(currBill)
                    else {
                        val newArrayList = arrayListOf(currBill)
                        billsAtDate[dateString] = newArrayList
                    }
                }
                updateConsolidatedList()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        currBillsQuery.addValueEventListener(billListener)
    }

    //Consolidated list is passed to recycler view for rendering
    fun updateConsolidatedList() {
        consolidatedList.clear()
        for (date in billsAtDate.keys) {
            val dateItem = DateItem(date)
            consolidatedList.add(dateItem)
            for (bills in billsAtDate[date]!!)
                consolidatedList.add(bills)
        }
        recyclerBillsAdapter.notifyDataSetChanged()
    }

    override fun printPDF(bill: Bill) {
        allBillsProgressIndicator.visibility = View.VISIBLE
        GlobalScope.launch {
            createOrShowBillPDF(this@AllBillsActivity, bill).join()
            runOnUiThread { allBillsProgressIndicator.visibility = View.GONE }
        }
    }

    override fun deleteBill(key: String) {
        val view = layoutInflater.inflate(R.layout.pop_up_delete, null, false)
        val cancelDelBtn = view.cancel_delete_pop_up_btn_id
        val proceedDelBtn = view.proceed_delete_pop_up_btn_id

        cancelDelBtn.setOnClickListener {
            dialog.dismiss()
        }

        proceedDelBtn.setOnClickListener {
            dialog.dismiss()
            database.child(BILLS_FIELD).child(auth.currentUser!!.uid).child(key).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                    } else
                        Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                }
        }

        dialog = AlertDialog.Builder(this).setView(view).create()
        dialog.show()
    }

    //For displaying date filter
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.allbills_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //For showing date picker and filtering based on data
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.date_selector) {
            val materialDatePicker = datePickerBuilder.build()
            materialDatePicker.show(supportFragmentManager, "Choose date range...")

            materialDatePicker.addOnPositiveButtonClickListener { datePicked ->
                //Adding offset in endTime (as I want to select bills inclusively in [start Date, end Date]
                //Adding 24hrs.toMillis() - 1L (a milli second less than 24 hrs like 23:59:59
                updateAllBillsData(
                    ConverterTime(datePicked.first).getTimeInGMT(),
                    ConverterTime(datePicked.second).getTimeInGMT() + 86400000L /* 24hrs */ - 1L /* 1ms */
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
