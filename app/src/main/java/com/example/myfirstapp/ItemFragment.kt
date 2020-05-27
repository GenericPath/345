package com.example.myfirstapp

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_item_list.*
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements
import java.lang.Exception
import java.lang.ref.WeakReference


class ItemFragment : AppCompatActivity() {
    var elements : Elements = Elements()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_list)

        val task : AsyncTask<Void,Void,Void> = LinkListTask("https://www.cs.otago.ac.nz/cosc244/lectures.php").execute()
        val list = generateList(elements)


        recycler_view.adapter = MyItemRecyclerViewAdapter(list)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
    }

    private fun generateList(elements : Elements): List<TestItem> {

        val list = ArrayList<TestItem>()

        for (i in 0 until elements.size) {
            val drawable = when (i % 3) {
                0 -> R.drawable.ic_android
                1 -> R.drawable.ic_sentiment
                else -> R.drawable.ic_thumb
            }

            val item = TestItem(drawable, "Lecture $i", text2 = elements[i].attr("href"))
            list += item
        }

        return list
    }

    inner class LinkListTask(private val url : String) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void?): Void? {
            // Get the html document from the url
            try {
                val document: Document = Jsoup.connect(url).get()
                elements = document.select("tr a")
            } catch (e : Exception) {
                e.printStackTrace()
            }


            return null
        }
    }

}



