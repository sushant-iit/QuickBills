package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.sushant.quickbills.data.USER_NAME_FIELD
import kotlinx.android.synthetic.main.activity_dashboard.*

class Dashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Initialising Variables
        auth = Firebase.auth
        database = Firebase.database.reference

        //Setting up dashboard
        val currUserRef = database.child("Users").child(auth.currentUser!!.uid).child(
            USER_NAME_FIELD
        )
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userName = snapshot.getValue<String>().toString()
                user_name_id.text = userName
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Error", "loadPost:onCancelled", error.toException())
            }
        }
        currUserRef.addValueEventListener(listener)

        //Setting up listeners
        customer_card_id.setOnClickListener {
            startActivity(Intent(this, CustomerActivity::class.java))
        }
        item_card_id.setOnClickListener {
            startActivity(Intent(this, ItemActivity::class.java))
        }
        new_bill_card_id.setOnClickListener {
            startActivity(Intent(this, NewBillActivity::class.java))
        }
    }
    //TODO("Link is only added for temporary functionality")

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