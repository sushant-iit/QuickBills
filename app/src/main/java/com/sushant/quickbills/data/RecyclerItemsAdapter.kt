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
import kotlinx.android.synthetic.main.row_item.view.*

class RecyclerItemsAdapter(
    private val context: Context,
    options: FirebaseRecyclerOptions<Item>,
    private val listener: OnClickListener
) :
    FirebaseRecyclerAdapter<Item, RecyclerItemsAdapter.ItemsViewHolder>(options) {
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
                R.id.edit_item_id -> listener.showEditItemPopUp(
                    getRef(adapterPosition),
                    getItem(adapterPosition)
                )
                R.id.delete_item_id -> listener.showDeleteItemPopUp(
                    getRef(adapterPosition),
                    getItem(adapterPosition)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false)
        return ItemsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsViewHolder, position: Int, model: Item) {
        holder.bindViews(model)
    }

    interface OnClickListener {
        fun showEditItemPopUp(itemReference: DatabaseReference, currItem: Item)
        fun showDeleteItemPopUp(itemReference: DatabaseReference, currItem: Item)
    }
}