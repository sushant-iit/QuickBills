package com.sushant.quickbills.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.sushant.quickbills.R
import com.sushant.quickbills.data.*
import com.sushant.quickbills.model.User
import com.sushant.quickbills.utils.getCompressedByteArray
import kotlinx.android.synthetic.main.activity_my_account.*
import kotlinx.android.synthetic.main.pop_up_my_account.view.*


class MyAccountActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference
    private lateinit var currUserQuery: Query
    private lateinit var brandNameField: TextView
    private lateinit var brandNumberField: TextView
    private lateinit var brandAddressField: TextView
    private val storageRef = Firebase.storage.reference
    private lateinit var progressBar: ProgressBar
    private var currentProgress: Int = 0

    private val cropImageActivityContract = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL).getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }
    }

    private val cropImageActivityLauncher =
        registerForActivityResult(cropImageActivityContract) { croppedImageUri ->
            if (croppedImageUri != null) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = 0
                //Compress the image to save costs and time of network transactions and storage space
                val compressedByteArrayImage = getCompressedByteArray(croppedImageUri, 60, this)
                //Upload Now:-
                val profileLogoPath = storageRef.child("logos/${auth.currentUser!!.uid}.jpg")
                profileLogoPath.putBytes(compressedByteArrayImage)
                    .addOnCompleteListener { uploadTask ->
                        progressBar.visibility = View.GONE
                        if (uploadTask.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Image uploaded successfully..",
                                Toast.LENGTH_SHORT
                            ).show()
                            //Update the logoUrl
                            profileLogoPath.downloadUrl.addOnSuccessListener { uploadedUri ->
                                database.child(USERS_FIELD).child(auth.currentUser!!.uid)
                                    .child(LOGO_URL).setValue(uploadedUri.toString())
                            }
                        } else {
                            Log.e("Error", uploadTask.exception!!.message!!)
                        }
                    }.addOnProgressListener { uploadSnapshot ->
                        val progress: Double =
                            100.0 * uploadSnapshot.bytesTransferred / uploadSnapshot.totalByteCount
                        progressBar.progress = progress.toInt()
                    }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        //Doing initialisations
        brandNameField = user_brand_name_account
        brandNumberField = user_brand_number
        brandAddressField = user_brand_address
        currUserQuery = database.child(USERS_FIELD).child(auth.currentUser!!.uid)
        progressBar = progressBarId

        //Set up basic info
        user_email.text = auth.currentUser!!.email
        user_name_account.text = auth.currentUser!!.displayName


        //Setting up click listeners
        edit_user_name.setOnClickListener {

            val view = layoutInflater.inflate(R.layout.pop_up_my_account, null, false)
            val defaultIcon = view.account_pop_up_icon
            val inputEditText = view.account_pop_up_entered_text
            val submitBtn = view.account_pop_up_submit_btn
            val updateUserNameDialog = AlertDialog.Builder(this).setView(view).create()

            //Set Dialog Default Properties
            val submitBtnText = "Update Name"
            defaultIcon.setImageResource(R.drawable.ic_person)
            inputEditText.hint = "Your Name:"
            inputEditText.setText(user_name_account.text.toString())
            inputEditText.inputType = EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME
            submitBtn.text = submitBtnText

            //Set up submit button
            submitBtn.setOnClickListener {
                if (inputEditText.text.toString().trim().isEmpty()) {
                    inputEditText.error = "Your Name can't be empty."
                    inputEditText.requestFocus()
                } else {
                    updateUserNameDialog.dismiss()
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(inputEditText.text.toString()).build()
                    auth.currentUser!!.updateProfile(profileUpdates).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                            user_name_account.text = auth.currentUser!!.displayName
                        } else {
                            Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show()
                            Log.w("UpdateUserNameFailure", it.exception)
                        }
                    }
                }
            }

            //Show the dialog
            updateUserNameDialog.show()
        }
        edit_brand_name.setOnClickListener {

            val view = layoutInflater.inflate(R.layout.pop_up_my_account, null, false)
            val defaultIcon = view.account_pop_up_icon
            val inputEditText = view.account_pop_up_entered_text
            val submitBtn = view.account_pop_up_submit_btn
            val updateBrandDialog = AlertDialog.Builder(this).setView(view).create()

            //Set Dialog Default Properties
            val submitBtnText = "Update Brand"
            defaultIcon.setImageResource(R.drawable.ic_shop)
            inputEditText.hint = "Brand Name:"
            inputEditText.setText(user_brand_name_account.text.toString())
            submitBtn.text = submitBtnText

            //Set up submit button
            submitBtn.setOnClickListener {
                if (inputEditText.text.toString().trim().isEmpty()) {
                    inputEditText.error = "Brand (Shop) Name can't be empty."
                    inputEditText.requestFocus()
                } else {
                    updateBrandDialog.dismiss()
                    (currUserQuery as DatabaseReference).child(BRAND_NAME_FIELD)
                        .setValue(inputEditText.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                            else {
                                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT)
                                    .show()
                                Log.w("UpdateUserBrandFailure", it.exception)
                            }
                        }
                }
            }

            //Show the dialog
            updateBrandDialog.show()
        }
        edit_brand_number.setOnClickListener {

            val view = layoutInflater.inflate(R.layout.pop_up_my_account, null, false)
            val defaultIcon = view.account_pop_up_icon
            val inputEditText = view.account_pop_up_entered_text
            val submitBtn = view.account_pop_up_submit_btn
            val updateBrandNumberDialog = AlertDialog.Builder(this).setView(view).create()

            //Set Dialog Default Properties
            val submitBtnText = "Update Contact"
            defaultIcon.setImageResource(R.drawable.ic_baseline_call_24)
            inputEditText.hint = "Brand Number:"
            inputEditText.setText(user_brand_number.text.toString())
            inputEditText.inputType = EditorInfo.TYPE_CLASS_PHONE
            submitBtn.text = submitBtnText

            //Set up submit button
            submitBtn.setOnClickListener {
                if (inputEditText.text.toString().trim().isEmpty()) {
                    inputEditText.error = "Brand (Shop) Number can't be empty."
                    inputEditText.requestFocus()
                } else {
                    updateBrandNumberDialog.dismiss()
                    (currUserQuery as DatabaseReference).child(BRAND_NUMBER_FIELD)
                        .setValue(inputEditText.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                            else {
                                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT)
                                    .show()
                                Log.w("UpdateUserBrandNoFail", it.exception)
                            }
                        }
                }
            }

            //Show the dialog
            updateBrandNumberDialog.show()
        }
        edit_brand_address.setOnClickListener {

            val view = layoutInflater.inflate(R.layout.pop_up_my_account, null, false)
            val defaultIcon = view.account_pop_up_icon
            val inputEditText = view.account_pop_up_entered_text
            val submitBtn = view.account_pop_up_submit_btn
            val updateBrandAddressDialog = AlertDialog.Builder(this).setView(view).create()

            //Set Dialog Default Properties
            val submitBtnText = "Update Address"
            defaultIcon.setImageResource(R.drawable.ic_baseline_location_on_24)
            inputEditText.hint = "Brand Address:"
            inputEditText.setText(user_brand_address.text.toString())
            inputEditText.inputType = EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
            submitBtn.text = submitBtnText

            //Set up submit button
            submitBtn.setOnClickListener {
                if (inputEditText.text.toString().trim().isEmpty()) {
                    inputEditText.error = "Brand (Shop) Address can't be empty."
                    inputEditText.requestFocus()
                } else {
                    updateBrandAddressDialog.dismiss()
                    (currUserQuery as DatabaseReference).child(BRAND_ADDRESS_FIELD)
                        .setValue(inputEditText.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful)
                                Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show()
                            else {
                                Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT)
                                    .show()
                                Log.w("UpdtUserAddressFailure", it.exception)
                            }
                        }
                }
            }

            //Show the dialog
            updateBrandAddressDialog.show()
        }
        uploadLogoBtn.setOnClickListener {
            cropImageActivityLauncher.launch(null)
        }
        reset_password_btn.setOnClickListener {
            reset_password_btn.isEnabled = false
            reset_password_btn.isClickable = false
            reset_password_btn.setBackgroundColor(Color.DKGRAY)
            auth.sendPasswordResetEmail(auth.currentUser!!.email.toString())
                .addOnCompleteListener { sendResetMailTask ->
                    if (sendResetMailTask.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Reset Email sent to your email successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            sendResetMailTask.exception!!.localizedMessage,
                            Toast.LENGTH_LONG
                        ).show()
                        Log.w("Error", sendResetMailTask.exception)
                    }
                }
        }
        delete_data_btn.setOnClickListener {
            //UI configurations
            currentProgress = 0
            delete_data_btn.isClickable = false
            delete_data_btn.isEnabled = false
            delete_data_btn.setBackgroundColor(Color.DKGRAY)
            progressBar.visibility = View.VISIBLE
            progressBar.progress = currentProgress

            //Start deleting the data
            deleteData(ITEMS_FIELD)
            deleteData(CUSTOMERS_FIELD)
            deleteData(BILLS_FIELD)

            //Delete User Profile pic (Just Changing the url to "default")
            database.child(USERS_FIELD).child(auth.currentUser!!.uid).child(LOGO_URL)
                .setValue("default").addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    currentProgress += 25
                    progressBar.progress = currentProgress
                    Toast.makeText(this, "Removed Picture...", Toast.LENGTH_SHORT).show()
                    if (currentProgress == 100)
                        progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, deleteTask.exception!!.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                    Log.w("DeleteAllData", deleteTask.exception)
                }
            }
        }
    }

    //Get the user brand info
    override fun onStart() {
        val currUserListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currUser: User = snapshot.getValue(User::class.java)!!
                brandNameField.text = currUser.brandName
                brandAddressField.text = currUser.brandAddress
                brandNumberField.text = currUser.brandNumber
                if (currUser.logoUrl != "default")
                    Picasso.get().load(currUser.logoUrl).noPlaceholder()
                        .into(profile_image)
                else
                    profile_image.setImageResource(R.mipmap.sample_logo)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("AccountActivityDbError", error.details)
            }

        }
        currUserQuery.addValueEventListener(currUserListener)
        super.onStart()
    }

    //Delete the account data from database function
    private fun deleteData(field: String) {
        database.child(field).child(auth.currentUser!!.uid).removeValue()
            .addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    currentProgress += 25
                    progressBar.progress = currentProgress
                    Toast.makeText(this, "Deleted $field...", Toast.LENGTH_SHORT).show()
                    if (currentProgress == 100)
                        progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, deleteTask.exception!!.localizedMessage, Toast.LENGTH_LONG)
                        .show()
                    Log.w("DeleteAllData", deleteTask.exception)
                }
            }
    }

}