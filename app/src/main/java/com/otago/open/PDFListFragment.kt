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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import org.jsoup.nodes.Document
import java.io.*
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.file.Files
import java.util.*
import kotlin.collections.ArrayList

/**
 * A [Fragment] that generates each [PDFItem] to display.
 */
class PDFListFragment : Fragment() {
    data class FetchResult(val paperFolder: String, val paperUrl: String, val pathSubName: String, val type: FileNavigatorType)

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

        if (args.listFiles) {
            //If we are just listing files (no HTTP) then we can just set the recycler items
            //Since we already have folders (or not) we don't need to complain
            setRecyclerViewItems(false)
        } else if (!visited) {
            //Block UI while the available files are fetched (but only if none exist already)
            http_bar.visibility = View.VISIBLE
        }

        //To check network status - we don't want to waste mobile data
        val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        //If we already have some files cached, and the network is metered then we *don't* want to re-download the PDFs
        //Otherwise, we may as well re-download them. Since we don't delete old things this isn't a problem
        //But don't download again in the same app instance (prevents re-downloading on navigation)
        //TODO: Better invalidation logic (user setting), handle new and old PDFs being different (in PDFService)
        if (!(args.listFiles and cm.isActiveNetworkMetered) and !visited) {
            PDFService.startService(
                args.paperFolder,
                args.paperUrl,
                args.paperSubName,
                this,
                args.paperSubName.isBlank()
            ) //Only look for folders on the index page!

            //If we're going to actually do something show a toast
            Toast.makeText(
                activity,
                "Background download running",
                Toast.LENGTH_LONG
            ).show()
        }

        //Only download once
        visited = true
    }

    /**
     * Sets the items in the recycler view by enumerating the directory
     *
     * @param notify - Whether to send a toast about having no files present
     */
    @Suppress("SameParameterValue") //TODO: Check this - needed to make the warning go away but the warning doesn't seem to be true?
    private fun setRecyclerViewItems(notify: Boolean) {
        //Generate the file list
        setRecyclerViewItems(notify, generateFolderList())
    }

    /**
     * Sets the items in the recycler view from some given [PDFItem]s
     *
     * @param notify - Whether to send a toast about having no files present
     * @param list - The [PDFItem]s to add
     */
    private fun setRecyclerViewItems(notify: Boolean, list: List<PDFItem>) {
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
     * Creates list of [PDFItem] items from what is present in the current directory (paper folder / sub name)
     *
     * @return The list of [PDFItem]s in the specified directory
     */
    private fun generateFolderList(): List<PDFItem> {
        //Get the "current folder"
        val currentFolder = File(args.paperFolder + "/" + args.paperSubName)
        val result = ArrayList<FetchResult>()
        val files = currentFolder.listFiles()

        //Show a message if there are no files
        if (files?.size == 0) {
            return emptyList()
        }

        //Only select PDFs and directories
        files?.forEach {
            if (it.extension.toLowerCase(Locale.ROOT) == "pdf") {
                result += FetchResult(args.paperFolder, args.paperUrl, it.absolutePath.replaceFirst(args.paperFolder, ""), FileNavigatorType.PDF)
            } else if (it.isDirectory) {
                result += FetchResult(args.paperFolder, args.paperUrl, it.absolutePath.replaceFirst(args.paperFolder, ""), FileNavigatorType.FOLDER)
            }
        }

        //Show a message if there are nothing of interest in the folder
        if (result.size == 0) {
            return emptyList()
        }

        //Create a list of PDF (or folder) items from out FetchItems
        return PDFService.generatePdfItems(result)
    }

    /**
     * Opens item in the directory, either PDF or folder.
     * @param item The [PDFItem] to display
     */
    private fun openFile(item: PDFItem) {
        when (item.pathType) {
            FileNavigatorType.PDF -> {
                //If is's a PDF then open the PDF in the PDFViewFragment
                val action = PDFListFragmentDirections.actionPDFListFragmentToPDFViewFragment(item.paperFolder + "/" + item.pathSubName, item.paperUrl + "/" + item.pathSubName)
                NavHostFragment.findNavController(nav_host_fragment).navigate(action)
            }
            FileNavigatorType.FOLDER -> {
                //Otherwise navigate to the subdirectory
                val action = when {
                    args.paperCode.isBlank() -> {
                        //If the paper code is blank then we are just listing paper folders
                        //So we want to "guess" the URL and paper code, and set them before moving on
                        val codeGuess = item.pathSubName.substringAfterLast('/').toLowerCase(Locale.ROOT)
                        val urlGuess = "https://cs.otago.ac.nz/$codeGuess"
                        PDFListFragmentDirections.actionPDFListFragmentSelf(
                            item.paperFolder,
                            urlGuess,
                            item.pathSubName,
                            codeGuess,
                            args.listFiles
                        )
                    }
                    item.pathSubName.endsWith("marks.php") -> {
                        //For the marks we want to go to the marks frame
                        //Sometimes makes a // as apposed to a / here but it seems to work
                        val postUrl = item.paperUrl + "/" + item.pathSubName
                        Log.d("Mark View POST", postUrl)
                        PDFListFragmentDirections.actionPDFListFragmentToMarkViewFragment(postUrl)
                    }
                    else -> {
                        //Otherwise just go to where the PDFItem tells us
                        PDFListFragmentDirections.actionPDFListFragmentSelf(
                            item.paperFolder,
                            item.paperUrl,
                            item.pathSubName,
                            args.paperCode,
                            args.listFiles
                        )
                    }
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
         * @see PDFItem for more details on what the paperFolder, paperUrl, and paperSubName is
         *
         * @param paperFolder The folder in which the paper's downloaded content is stored
         * @param paperUrl The URL at which course PDFs are located
         * @param paperSubName The resource over which we are enumerating (e.g. looking for folders in index.php or PDFs in lectures.php)
         * @param inFragment The instance of [PDFListFragment], to call in class functions
         * @param doFolders Whether we want to include folders. Currently this should only be true if paperSubName is "" or "index.php" due to the COSC website navigation structure
         */
        fun startService(paperFolder: String, paperUrl: String, paperSubName: String, inFragment: PDFListFragment, doFolders: Boolean) {
            //Launch coroutine
            coroutineScope.launch {
                //Fetch the links
                val fetchedLinks = fetchLinks(paperFolder, paperUrl, paperSubName, doFolders)
                val pdfs = generatePdfItems(fetchedLinks)

                withContext(Dispatchers.Main) {
                    if (inFragment.http_bar != null) {
                        inFragment.http_bar.visibility = View.GONE
                    }
                    //If we are just listing files don't complain if we don't find any - they will see that already
                    inFragment.setRecyclerViewItems(!inFragment.args.listFiles, pdfs)
                }

                //If we do have links, download them
                if (fetchedLinks.size > 0) {
                    downloadPDF(fetchedLinks)
                }

            }
        }

        /**
         * Creates [PDFItem]s from [FetchResult]s by deciding on the pretty name and the icon to use.
         *
         * @param fetched The fetched / chosen PDFs / folders
         *
         * @return The processed list of [FetchResult]s as a list of [PDFItem]s
         */
        fun generatePdfItems(fetched: List<FetchResult>) : List<PDFItem> {
            val pdfs = ArrayList<PDFItem>(fetched.size)
            fetched.forEach {
                val drawable = when (it.type) {
                    FileNavigatorType.FOLDER -> R.drawable.ic_folder
                    FileNavigatorType.PDF -> R.drawable.ic_pdf
                }

                val fileName = it.pathSubName.substringAfterLast("/")
                //Give a pretty path to the file names
                //i.e. replace lectures.php / tutorials.php etc, and trim the .pdf if it is a PDF
                val prettyPath: String = when (it.type) {
                    //If it's a folder either replace it with something we know, or just trim the .php off at the end
                    FileNavigatorType.FOLDER -> when (fileName) {
                        "lectures.php" -> "Lectures"
                        "tutorials.php" -> "Tutorials"
                        "labs.php" -> "Labs"
                        "assignments.php" -> "Assignments"
                        "assessment.php" -> "Assessment"
                        "resources.php" -> "Resources"
                        "staff.php" -> "Staff"
                        "marks.php" -> "Marks"
                        else -> if (fileName.endsWith(".php", ignoreCase = true)) {
                            fileName.replace(".php", "", ignoreCase = true)
                        } else {
                            fileName
                        }
                    }
                    //If it's a PDF either just trim the .pdf off at the end
                    FileNavigatorType.PDF -> if (fileName.endsWith(".pdf", ignoreCase = true)) {
                        fileName.replace(".pdf", "", ignoreCase = true)
                    } else {
                        //Otherwise do nothing
                        fileName
                    }
                }

                pdfs.add(PDFItem(drawable, it.type, it.paperFolder, it.paperUrl, it.pathSubName, prettyPath))
            }

            return pdfs
        }

        /**
         * Retrieve links from a table containing href elements.
         *
         * @see PDFItem for more details on what the paperFolder, paperUrl, and paperSubName is
         *
         * @param paperFolder The folder in which the paper's downloaded content is stored
         * @param paperUrl The URL at which course PDFs are located
         * @param paperSubName The resource over which we are enumerating (e.g. looking for folders in index.php or PDFs in lectures.php)
         * @param doFolders Whether we want to include folders. Currently this should only be true if paperSubName is "" or "index.php" due to the COSC website navigation structure
         *
         * @return The list of [FetchResult]s for each PDF (or folder if [doFolders] is true) on the page
         */
        private fun fetchLinks(paperFolder: String, paperUrl: String, paperSubName: String, doFolders: Boolean): ArrayList<FetchResult> {
            val links = ArrayList<FetchResult>()
            try {
                val document: Document = Jsoup.connect("$paperUrl/$paperSubName").get()

                //PDF links
                document.select("tr a").forEach {
                    val href = it.attr("href")
                    if (href.endsWith(".pdf")) {
                        Log.d("Fetched Link", href)
                        links += FetchResult(paperFolder, paperUrl, "$paperSubName/$href", FileNavigatorType.PDF)
                    }
                }

                if (doFolders) {
                    //Nav links
                    document.select("div#coursepagenavmenu li a").forEach {
                        val href = it.attr("href")
                        val trimHref = if (href.startsWith("./")) {
                            href.substring(2)
                        } else {
                            href
                        }

                        //Don't fetch the home page (we're already there)
                        if (trimHref != "index.php") {
                            links += FetchResult(paperFolder, paperUrl, "$paperSubName/$href", FileNavigatorType.FOLDER)
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
         * @param links [ArrayList] of the [FetchResult]s from something like [fetchLinks]
         */
        private fun downloadPDF(links: ArrayList<FetchResult>) {
            links.forEach{
                //If it's a folder then we just need to create it
                if (it.type == FileNavigatorType.FOLDER) {
                    val dirName = it.paperFolder + "/" + it.pathSubName
                    Log.d("Creating Folder ", dirName)
                    File(dirName).mkdirs()

                    return@forEach
                }

                val pdfUrl = it.paperUrl + "/" + it.pathSubName

                //Sanitise input - in case of a blank href
                if (pdfUrl.isBlank()) {
                    return@forEach
                }

                //Generate the URL for where the PDF is
                val url = URL(pdfUrl)

                //Get the filename for the PDF
                val fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1)

                val saveFolder = it.paperFolder + "/" + it.pathSubName.substring(0, it.pathSubName.lastIndexOf('/'))
                //Make the folder to save the lecture PDF into
                File(saveFolder).mkdirs()

                //Create a buffer for the HTTP requests
                val buf = ByteArray(1024)

                //Create the file to save the lecture PDF into
                val outFile  = File(saveFolder, "$fileName.download")
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

                    //If we're happy with the final file then move it into its proper location
                    Files.move(outFile.toPath(), File(saveFolder, fileName).toPath())
                } catch (e: FileNotFoundException) {
                    //TODO: Something here
                    e.printStackTrace()
                    return@forEach
                } catch (e: IOException) {
                    //TODO: Something here
                    e.printStackTrace()
                    return@forEach
                }
            }
        }
    }
}

