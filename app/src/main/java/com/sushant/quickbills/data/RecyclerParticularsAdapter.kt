package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sushant.quickbills.model.Bill
import androidx.recyclerview.widget.RecyclerView
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.row_bill.view.*

class RecyclerParticularsAdapter(
    private val context: Context,
    private val particularsList: ArrayList<Bill.ParticularItem>
) : RecyclerView.Adapter<RecyclerParticularsAdapter.ParticularsViewHolder>() {
    inner class ParticularsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var particularName = itemView.particular_name
        val particularPrice = itemView.particular_price
        val particularQty = itemView.particular_qty
        val particularAmt = itemView.particular_amount

        fun bindViews(particularItem: Bill.ParticularItem){
            particularName.text = particularItem.itemName
            particularPrice.text = particularItem.itemPrice.toString()
            particularQty.text = particularItem.itemQty.toString()
            particularAmt.text = particularItem.itemAmount.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticularsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_bill, parent, false)
        return ParticularsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticularsViewHolder, position: Int) {
        holder.bindViews(particularsList[position])
    }

    override fun getItemCount(): Int {
        return particularsList.size
    }
}