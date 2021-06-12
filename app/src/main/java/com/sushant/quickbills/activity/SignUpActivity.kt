package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.*
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //Initialising variables
        auth = Firebase.auth
        database = Firebase.database.reference

        //Setting up all the listeners
        sign_up_in_proceed_btn.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        val userName: String = user_name_entered_id.text.toString()
        val userEmail: String = sign_up_email_entered_id.text.toString()
        val userPassword: String = sign_up_password_entered_id.text.toString()
        val userPasswordConfirm: String = sign_up_confirm_password_entered_id.text.toString()

        //If the entered name is empty
        if (userName.trim().isEmpty()) {
            user_name_entered_id.error = "Please enter your name!!"
            user_name_entered_id.requestFocus()
            return
        }

        //If the email entered is not valid
        if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            sign_up_email_entered_id.error = "Please enter a valid email!!"
            sign_up_email_entered_id.requestFocus()
            return
        }

        //If the password doesn't match constraints
        if (userPassword.length < 8) {
            sign_up_password_entered_id.error = "The password length must be atleast 8!!"
            sign_up_password_entered_id.requestFocus()
            return
        }

        //If the passwords do not match
        if (!userPassword.contentEquals(userPasswordConfirm)) {
            sign_up_confirm_password_entered_id.error = "Both the passwords do not match!!"
            sign_up_confirm_password_entered_id.requestFocus()
            return
        }

        //Disable Multiple SignUp requests before async tasks:
        sign_up_in_proceed_btn.isEnabled = false
        sign_up_in_proceed_btn.isClickable = false

        //Creating now actual user and saving to the database
        auth.createUserWithEmailAndPassword(
            userEmail, userPassword
        ).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                //This function inserts user into database and returns true if database insertion
                //is done properly. If not, we have to remove the user
                val currUser = HashMap<String, Any>()
                currUser[USER_NAME_FIELD] = userName
                currUser[USER_EMAIL_ID_FIELD] = userEmail
                insertIntoDataBase(auth.currentUser!!.uid, currUser)
            } else {
                Log.w("Error", "SignUpWithEmail:Failure", task.exception)
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG).show()
                sign_up_in_proceed_btn.isEnabled = true
                sign_up_in_proceed_btn.isClickable = true
            }
        }
    }

    private fun insertIntoDataBase(uid : String, currUser: HashMap<String, Any>) {
        database.child("Users").child(uid).setValue(currUser).addOnCompleteListener(this){
            task ->
            if(task.isSuccessful){
                Toast.makeText(this, "User Created Successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }else{
                Log.w("Error", "SignUpWithEmailDataBaseWrite:Failure", task.exception)
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG).show()
            }
            sign_up_in_proceed_btn.isEnabled = true
            sign_up_in_proceed_btn.isClickable = true
        }
    }
}