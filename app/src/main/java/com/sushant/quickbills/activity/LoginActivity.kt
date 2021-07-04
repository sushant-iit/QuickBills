package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.pop_up_reset_password.view.*

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
                //Check if the user is email verified
                    if(auth.currentUser!!.isEmailVerified){
                        Toast.makeText(this, "Logged-In Successfully", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, Dashboard::class.java))
                        finish()
                    }else{
                        val msg = "Resend Verification Mail?"
                        resetResend.text = msg
                        resetResend.visibility = View.VISIBLE
                        Toast.makeText(this, "Email Not Verified", Toast.LENGTH_SHORT).show()
                        resetResend.setOnClickListener{
                            resetResend.isClickable = false
                            auth.currentUser!!.sendEmailVerification().addOnCompleteListener{
                                if(it.isSuccessful){
                                    Toast.makeText(this, "Verification Mail Sent", Toast.LENGTH_SHORT).show()
                                    resetResend.visibility = View.GONE
                                }else{
                                    Toast.makeText(this, it.exception!!.localizedMessage!!, Toast.LENGTH_SHORT).show()
                                    resetResend.isClickable = true
                                }
                            }
                        }
                    }
            }else{
                //If auth fails, means high chances are there of forgetting password
                val msg = "Forgot Password?"
                resetResend.text = msg
                resetResend.visibility = View.VISIBLE
                resetResend.setOnClickListener {
                    val view = layoutInflater.inflate(R.layout.pop_up_reset_password, null, false)
                    val submitBtn = view.reset_password_pop_up_btn
                    val email = view.reset_email_entered
                    val dialog = AlertDialog.Builder(this).setView(view).create()
                    submitBtn.setOnClickListener {
                        val enteredEmail = email.text
                        auth.sendPasswordResetEmail(enteredEmail.toString()).addOnCompleteListener{
                            sendResetMailTask->
                            if(sendResetMailTask.isSuccessful){
                                Toast.makeText(this, "Reset Email sent to your email successfully", Toast.LENGTH_LONG).show()
                                dialog.dismiss()
                            }else{
                                Toast.makeText(this, sendResetMailTask.exception!!.localizedMessage, Toast.LENGTH_LONG).show()
                                Log.w("Error", sendResetMailTask.exception)
                            }
                        }
                    }
                    dialog.show()
                }
                Log.w("Error", "LogInWithEmail:Failure", task.exception)
                Toast.makeText(this, task.exception!!.localizedMessage, Toast.LENGTH_LONG).show()
            }
            //When the requests complete, again make them clickable:
            loginBtn.isClickable = true
            loginBtn.isEnabled = true
        }
    }


}