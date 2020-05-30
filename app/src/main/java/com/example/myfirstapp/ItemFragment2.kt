package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.io.File

class ItemFragment2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_list)

        val list = generateList(filesDir.listFiles().size)

        recycler_view.adapter = MyItemRecyclerViewAdapter(list)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
    }

    private fun generateList(size: Int): List<TestItem> {

        val list = ArrayList<TestItem>()

        for (i in 0 until size) {
            val fileType = if (filesDir.listFiles()[i].isDirectory) {
                "folder"
            } else {
                filesDir.listFiles()[i].extension
            }

            val drawable = if (fileType == "folder") {
                R.drawable.ic_folder
            } else if (fileType == "pdf" ){
                R.drawable.ic_pdf
            } else {
                R.drawable.ic_thumb
            }

            val item = TestItem(drawable, filesDir.listFiles()[i].name, fileType)
            list += item
        }

        return list
    }
}
