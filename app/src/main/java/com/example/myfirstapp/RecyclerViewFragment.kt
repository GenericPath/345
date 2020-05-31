package com.example.myfirstapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.io.File


class RecyclerViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    val args : RecyclerViewFragmentArgs by navArgs()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dir = File(args.dir)
        val list = generateList(dir.listFiles().size)

        recycler_view.adapter = MyItemRecyclerViewAdapter(list) { item ->
            openFile(item)
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)
    }

    private fun generateList(size: Int): List<TestItem> {

        val list = ArrayList<TestItem>()
        val dir = File(args.dir)

        for (i in 0 until size) {
            val fileType = if (dir.listFiles()[i].isDirectory) {
                "folder"
            } else {
                dir.listFiles()[i].extension
            }

            val drawable = when (fileType) {
                "folder" -> R.drawable.ic_folder
                "pdf" -> R.drawable.ic_pdf
                else -> R.drawable.ic_thumb
            }

            val item = TestItem(drawable, dir.listFiles()[i].name, fileType)
            list += item
        }

        return list
    }

    private fun openFile(item: TestItem) {
        when (item.text2) {
            "pdf" -> {
                val action = RecyclerViewFragmentDirections.actionRecyclerViewFragmentToRecyclerPdfFragment3(item.text1)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            "folder" -> {
                val action = RecyclerViewFragmentDirections.actionRecyclerViewFragmentSelf(args.dir + "/" + item.text1)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            else -> {
                val toast = Toast.makeText(context, args.dir + "/" + item.text1, Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }
}

