package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sushant.quickbills.R
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.customer_row.view.*

class CustomerListAdapter(
    private val customerList: ArrayList<Customer>,
    private val context: Context
) :
    RecyclerView.Adapter<CustomerListAdapter.CustomerListViewHolder>() {

    //This class holds views Ids;
    class CustomerListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName = itemView.customer_name_id
        val customerMobile = itemView.customer_mobile_id
        val customerAddress = itemView.customer_address_id
        fun bindViews(customer : Customer){
            customerName.text = customer.Name
            customerMobile.text = customer.Number.toString()
            customerAddress.text = customer.Address
        }
    }

    //This basically first runs and parses our layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.customer_row, parent, false)
        return CustomerListViewHolder(view)
    }

    //This passes data for binding to views
    override fun onBindViewHolder(holder: CustomerListViewHolder, position: Int) {
        holder.bindViews(customerList[position])
    }

    override fun getItemCount(): Int {
        return customerList.size
    }
}