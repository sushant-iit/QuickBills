package com.sushant.quickbills.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sushant.quickbills.R
import com.sushant.quickbills.data.ItemListAdapter
import com.sushant.quickbills.model.Item
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {
    var itemList = arrayListOf<Item>()
    val layoutManager = LinearLayoutManager(this)
    val itemListAdapter = ItemListAdapter(itemList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        //TODO("Remember to remove this dummy data with real data from the database)
        var item = Item("Jagdamba Sarees", 5078.12)
        for(i in 1..1000)
            itemList.add(item)

        //Setting up recycler view
        item_list_recycler_view_id.layoutManager = layoutManager
        item_list_recycler_view_id.adapter = itemListAdapter
        itemListAdapter.notifyDataSetChanged()
    }
}
