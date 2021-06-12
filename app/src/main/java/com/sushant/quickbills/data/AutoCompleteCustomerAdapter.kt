package com.sushant.quickbills.data

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.sushant.quickbills.R
import com.sushant.quickbills.model.Customer
import kotlinx.android.synthetic.main.row_auto_complete.view.*

class AutoCompleteCustomerAdapter(context: Context,var customerList: MutableList<Customer>) :
    ArrayAdapter<Customer>(context, 0, customerList) {
    private var customerListFull: MutableList<Customer> = ArrayList(customerList)

    override fun getFilter(): Filter {
        return filter
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_auto_complete, parent, false)
        val rowText = view.row_auto_complete_text_id
        val currCustomer = getItem(position)
        if (currCustomer != null)
            rowText.text = currCustomer.name
        return view
    }

    //Create the filter
    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            val suggestions = arrayListOf<Customer>()
            if (constraint.toString().isNotEmpty()) {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in customerListFull) {
                    val customerName = item.name!!.lowercase().trim()
                    if (customerName.contains(filterPattern))
                        suggestions.add(item)
                }
            }
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            customerList.clear()
            if(results!!.count > 0) {
                //Suppressing as I am sure:
                @Suppress("UNCHECKED_CAST")
                customerList.addAll(results.values as List<Customer>)
            }
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as Customer).name.toString()
        }
    }

}