package com.sushant.quickbills.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.activity_all_bills.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

class AllBillsActivity : AppCompatActivity() {
    private val database = Firebase.database.reference
    private val auth = Firebase.auth
    private val billsAtDate : LinkedHashMap<String, ArrayList<Bill>> = LinkedHashMap()
    private val consolidatedList = arrayListOf<ListItem>()
    private val layoutManager = LinearLayoutManager(this)
    private val recyclerBillsAdapter = RecyclerBillsAdapter(this, consolidatedList)



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
        val currBillsRef = database.child(BILLS_FIELD).child(auth.currentUser!!.uid).orderByChild(
            BILLS_TIME_FIELD)
        super.onStart()
        val billListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                billsAtDate.clear()
                for(child in snapshot.children){
                    val currBill = child.getValue(Bill::class.java)
                    //convert time to date format
                    //Negative sign as curr time is store as negative long for decreasing order
                    val dateObj = Date(-currBill!!.purchasedAt!!.toLong())
                    val dateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateObj)
                    //Add data to the hashmap
                    if(billsAtDate.containsKey(dateString))
                        billsAtDate[dateString]!!.add(currBill)
                    else{
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
        currBillsRef.addValueEventListener(billListener)
    }

    //Consolidated list is passed to recycler view for rendering
    fun updateConsolidatedList(){
        consolidatedList.clear()
        for(date in billsAtDate.keys){
            val dateItem = DateItem(date)
            consolidatedList.add(dateItem)
            for(bills in billsAtDate[date]!!)
                consolidatedList.add(bills)
        }
        Log.d("keys",billsAtDate.keys.toString())
        recyclerBillsAdapter.notifyDataSetChanged()
    }
}
