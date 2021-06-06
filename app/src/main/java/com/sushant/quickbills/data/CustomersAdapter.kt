package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.sushant.quickbills.R
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.customer_row.view.*

class CustomersAdapter(
    private val context: Context,
    options: FirebaseRecyclerOptions<Customer>,
    private val listener: onClickListener
) :
    FirebaseRecyclerAdapter<Customer, CustomersAdapter.CustomersViewHolder>(
        options
    ) {

    //This class holds views Ids;
    inner class CustomersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val customerName = itemView.customer_name_id
        val customerMobile = itemView.customer_mobile_id
        val customerAddress = itemView.customer_address_id
        val editBtn = itemView.edit_customer_id
        val deleteBtn = itemView.delete_customer_id

        init {
            editBtn.setOnClickListener(this)
            deleteBtn.setOnClickListener(this)
        }

        fun bindViews(customer: Customer) {
            customerName.text = customer.name
            customerMobile.text = customer.number
            customerAddress.text = customer.address
        }

        //This basically passes the item clicked database reference to CustomerActivity which
        // handles it correctly
        override fun onClick(v: View?) {
            when (v!!.id) {
                R.id.edit_customer_id -> listener.showEditCustomerPopUp(
                    getRef(adapterPosition),
                    getItem(adapterPosition)
                )
                R.id.delete_customer_id -> listener.showDeleteCustomerPopUp(
                    getRef(adapterPosition),
                    getItem(adapterPosition)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.customer_row, parent, false)
        return CustomersViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomersViewHolder, position: Int, model: Customer) {
            holder.bindViews(model)
    }

    interface onClickListener {
        fun showEditCustomerPopUp(customerReference: DatabaseReference, currCustomer: Customer)
        fun showDeleteCustomerPopUp(customerReference: DatabaseReference, currCustomer: Customer)
    }

}