package com.sushant.quickbills.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.USERS_FIELD
import com.sushant.quickbills.model.User
import kotlinx.android.synthetic.main.activity_my_account.*

class MyAccountActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference
    private lateinit var brandNameField : TextView
    private lateinit var brandNumberField : TextView
    private lateinit var brandAddressField : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        //Doing initialisations
        brandNameField = user_brand_name_account
        brandNumberField = user_brand_number
        brandAddressField = user_brand_address

        //Set up basic info
        user_email.text = auth.currentUser!!.email
        user_name_account.text = auth.currentUser!!.displayName

        //Setting up click listeners
    }

    //Get the user brand info
    override fun onStart() {
        val currUserQuery = database.child(USERS_FIELD).child(auth.currentUser!!.uid)
        val currUserListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val currUser : User
                if(snapshot.value==null){
                    currUser = User()
                    currUserQuery.setValue(currUser)
                }else{
                    currUser = snapshot.getValue(User::class.java)!!
                }
                brandNameField.text = currUser.brandName
                brandAddressField.text = currUser.brandAddress
                brandNumberField.text = currUser.brandNumber
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("AccountActivityDbError", error.details)
            }

        }
        currUserQuery.addValueEventListener(currUserListener)
        super.onStart()
    }
}