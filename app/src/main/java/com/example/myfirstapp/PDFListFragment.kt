package com.example.myfirstapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.io.File
import kotlin.collections.ArrayList


class PDFListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    val args : PDFListFragmentArgs by navArgs()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var size = 0
        try {
            val dir = File(args.dir)
            for (i in 0 until dir.listFiles().size) {
                if ((dir.listFiles()[i].extension == "pdf") or dir.listFiles()[i].isDirectory) {
                    size++
                }
            }
        } catch (e : Exception) {
            Log.d("PDFListFragment", "Failed to count files")
            // should show an error on screen or via toast
        }

        val list = generateList(size)

        recycler_view.adapter = PDFItemRecyclerViewAdapter(list) { item ->
            openFile(item)
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)


    }

    private fun generateList(size: Int): List<PDFItem> {

        val dir = File(args.dir)
        val filteredDir = ArrayList<File>()
        try {
            for (i in 0 until dir.listFiles().size) {
                if ((dir.listFiles()[i].extension == "pdf") or dir.listFiles()[i].isDirectory) {
                    filteredDir += dir.listFiles()[i]
                }
            }
        } catch (e : Exception) {
            Log.d("PDFListFragment" , "Failed on use of dir.size in generate list")
            // should show an error on screen or via toast
        }

        val list = ArrayList<PDFItem>()

        for (i in 0 until size) {
            val fileType = if (filteredDir[i].isDirectory) {
                "folder"
            } else {
                filteredDir[i].extension
            }

            val drawable = when (fileType) {
                "folder" -> R.drawable.ic_folder
                "pdf" -> R.drawable.ic_pdf
                else -> R.drawable.ic_thumb
            }

            val item = PDFItem(drawable, filteredDir[i].name, fileType)
            list += item
        }

        return list
        //return fileSort(list)
    }

    private fun openFile(item: PDFItem) {
        when (item.pathType) {
            "pdf" -> {
                val action = PDFListFragmentDirections.actionPDFListFragmentToPDFViewFragment(args.dir + "/" + item.pathName)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            "folder" -> {
                val action = PDFListFragmentDirections.actionPDFListFragmentSelf(args.dir + "/" + item.pathName)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            else -> {
                val toast = Toast.makeText(context, args.dir + "/" + item.pathName, Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }

    private fun fileSort(list: ArrayList<PDFItem>) : List<PDFItem> {
        return list.sortedWith(compareBy {it.pathName})
    }
}

