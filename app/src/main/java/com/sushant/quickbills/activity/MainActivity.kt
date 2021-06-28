package com.sushant.quickbills.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        //Reset to window mde from full screen mode in splash screen to main activity
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setting up the listeners:
        //TODO("Improve Feedback later by getting feedback from login Activity")
        //As of now, it is totally fine...
        log_in_btn_id.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        sign_up_btn_id.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

    }

    override fun onStart() {
        //If the user is already logged-in, take him directly to the dashboard:
        auth = Firebase.auth
        if (auth.currentUser != null && auth.currentUser!!.isEmailVerified) {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }
        super.onStart()
    }
}