package com.example.myfirstapp

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_item_list.*

class ItemFragment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_list)

        val list = generateList(500)

        recycler_view.adapter = MyItemRecyclerViewAdapter(list)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
    }

    private fun generateList(size: Int): List<TestItem> {

        val list = ArrayList<TestItem>()

        for (i in 0 until size) {
            val drawable = when (i % 3) {
                0 -> R.drawable.ic_android
                1 -> R.drawable.ic_sentiment
                else -> R.drawable.ic_thumb
            }

            val item = TestItem(drawable, "Item $i", "Line 2")
            list += item
        }

        return list
    }
}
