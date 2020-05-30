package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements
import java.lang.Exception

class ItemFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_item_list)

        val links = PDFService.startService("https://www.cs.otago.ac.nz/cosc244/lectures.php", "", this)
        Log.d("ItemFragment", "finished fetching data")
        val list = generateList(links)

        recycler_view.adapter = MyItemRecyclerViewAdapter(list) {

        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.setHasFixedSize(true)
        }
    }

    fun generateList(links: ArrayList<String>): List<TestItem> {

        val list = ArrayList<TestItem>()

        for (i in 0 until links.size) {
            val drawable = when (i % 3) {
                0 -> R.drawable.ic_android
                1 -> R.drawable.ic_sentiment
                else -> R.drawable.ic_thumb
            }

            Log.d("generateList", links[i])
            val item = TestItem(drawable, "Lecture $i", text2 = links[i])
            list += item
        }

        return list
    }

    object PDFService {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        fun startService(url : String, storage : String, context : Context): ArrayList<String> {
            Log.d("startService", "begin PDFService")
            var links = ArrayList<String>()
            coroutineScope.launch {
                links = fetchLinks(url)
                if(links.size > 0) {
                    val download = downloadPDF(links, storage, context)
                }
                Log.d("startService", "exit coroutine scope")
            }
            return links
        }

        private suspend fun fetchLinks(url : String): ArrayList<String> {
            val links = ArrayList<String>()
            try {
                Log.d("fetchLinks", url)
                val document: Document = Jsoup.connect(url).get()
                val elements = document.select("tr a")
                for(i in 0 until elements.size) {
                    Log.d("fetchLinks", elements[i].attr("href"))
                    links += elements[i].attr("href") // not sure if this works or not
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return links
        }

        private suspend fun downloadPDF(links : ArrayList<String>, storage: String, context: Context) {
            Log.d("downloadPDF", "reached")
        }
    }



