/*
Copyright (C) 2020 Burnie Lorimer, Damian Soo, Garth Wales, Louis Whitburn

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
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_fetch.*
import kotlinx.android.synthetic.main.fragment_item_list.recycler_view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import org.jsoup.nodes.Document
import java.io.*
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
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

    private var visited = false

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_list_view, container, false)
    }

    /**
     * Create the items to display upon starting the fragment.
     * All items are pulled from one directory
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycler_view.layoutManager = LinearLayoutManager(context)
        recycler_view.setHasFixedSize(true)

        if (args.courseUrl == null) {
            setRecyclerViewItems(true)
        } else {
            if (!visited) {
                http_bar.visibility = View.VISIBLE
                PDFService.startService(
                    args.courseUrl!!,
                    args.courseUrl!! + "/" + args.courseContentPage!!,
                    args.dir,
                    this,
                    args.courseContentPage.isNullOrBlank() //Only look for folders on the index page!
                )
            }
            setRecyclerViewItems(false)
        }

        //Only download once
        visited = true
    }

    /**
     * Sets the items in the recycler view by enumerating the directory
     *
     * @param notify - Whether to send a toast about having no files present
     */
    private fun setRecyclerViewItems(notify: Boolean) {
        //Generate the file list
        val list = generateList()

        //Only send the message if we want to
        if (list.isEmpty() and notify) {
            Toast.makeText(context, "No content yet", Toast.LENGTH_SHORT).show()
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

            //Give a pretty path to the file names
            //i.e. replace lectures.php / tutorials.php etc, and trim the .pdf if it is a PDF
            val prettyPath : String = when (fileType) {
                FileNavigatorType.FOLDER -> when (it.name) {
                    "lectures.php" -> "Lectures"
                    "tutorials.php" -> "Tutorials"
                    "labs.php" -> "Labs"
                    "assignments.php" -> "Assignments"
                    "assessment.php" -> "Assessment"
                    "resources.php" -> "Resources"
                    "staff.php" -> "Staff"
                    "marks.php" -> "Marks"
                    else -> if (it.name.endsWith(".php")) {
                        it.name.substring(0, it.name.length-4)
                     } else {
                        it.name
                    }
                }
                FileNavigatorType.PDF -> if (it.name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                    it.name.substring(0,it.name.length-4)
                } else {
                    it.name
                }
            }

            list += PDFItem(drawable, it.name, prettyPath, fileType)
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
                //But first check if this is based on a URL or just folders
                val action = if (args.courseUrl.isNullOrEmpty()) {
                    PDFListFragmentDirections.actionPDFListFragmentSelf(args.dir + "/" + item.pathName, null, null)
                } else {
                    PDFListFragmentDirections.actionPDFListFragmentSelf(args.dir + "/" + item.pathName, args.courseUrl, args.courseContentPage + "/" + item.pathName)
                }
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
        }
    }

    /**
     * Coordinates fetching and downloading PDF files from a url.
     * Use [PDFService.startService] to begin coroutines will fetch,
     * and then download each PDF.
     */
    object PDFService {
        data class FetchResult(val url: String, val type: FileNavigatorType)
        /**
         * The coroutine scope for this coroutine service
         */
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Starts fetching and downloading PDFs.
         * Utilises coroutines to run networking on a separate threads to UI.
         *
         * @see fetchLinks for link logic
         * @see downloadPDF for downloading logic
         *
         * @param url The url to search for pdf links from
         * @param storage The location to store PDFs in
         * @param inFragment The instance of [FetchFragment], to call in class functions
         */
        fun startService(baseUrl: String, url: String, storage: String, inFragment: PDFListFragment, doFolders: Boolean) {
            //Launch coroutine
            coroutineScope.launch {
                //Fetch the links
                val fetchedlinks = fetchLinks(url, doFolders)

                withContext(Dispatchers.Main) {
                    if (inFragment.http_bar != null) {
                        inFragment.http_bar.visibility = View.GONE
                    }
                }

                //If we do have links, download them
                if (fetchedlinks.size > 0) {
                    downloadPDF(baseUrl, fetchedlinks, storage, inFragment)
                }
            }
        }

        /**
         * Retrieve links from a table containing href elements.
         *
         * @param url The url to search for pdf links from
         *
         * @return The list of urls for each PDF on the page
         */
        private fun fetchLinks(url: String, doFolders: Boolean): ArrayList<FetchResult> {
            val links = ArrayList<FetchResult>()
            try {
                val document: Document = Jsoup.connect(url).get()

                //PDF links
                document.select("tr a").forEach { it ->
                    val href = it.attr("href")
                    if (href.endsWith(".pdf")) {
                        Log.d("Fetched Link", href)
                        links += FetchResult(href, FileNavigatorType.PDF)
                    }
                }

                if (doFolders) {
                    //Nav links
                    document.select("div#coursepagenavmenu li a").forEach { it ->
                        val href = it.attr("href")
                        val trimHref = if (href.startsWith("./")) {
                            href.substring(2)
                        } else {
                            href
                        }

                        //Don't fetch the home page (we're already there)
                        if (!trimHref.equals("index.php")) {
                            links += FetchResult(trimHref, FileNavigatorType.FOLDER)
                            Log.d("Fetched Link (URL)", trimHref)
                        }
                    }
                }

            } catch (e: MalformedURLException) {
                //TODO: Do something here
                e.printStackTrace()
            } catch (e: HttpStatusException) {
                //TODO: Do something here
                e.printStackTrace()
            } catch (e: UnsupportedMimeTypeException) {
                //TODO: Do something here
                e.printStackTrace()
            } catch (e: SocketTimeoutException) {
                //TODO: Do something here
                e.printStackTrace()
            } catch (e: IOException) {
                //TODO: Do something here
                e.printStackTrace()
            }

            return links
        }

        /**
         * Download each PDF from list of url endings retrieved from [fetchLinks].
         *
         * @param baseUrl The URL relative to which each item in links refers to a PDF
         * @param links ArrayList of the PDFs or URLs that correspond to the endings of urls to PDFs
         * @param storage The location to store PDFs in
         * @param inFragment The instance of [FetchFragment], to call in class functions
         */
        private suspend fun downloadPDF(baseUrl: String, links: ArrayList<FetchResult>, storage: String, inFragment: PDFListFragment) {
            Log.d("PDF Downloading", baseUrl)

            withContext (Dispatchers.Main) {
                Toast.makeText(
                    inFragment.activity,
                    "Background download running",
                    Toast.LENGTH_LONG
                ).show()
            }

            links.forEach{
                //If it's a folder then we just need to create it
                if (it.type == FileNavigatorType.FOLDER) {
                    val dirName = storage + "/" + it.url
                    Log.d("Creating Folder ", dirName)
                    File(dirName).mkdirs()

                    //Make sure to set view items
                    try {
                        withContext(Dispatchers.Main) {
                            //Show downloaded PDF in list
                            inFragment.setRecyclerViewItems(false)
                        }
                    } catch (e: NullPointerException) {
                        //Ignore - we'll keep downloading
                    } catch (e: IllegalStateException) {
                        //Ignore - we'll keep downloading
                    }
                    return@forEach
                }

                val pdfUrl = it.url

                //Sanitise input - in case of a blank href
                if (pdfUrl.isBlank()) {
                    return@forEach
                }

                //Generate the URL for where the PDF is
                val url = URL("$baseUrl/$pdfUrl")

                //Get the filename for the PDF
                val fname = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1)

                //Create a buffer for the HTTP requests
                val buf = ByteArray(1024)

                //Make the folder to save the lecture PDF into
                File(storage).mkdirs()

                //Create the file to save the lecture PDF into
                val outFile  = File(storage, fname)
                Log.d("PDF Saving", outFile.absolutePath)
                try {
                    outFile.createNewFile()
                } catch (e: IOException) {
                    //TODO: Something here
                    e.printStackTrace()
                    return@forEach
                }

                try {
                    val outStream = BufferedOutputStream(
                        FileOutputStream(outFile)
                    )

                    Log.d("PDF Downloading", url.toExternalForm())

                    //Open a HTTP connection
                    val conn = url.openConnection()
                    val inStream = conn.getInputStream()

                    //Read the result and write it to file
                    var byteRead = inStream.read(buf)
                    while (byteRead != -1) {
                        outStream.write(buf, 0, byteRead)
                        byteRead = inStream.read(buf)
                    }

                    //Close the file streams
                    inStream.close()
                    outStream.close()
                } catch (e: FileNotFoundException) {
                    //TODO: Something here
                    e.printStackTrace()
                    return@forEach
                } catch (e: IOException) {
                    //TODO: Something here
                    e.printStackTrace()
                    return@forEach
                }

                try {
                    withContext(Dispatchers.Main) {
                        //Show downloaded PDF in list
                        inFragment.setRecyclerViewItems(false)
                    }
                } catch (e: NullPointerException) {
                    //Ignore - we'll keep downloading
                } catch (e: IllegalStateException) {
                    //Ignore - we'll keep downloading
                }
            }
        }
    }
}

