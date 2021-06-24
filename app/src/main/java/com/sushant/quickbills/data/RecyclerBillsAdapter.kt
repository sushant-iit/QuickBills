package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sushant.quickbills.R
import com.sushant.quickbills.model.Bill
import com.sushant.quickbills.model.DateItem
import com.sushant.quickbills.model.ListItem
import kotlinx.android.synthetic.main.row_bill.view.*
import kotlinx.android.synthetic.main.row_date.view.*
import java.text.DateFormat
import java.util.*

class RecyclerBillsAdapter(
    private val context: Context,
    private val consolidateList: ArrayList<ListItem>,
    private val listener : OnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val view: View
        when (viewType) {
            //Type 0 is Date and Type 1 is Bill
            0 -> {
                view = LayoutInflater.from(context).inflate(R.layout.row_date, parent, false)
                viewHolder = DateViewHolder(view)
            }
            1 -> {
                view = LayoutInflater.from(context).inflate(R.layout.row_bill, parent, false)
                viewHolder = BillViewHolder(view)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val dateItem = consolidateList[position] as DateItem
                val dateViewHolder = holder as DateViewHolder
                dateViewHolder.billDate.text = dateItem.date
            }
            1 -> {
                val billItem = consolidateList[position] as Bill
                val billViewHolder = holder as BillViewHolder
                //Extract time to show (note negative is there to maintain descending order)
                val dateObj = Date(-billItem.purchasedAt!!.toLong())
                val timeString = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(dateObj)
                val amountString = "\u20B9 ${billItem.totalAmount.toString()}"
                //Set up the view now
                billViewHolder.billCustomerName.text = billItem.customerDetails!!.name.toString()
                billViewHolder.billCustomerMob.text = billItem.customerDetails!!.number.toString()
                billViewHolder.billAmount.text = amountString
                billViewHolder.billCreateTime.text = timeString
            }
        }
    }

    override fun getItemCount(): Int {
        return consolidateList.size
    }

    override fun getItemViewType(position: Int): Int {
        return consolidateList[position].getType()
    }

    //Create separate viewHolders for both
    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val billDate = itemView.date_id!!
    }

    inner class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val billCustomerName: TextView = itemView.customer_name_id
        val billAmount: TextView = itemView.purchase_amount_id
        val billCustomerMob: TextView = itemView.customer_mobile_id
        val billCreateTime: TextView = itemView.purchase_time_id
        private val printBtn: ImageView = itemView.print_bill_id
        private val deleteBtn : ImageView = itemView.delete_bill_id

        init {
            printBtn.setOnClickListener(this)
            deleteBtn.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when(v!!.id){
                R.id.print_bill_id-> listener.printPDF(consolidateList[adapterPosition] as Bill)
                R.id.delete_bill_id->listener.deleteBill((consolidateList[adapterPosition] as Bill).key.toString())
            }
        }
    }

    interface OnClickListener{
        fun printPDF(bill: Bill)
        fun deleteBill(key : String)
    }
}