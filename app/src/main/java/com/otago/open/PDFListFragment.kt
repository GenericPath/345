package com.otago.open

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * A [Fragment] that generates each [PDFItem] to display.
 */
class PDFListFragment : Fragment() {
    /**
     * Args to pass in directory navigated to
     */
    private val args : PDFListFragmentArgs by navArgs()

    /**
     * Entry point of [PDFListFragment].
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    /**
     * Create the items to display upon starting the fragment.
     * All items are pulled from one directory
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Generate the file list
        val list = generateList()

        //Add to the recycler, with openFile as the click event handler
        recycler_view.adapter = PDFItemRecyclerViewAdapter(list) { item ->
            openFile(item)
        }
        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)
    }

    /**
     * Creates list of [PDFItem] items from what is present in specified directory.
     *
     * @return The list of [PDFItem]s in the specified directory
     */
    private fun generateList(): List<PDFItem> {
        val dir = File(args.dir)
        val filteredDir = ArrayList<File>()
        val files = dir.listFiles()

        //Show a message if there are no files
        if (files?.size == 0) {
            Toast.makeText(context, "No items in this folder", Toast.LENGTH_SHORT).show()
            return emptyList()
        }

        //Only select PDFs and directories
        files?.forEach {
            if ((it.extension.toLowerCase(Locale.ROOT) == "pdf") or it.isDirectory) {
                filteredDir += it
            }
        }

        //Show a message if there are nothing of interest in the folder
        if (filteredDir.size == 0) {
            Toast.makeText(context, "No PDFs or subfolders in this folder", Toast.LENGTH_SHORT).show()
            return emptyList()
        }

        //Create a list of PDF (or folder) items
        val list = ArrayList<PDFItem>()
        filteredDir.forEach {
            val fileType = if (it.isDirectory) {
                FileNavigatorType.FOLDER
            } else {
                FileNavigatorType.PDF
            }

            val drawable = when (fileType) {
                FileNavigatorType.FOLDER -> R.drawable.ic_folder
                FileNavigatorType.PDF -> R.drawable.ic_pdf
            }

            list += PDFItem(drawable, it.name, fileType)
        }

        //Sort the list by filename
        return list.toList()
    }

    /**
     * Opens item in the directory, either PDF or folder.
     * @param item The [PDFItem] to display
     */
    private fun openFile(item: PDFItem) {
        when (item.pathType) {
            FileNavigatorType.PDF -> {
                //If is's a PDF then open the PDF in the PDFViewFragment
                val action = PDFListFragmentDirections.actionPDFListFragmentToPDFViewFragment(args.dir + "/" + item.pathName)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            FileNavigatorType.FOLDER -> {
                //If it's a subfolder then return to this but in a new instance
                val action = PDFListFragmentDirections.actionPDFListFragmentSelf(args.dir + "/" + item.pathName)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
        }
    }
}

