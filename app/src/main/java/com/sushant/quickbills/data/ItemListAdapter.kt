package com.sushant.quickbills.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sushant.quickbills.R
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.item_row.view.*

class ItemListAdapter(private val itemList: ArrayList<Item>, private val context: Context) :
    RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder>() {

    class ItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName = itemView.item_name_id
        val itemPrice = itemView.item_price_id

        fun bindViews(item : Item){
            itemName.text = item.itemName
            itemPrice.text = "$ ${item.price}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false)
        return ItemListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        holder.bindViews(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}