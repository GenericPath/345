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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_pdf_list_view.*
import kotlinx.coroutines.*
import java.io.*
import kotlin.collections.ArrayList

/**
 * A [Fragment] that generates each [PDFItem] to display.
 */
class PDFListFragment : Fragment() {
    /**
     * The items in the recycler
     */
    private var adapterItems : List<PDFItem> = emptyList()

    /**
     * Whether the [adapterItems] parameter is valid
     */
    private var hasItems: Boolean = false

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
        activity!!.toolbar.title = args.navName
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdf_list_view, container, false)
    }

    /**
     * Runs the PDF fetching service, if none is running already
     *
     * @param item the item to run the fetch on
     */
    private fun runService(item: FetchResult, modal: Boolean) {
        if (pdfService != null && pdfService!!.isActive) {
            Log.d("PDF Service", "Cancelling before start")
            if (pdf_list_swipe_refresh != null) {
                Log.d("PDF Service", "Re-enabling pull down")
                pdf_list_swipe_refresh.isRefreshing = false
            }
            return
        }

        Log.d("PDF Service", "Starting")

        if (modal) {
            http_bar_pdf_list.visibility = View.VISIBLE
        }

        pdfService = PDFService()
        pdfService!!.startService(
            args.folder,
            item.itemUrl,
            this,
            item.itemUrl.endsWith("index.php", true)
        ) //Only look for folders on the index page!
    }

    /**
     * Loads the saved [PDFItem] instances in case of a restoration of state
     * Also called when the screen is rotated
     *
     * @param savedInstanceState The (saved) state of the application
     */
    private fun restoreState(savedInstanceState: Bundle) {
        Log.d("Fetch Fragment Saving", "Restoring bundle")

        val coscName = savedInstanceState.getStringArray("coscNames")
        val imageResource = savedInstanceState.getIntArray("imageResources")
        val itemFile = savedInstanceState.getStringArray("itemFiles")
        val itemUrl = savedInstanceState.getStringArray("itemUrls")
        val pathType = savedInstanceState.getStringArray("pathTypes")

        if (coscName == null || imageResource == null || itemFile == null || itemUrl == null || pathType == null) {
            //If something fails then complain then just run it again
            Log.d("List Fragment Saving", "Bundle restore failed")
            Toast.makeText(context, "Failed to load previous state - listing again", Toast.LENGTH_SHORT).show()
            setRecyclerItems(if (args.listFiles) RefreshNotifyType.CACHE_NAV_NONE else RefreshNotifyType.NAV_NONE)
            return
        }

        val incomingItems = ArrayList<PDFItem>()
        coscName.forEachIndexed { i, it ->
            incomingItems.add(
                PDFItem(
                    imageResource[i],
                    FileNavigatorType.valueOf(pathType[i]),
                    itemFile[i],
                    itemUrl[i],
                    it
                )
            )
        }

        //Load the recycler
        setRecyclerItems(RefreshNotifyType.NONE, incomingItems.toList())
    }

    /**
     * Event handler for saving the state when the user navigates away or rotates the screen
     *
     * @param outState bundle the bundle in which to package any relevant information
     */
    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("List Fragment Saving", "Creating bundle")
        if (adapterItems.isEmpty()) {
            outState.putString("emptyState", if (http_bar_pdf_list.visibility == View.VISIBLE) "fetching" else "empty")
        } else {
            val coscName = Array(adapterItems.size) {
                adapterItems[it].coscName
            }
            val imageResource = IntArray(adapterItems.size) {
                adapterItems[it].imageResource
            }
            val itemFile = Array(adapterItems.size) {
                adapterItems[it].itemFile
            }
            val itemUrl = Array(adapterItems.size) {
                adapterItems[it].itemUrl
            }

            val pathType = Array(adapterItems.size) {
                adapterItems[it].pathType.name
            }

            outState.putStringArray("coscNames", coscName)
            outState.putIntArray("imageResources", imageResource)
            outState.putStringArray("itemFiles", itemFile)
            outState.putStringArray("itemUrls", itemUrl)
            outState.putStringArray("pathTypes", pathType)
        }

        super.onSaveInstanceState(outState)
    }

    /**
     * Sets the [adapterItems] and marks it as being correct
     * Also updates the UI if it exists
     *
     * @param links The items to add
     */
    fun notify(links: List<PDFItem>?) {
        if (links == null) {
            Toast.makeText(context, "Failed to fetch links - try refreshing", Toast.LENGTH_SHORT).show()
            setRecyclerItems(RefreshNotifyType.NONE, emptyList())
            hasItems = true
            return
        }

        //Update the UI on completion of paper fetch
        setRecyclerItems(if (http_bar_pdf_list == null || http_bar_pdf_list.visibility == View.INVISIBLE) RefreshNotifyType.REFRESH_NONE else RefreshNotifyType.NAV_NONE, links)
        if (http_bar_pdf_list != null) {
            http_bar_pdf_list.visibility = View.INVISIBLE
        }
        hasItems = true
    }

    /**
     * Create the items to display upon starting the fragment.
     * All items are pulled from one directory
     *
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view_list.layoutManager = LinearLayoutManager(context)
        recycler_view_list.setHasFixedSize(true)

        //If we're just listing the papers don't allow any form of refresh
        if (args.paperCode == null) {
            pdf_list_swipe_refresh.isEnabled = false
            setRecyclerItems(RefreshNotifyType.NAV_PAPERS) //Load from folder
            return
        }

        //Try to load the meta file
        val item: FetchResult? = try {
            PDFOperations.loadMetaFile(args.folder)
        } catch (e: IOException) {
            null
        } catch (e: FileNotFoundException) {
            null
        }

        //If we fail then exit
        if (item == null) {
            Toast.makeText(context, "Couldn't load file list", Toast.LENGTH_SHORT).show()
            fragmentManager?.popBackStack()
            return
        }

        //Set pull down listener
        pdf_list_swipe_refresh.setOnRefreshListener {
            Log.d("Refresh Pulldown", "Starting?")
            //runService handles if the service is running
            runService(item, false)
        }

        //If we don't have a saved instance state, try first to load any data from a previous state, otherwise just fetch it again
        //If we're listing then just list
        if (savedInstanceState == null) {
            Log.d("Fetch Activity Created", "Fetching")
            when {
                hasItems -> {
                    setRecyclerItems(RefreshNotifyType.NONE, adapterItems)
                }
                args.listFiles -> {
                    setRecyclerItems(RefreshNotifyType.CACHE_NAV_NONE) //Load from folder
                }
                else -> {
                    runService(item, true)
                }
            }
        }
        else  { //Otherwise, try to load the state from the saved instance state
            //Restore state
            Log.d("Fetch Activity Created", "Restoring from saved state")

            //We want to handle restoring state when there are no items
            when (savedInstanceState.getString("emptyState")) {
                null -> { //If it's null, we have items
                    restoreState(savedInstanceState)
                }
                "fetching" -> { //We are still fetching here
                    Log.d("Restoring", "Still fetching")
                    http_bar_pdf_list.visibility = View.VISIBLE
                }
                "empty" -> { //We have no items
                    setRecyclerItems(RefreshNotifyType.NONE, adapterItems)
                }
            }
        }
    }

    /**
     * Sets the items in the recycler view by enumerating the directory
     *
     * @param cacheMessage - Whether to send a toast about having no files present in the cache or about no files at all
     */
    private fun setRecyclerItems(cacheMessage: RefreshNotifyType) {
        //Generate the file list
        setRecyclerItems(cacheMessage, generateFolderList(args.folder))
    }

    /**
     * Sets the items in the recycler view from some given [PDFItem]s
     *
     * @param cacheMessage - Whether to send a toast about having no files present
     * @param list - The [PDFItem]s to add
     */
    private fun setRecyclerItems(cacheMessage: RefreshNotifyType, list: List<PDFItem>) {
        //If we are called from a coroutine which is running with a destroyed fragment
        //e.g. from after navigation we don't want to do anything here
        if (recycler_view_list == null) {
            return
        }

        adapterItems = list

        //Only send the message if we want to
        if (list.isEmpty()) {
            Toast.makeText(context, cacheMessage.message, Toast.LENGTH_SHORT).show()
        }

        //Preserve the view state (i.e. the scroll position)
        val recyclerViewState = recycler_view_list.layoutManager?.onSaveInstanceState()

        //Add to the recycler, with openFile as the click event handler
        recycler_view_list.adapter = PDFItemRecyclerViewAdapter(list) { item ->
            openFile(item)
        }

        //Restore the view state
        recycler_view_list.layoutManager?.onRestoreInstanceState(recyclerViewState)

        //If we came here from a refresh operation then make sure to note that we're done
        if (pdf_list_swipe_refresh.isRefreshing) {
            Log.d("Refresh Pulldown", "Stopping refresh")
            pdf_list_swipe_refresh.isRefreshing = false
        }
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
            //Try to load the meta file corresponding to the input filename
            if (!it.absolutePath.endsWith(".meta")) {
                return null
            }
            return PDFOperations.loadMetaFile(it.absolutePath.substringBeforeLast(".meta"))
        } catch (e: IOException) { //Silently ignore errors - the user can just refresh if something's missing
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
        val nextNav = args.navName + "/" + item.coscName
        when (item.pathType) {
            FileNavigatorType.PDF -> {
                Log.d("PDF View (URL)", item.itemUrl)
                Log.d("PDF View (Folder)", item.itemFile)
                //If is's a PDF then open the PDF in the PDFViewFragment
                val action = PDFListFragmentDirections.actionPDFListFragmentToPDFViewFragment(item.itemFile, item.itemUrl, nextNav)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            FileNavigatorType.FOLDER -> {
                //Just go to where the PDFItem tells us
                val action = PDFListFragmentDirections.actionPDFListFragmentSelf(
                    item.itemFile,
                    args.paperCode ?: item.coscName,
                    args.listFiles,
                    nextNav
                )
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            FileNavigatorType.MARKS -> {
                //For the marks we want to go to the marks fragment
                Log.d("Mark View GET", item.itemUrl)
                NavHostFragment.findNavController(nav_host_fragment).navigate(PDFListFragmentDirections.actionPDFListFragmentToMarkViewFragment(item.itemUrl, nextNav))
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

                if (fetchedLinks == null) {
                    inFragment.notify(null)
                    return@launch
                }

                val pdfs = PDFOperations.generatePdfItems(fetchedLinks)

                withContext(Dispatchers.Main) {
                    if (inFragment.http_bar_pdf_list != null) {
                        inFragment.http_bar_pdf_list.visibility = View.INVISIBLE
                    }
                    //If we are just listing files don't complain if we don't find any - they will see that already
                    inFragment.notify(pdfs)
                }

                var allOk = true

                //If we do have links, download them
                if (fetchedLinks.size > 0) {
                    fetchedLinks.forEach {
                        if (PDFOperations.downloadPDF(it) == null) {
                            allOk = false
                        }
                    }
                }

                //If some PDFs don't download then notify the user but otherwise ignore them
                if (!allOk) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(inFragment.context, "Some PDFs failed to download", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}

