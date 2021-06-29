package com.sushant.quickbills.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sushant.quickbills.R
import com.sushant.quickbills.data.NIGHT_MODE_ON
import com.sushant.quickbills.data.PREFS_NAME
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var mySharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        //Reset to window mde from full screen mode in splash screen to main activity
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)

        //Set up Theme
        mySharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        var isDark = 0
        if(mySharedPref.contains(NIGHT_MODE_ON))
            isDark = mySharedPref.getInt(NIGHT_MODE_ON, 0)
        if(isDark==1)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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