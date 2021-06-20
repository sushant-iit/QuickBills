package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sushant.quickbills.model.Bill
import androidx.recyclerview.widget.RecyclerView
import com.sushant.quickbills.R
import kotlinx.android.synthetic.main.row_particular.view.*

class RecyclerParticularsAdapter(
    private val context: Context,
    private val particularsList: ArrayList<Bill.ParticularItem>,
    private val listener: OnClickListener
) : RecyclerView.Adapter<RecyclerParticularsAdapter.ParticularsViewHolder>() {

    inner class ParticularsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var particularName = itemView.particular_name
        private val particularPrice = itemView.particular_price
        private val particularQty = itemView.particular_qty
        private val particularAmt = itemView.particular_amount
        private val particularCard = itemView.particular_item_card

        fun bindViews(particularItem: Bill.ParticularItem){
            particularName.text = particularItem.itemName
            particularPrice.text = particularItem.itemPrice.toString()
            particularQty.text = particularItem.itemQty.toString()
            particularAmt.text = particularItem.itemAmount.toString()
        }

        init {
            particularCard.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v!!.id){
                R.id.particular_item_card -> listener.deleteParticular(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticularsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_particular, parent, false)
        return ParticularsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticularsViewHolder, position: Int) {
        holder.bindViews(particularsList[position])
    }

    override fun getItemCount(): Int {
        return particularsList.size
    }

    interface OnClickListener{
        fun deleteParticular(position: Int)
    }
}