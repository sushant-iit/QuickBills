package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.sushant.quickbills.R
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.row_auto_complete.view.*

class AutoCompleteParticularNameAdapter(context: Context,var itemList: MutableList<Item>) :
    ArrayAdapter<Item>(context, 0, itemList) {
    private var itemListFull: MutableList<Item> = ArrayList(itemList)

    override fun getFilter(): Filter {
        return filter

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.row_auto_complete, parent, false)
        val rowText = view.row_auto_complete_text_id
        val currItem = getItem(position)
        if (currItem != null)
            rowText.text = currItem.name
        return view
    }

    //Create the filter
    private val filter = object : Filter() {
        override fun performFiltering(constraint:CharSequence?): FilterResults {
            val results = FilterResults()
            val suggestions = arrayListOf<Item>()
            if (constraint.toString().isNotEmpty()) {
                val filterPattern = constraint.toString().lowercase().trim()
                for (item in itemListFull) {
                    val itemName = item.name!!.lowercase().trim()
                    if (itemName.contains(filterPattern))
                        suggestions.add(item)
                }
            }
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

            itemList.clear()
            if(results!!.count > 0) {
                //Suppressing as I am sure:
                @Suppress("UNCHECKED_CAST")
                itemList.addAll(results.values as List<Item>)
            }
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            return (resultValue as Item).name.toString()
        }
    }

    fun updateData(newItemList: MutableList<Item>){
        itemListFull.clear()
        for(item in newItemList){
            itemListFull.add(item)
        }
    }

}