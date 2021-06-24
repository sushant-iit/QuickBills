package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Initialisation
        auth = Firebase.auth

        //Setting Up the Listener
        log_in_proceed_btn.setOnClickListener{
            loginUser()
        }
    }

    private fun loginUser(){
        val userEmail : String = login_email_id_entered_id.text.toString()
        val userPassword : String = login_password_entered_id.text.toString()
        val loginBtn = log_in_proceed_btn

        //If the email entered is invalid
        if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            login_email_id_entered_id.error = "Please, enter a valid email!!"
            login_email_id_entered_id.requestFocus()
            return
        }

        //If the password entered is empty
        if(userPassword.isEmpty()){
            login_password_entered_id.error = "Please enter your password!!"
            login_password_entered_id.requestFocus()
            return
        }

        //To prevent multiple requests
        loginBtn.isClickable = false
        loginBtn.isEnabled = false

        //Logging the user in
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener{
            task ->
            if(task.isSuccessful){
                Toast.makeText(this, "Logged-In Successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, Dashboard::class.java))
                finish()
            }else{
                Log.w("Error", "LogInWithEmailDataBaseWrite:Failure", task.exception)
                Toast.makeText(this, "Something went wrong!!", Toast.LENGTH_LONG).show()
            }
            //When the requests complete, again make them clickable:
            loginBtn.isClickable = true
            loginBtn.isEnabled = true
        }
    }


}