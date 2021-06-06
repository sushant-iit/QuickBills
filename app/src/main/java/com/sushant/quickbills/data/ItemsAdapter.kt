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
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.item_row.view.*

class ItemsAdapter(
    private val context: Context,
    options: FirebaseRecyclerOptions<Item>,
    private val listener: OnClickListener
) :
    FirebaseRecyclerAdapter<Item, ItemsAdapter.ItemsViewHolder>(options) {
    inner class ItemsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val itemName = itemView.item_name_id
        private val itemPrice = itemView.item_price_id
        private val editBtn = itemView.edit_item_id
        private val deleteBtn = itemView.delete_item_id

        init {
            editBtn.setOnClickListener(this)
            deleteBtn.setOnClickListener(this)
        }

        fun bindViews(item: Item) {
            val price = "\u20B9 ${item.price}"
            itemName.text = item.name
            itemPrice.text = price
        }

        //Passing the item reference clicked
        override fun onClick(v: View?) {
            when (v!!.id) {
                R.id.edit_item_id -> listener.showEditCustomerPopUp(
                    getRef(adapterPosition),
                    getItem(adapterPosition)
                )
                R.id.delete_item_id -> listener.showDeleteCustomerPopUp(
                    getRef(adapterPosition),
                    getItem(adapterPosition)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false)
        return ItemsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int, model: Item) {
        holder.bindViews(model)
    }

    interface OnClickListener {
        fun showEditCustomerPopUp(itemReference: DatabaseReference, currItem: Item)
        fun showDeleteCustomerPopUp(itemReference: DatabaseReference, currItem: Item)
    }
}