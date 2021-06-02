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
    private val context: Context,
    private val listener : customerAdapterClickListener
) :
    RecyclerView.Adapter<CustomerListAdapter.CustomerListViewHolder>() {

    //This class holds views Ids;
    inner class CustomerListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val customerName = itemView.customer_name_id
        val customerMobile = itemView.customer_mobile_id
        val customerAddress = itemView.customer_address_id
        val editBtn = itemView.edit_customer_id
        val deleteBtn = itemView.delete_customer_id

        init{
            editBtn.setOnClickListener(this)
            deleteBtn.setOnClickListener(this)
        }

        fun bindViews(customer : Customer){
            customerName.text = customer.Name
            customerMobile.text = customer.Number.toString()
            customerAddress.text = customer.Address
        }

        //This basically passes the item clicked position to CustomerActivity which handles it correctly
        override fun onClick(v: View?) {
            when(v!!.id){
                R.id.edit_customer_id -> listener.showEditCustomerPopUp(adapterPosition)
                R.id.delete_customer_id->listener.showDeleteCustomerPopUp(adapterPosition)
            }
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

    interface customerAdapterClickListener{
        fun showEditCustomerPopUp(position: Int)
        fun showDeleteCustomerPopUp(position: Int)
    }
}