package com.example.myfirstapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_item_list.*


class RecyclerViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val list = generateList(context?.filesDir!!.listFiles().size)

        recycler_view.adapter = MyItemRecyclerViewAdapter(list) { item ->
            openFile(item)
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)
    }

    private fun generateList(size: Int): List<TestItem> {

        val list = ArrayList<TestItem>()

        for (i in 0 until size) {
            val fileType = if (context?.filesDir!!.listFiles()[i].isDirectory) {
                "folder"
            } else {
                context?.filesDir!!.listFiles()[i].extension
            }

            val drawable = when (fileType) {
                "folder" -> R.drawable.ic_folder
                "pdf" -> R.drawable.ic_pdf
                else -> R.drawable.ic_thumb
            }

            val item = TestItem(drawable, context?.filesDir!!.listFiles()[i].name, fileType)
            list += item
        }

        return list
    }

    private fun openFile(item: TestItem) {
        val myToast = Toast.makeText(context, "poggers?", Toast.LENGTH_SHORT)
        myToast.show()

        /*
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        val action = FirstFragmentDirections.actionFirstFragmentToRecyclerPdfFragment(item.text1)
        NavHostFragment.findNavController(nav_host_fragment).navigate(action)
        */

        /*
        val file = File(filesDir, item.text1)
        val pdfView = findViewById<PDFView>(R.id.recyclerPdfView)

        pdfView.fromFile(file).load()
         */

    }
}

