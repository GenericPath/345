/*
Copyright (C) 2020 Damian Soo, Garth Wales, Louis Whitburn

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package com.otago.open

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_fetch.*
import kotlinx.android.synthetic.main.fragment_item_list.recycler_view
import kotlinx.coroutines.*
import java.io.*
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
     * The coroutine to use for downloading PDFs / folders
     */
    private var pdfService: PDFService? = null

    /**
     * Entry point for creating a [PDFListFragment]
     * Ensure that the instance is retained on back button press
     *
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    /**
     * Entry point of the [PDFListFragment] view.
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pdf_list_view, container, false)
    }

    /**
     * Create the items to display upon starting the fragment.
     * All items are pulled from one directory
     *
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)

        if (args.listFiles || pdfService != null) {
            //If we are just listing files (no HTTP) or we have already visited this page
            // then we can just set the recycler items
            //Since we already have folders (or not) we don't need to complain
            setRecyclerItems(false)
        } else if (pdfService == null) {
            //Block UI while the available files are fetched (but only if none exist already)
            http_bar.visibility = View.VISIBLE
        }

        var item: FetchResult? = null
        //Try to load the meta file
        try {
            item = PDFOperations.loadMetaFile(args.folder)
        } catch (e: IOException) {
            //TODO: Something
        } catch (e: FileNotFoundException) {
            //TODO: Something
        }

        if (item == null) {
            //TODO: Something
            return
        }

        //To check network status - we don't want to waste mobile data
        val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //If we already have some files cached, and the network is metered then we *don't* want to re-download the PDFs
        //Otherwise, we may as well re-download them. Since we don't delete old things this isn't a problem
        //But don't download again in the same app instance (prevents re-downloading on navigation)
        //TODO: Better invalidation logic (user setting), handle new and old PDFs being different (in PDFService)
        if (!args.listFiles or !cm.isActiveNetworkMetered) {
            if (pdfService != null && pdfService!!.isActive) {
                return
            }
            Log.d("PDF Service", "Starting")

            pdfService = PDFService()
            pdfService!!.startService(
                args.folder,
                item.itemUrl,
                this,
                item.itemUrl.endsWith("index.php", true)
            ) //Only look for folders on the index page!
        }
    }

    /**
     * Sets the items in the recycler view by enumerating the directory
     *
     * @param cacheMessage - Whether to send a toast about having no files present in the cache or about no files at all
     */
    @Suppress("SameParameterValue") //TODO: Check this - needed to make the warning go away but the warning doesn't seem to be true?
    private fun setRecyclerItems(cacheMessage: Boolean) {
        //Generate the file list
        setRecyclerItems(cacheMessage, generateFolderList(args.folder))
    }

    /**
     * Sets the items in the recycler view from some given [PDFItem]s
     *
     * @param cacheMessage - Whether to send a toast about having no files present
     * @param list - The [PDFItem]s to add
     */
    private fun setRecyclerItems(cacheMessage: Boolean, list: List<PDFItem>) {
        //If we are called from a coroutine which is running with a destroyed fragment
        //e.g. from after navigation we don't want to do anything here
        if (recycler_view == null) {
            return
        }

        //Only send the message if we want to
        if (list.isEmpty()) {
            if (cacheMessage)
            {
                Toast.makeText(context, "No content downloaded yet", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No content yet", Toast.LENGTH_SHORT).show()
            }
        }

        //Preserve the view state (i.e. the scroll position)
        val recyclerViewState = recycler_view.layoutManager?.onSaveInstanceState()

        //Add to the recycler, with openFile as the click event handler
        recycler_view.adapter = PDFItemRecyclerViewAdapter(list) { item ->
            openFile(item)
        }

        //Restore the view state
        recycler_view.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    /**
     * Gets a [FetchResult] from a given file, by loading its associated meta file
     *
     * @param it The file to generate a [FetchResult] for
     *
     * @return A [FetchResult] from the file and meta file, if successful, otherwise null
     */
    private fun getPdfItemFetchResult(it: File): FetchResult? {
        try {
            if (it.absolutePath.endsWith(".meta")) {
                return null
            }
            return PDFOperations.loadMetaFile(it.absolutePath)
        } catch (e: IOException) {
            return null
        } catch (e: FileNotFoundException) {
            return null
        }

    }

    /**
     * Creates list of [PDFItem] items from what is present in the current directory (paper folder / sub name)
     *
     * @return The list of [PDFItem]s in the specified directory
     */
    fun generateFolderList(folder: String): List<PDFItem> {
        //Get the "current folder"
        val currentFolder = File(folder)
        val result = ArrayList<FetchResult>()
        val files = currentFolder.listFiles()

        //Show a message if there are no files
        if (files?.size == 0) {
            return emptyList()
        }

        //Only select PDFs and directories
        files?.forEach {
            val item = getPdfItemFetchResult(it)

            //TODO: Handle null (something)
            if (item != null) {
                result.add(item)
            }
        }

        //Show a message if there are nothing of interest in the folder
        if (result.size == 0) {
            return emptyList()
        }

        //Create a list of PDF (or folder) items from out FetchItems
        return PDFOperations.generatePdfItems(result)
    }

    /**
     * Opens item in the directory, either PDF or folder.
     * @param item The [PDFItem] to display
     */
    private fun openFile(item: PDFItem) {
        //pdfService?.cancelService()
        when (item.pathType) {
            FileNavigatorType.PDF -> {
                Log.d("PDF View (URL)", item.itemUrl)
                Log.d("PDF View (Folder)", item.itemFile)
                //If is's a PDF then open the PDF in the PDFViewFragment
                val action = PDFListFragmentDirections.actionPDFListFragmentToPDFViewFragment(item.itemFile, item.itemUrl)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            FileNavigatorType.FOLDER -> {
                //Just go to where the PDFItem tells us
                val action = PDFListFragmentDirections.actionPDFListFragmentSelf(
                    item.itemFile,
                    args.paperCode,
                    args.listFiles
                )
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            FileNavigatorType.MARKS -> {
                //For the marks we want to go to the marks fragment
                Log.d("Mark View POST", item.itemUrl)
                NavHostFragment.findNavController(nav_host_fragment).navigate(PDFListFragmentDirections.actionPDFListFragmentToMarkViewFragment(item.itemUrl))
            }
        }
    }

    /**
     * Coordinates fetching and downloading PDF files from a url.
     * Use [PDFService.startService] to begin coroutines will fetch,
     * and then download each PDF.
     */
    class PDFService {
        /**
         * The coroutine scope for this coroutine service
         */
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Whether the current coroutine is running
         */
        val isActive: Boolean
            get() = coroutineScope.isActive

        /**
         * Starts fetching and downloading PDFs.
         * Utilises coroutines to run networking on a separate threads to UI.
         *
         * @see PDFOperations.fetchLinks for link logic
         * @see PDFOperations.downloadPDF for downloading logic
         * @see PDFItem for more details on what the [parentFolder], and [url] are part of
         *
         * @param parentFolder The folder in which the downloaded content will be stored
         * @param url The URL at which we will look for folders and PDFs etc
         * @param inFragment The instance of [PDFListFragment], to call in class functions
         * @param doFolders Whether we want to include folders. Currently this should only be true if paperSubName is "" or "index.php" due to the COSC website navigation structure
         */
        fun startService(parentFolder: String, url: String, inFragment: PDFListFragment, doFolders: Boolean) {
            //Launch coroutine
            coroutineScope.launch {
                //Fetch the links
                val fetchedLinks = PDFOperations.fetchLinks(parentFolder, url, doFolders)
                val pdfs = PDFOperations.generatePdfItems(fetchedLinks)

                withContext(Dispatchers.Main) {
                    if (inFragment.http_bar != null) {
                        inFragment.http_bar.visibility = View.GONE
                    }
                    //If we are just listing files don't complain if we don't find any - they will see that already
                    inFragment.setRecyclerItems(!inFragment.args.listFiles, pdfs)
                }

                //If we do have links, download them
                if (fetchedLinks.size > 0) {
                    fetchedLinks.forEach {
                        PDFOperations.downloadPDF(it)
                    }
                }
            }
        }
    }
}

