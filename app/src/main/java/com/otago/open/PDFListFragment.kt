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
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import java.io.*
import java.lang.Exception
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.collections.ArrayList

/**
 * A [Fragment] that generates each [PDFItem] to display.
 */
class PDFListFragment : Fragment() {
    /**
     * Data class for fetched folders / PDFs from the COSC website
     *
     * @param itemFile When this particular file (indicated vis [itemUrl] should be saved
     * @param itemUrl The URL of the item (folder / PDF) to ferch
     * @param coscName The name of the item on the COSC website
     * @param type The type of the item (folder or PDF etc)
     */
    data class FetchResult(val itemFile: String, val itemUrl: String, val coscName: String, val type: FileNavigatorType)

    /**
     * Args to pass in directory navigated to
     */
    private val args : PDFListFragmentArgs by navArgs()

    /**
     * Whether we have already visited this fragment. This will be true if we come here via the back button
     */
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

        if (args.listFiles || visited) {
            //If we are just listing files (no HTTP) or we have already visited this page
            // then we can just set the recycler items
            //Since we already have folders (or not) we don't need to complain
            setRecyclerViewItems(false)
        } else if (!visited) {
            //Block UI while the available files are fetched (but only if none exist already)
            http_bar.visibility = View.VISIBLE
        }

        var item: FetchResult? = null
        //Try to load the meta file
        try {
            item = PDFService.loadMetaFile(args.folder)
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
        if (!(args.listFiles and cm.isActiveNetworkMetered) and !visited) {
            Log.d("PDF Service", "Starting")
            PDFService.startService(
                args.folder,
                item.itemUrl,
                this,
                item.itemUrl.endsWith("index.php", true)
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
        val currentFolder = File(args.folder)
        val result = ArrayList<FetchResult>()
        val files = currentFolder.listFiles()

        //Show a message if there are no files
        if (files?.size == 0) {
            return emptyList()
        }

        //Only select PDFs and directories
        files?.forEach {
            if (it.absolutePath.endsWith(".meta")) {
                return@forEach
            }
            try {
                result += PDFService.loadMetaFile(it.absolutePath)
            } catch (e: IOException) {
                //TODO: Something
                return@forEach
            } catch (e: FileNotFoundException) {
                //TODO: Something
                return@forEach
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
        PDFService.cancelService()
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
    object PDFService {
        /**
         * The coroutine scope for this coroutine service
         */
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Function to cancel this service, e.g. if we are navigating away
         */
        fun cancelService() {
            coroutineScope.cancel()
        }

        /**
         * Starts fetching and downloading PDFs.
         * Utilises coroutines to run networking on a separate threads to UI.
         *
         * @see fetchLinks for link logic
         * @see downloadPDF for downloading logic
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
                val fetchedLinks = fetchLinks(parentFolder, url, doFolders)
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
         * Gets the resource icon from a [FileNavigatorType]
         *
         * @param navType The [FileNavigatorType] we want an icon for
         *
         * @return The icon's ID
         */
        fun getResourceItem(navType: FileNavigatorType): Int {
            return when (navType) {
                FileNavigatorType.FOLDER -> R.drawable.ic_folder
                FileNavigatorType.PDF -> R.drawable.ic_pdf
                FileNavigatorType.MARKS -> R.drawable.ic_thumb //TODO: New icon
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
                pdfs.add(PDFItem(getResourceItem(it.type), it.type, it.itemFile, it.itemUrl, it.coscName))
            }
            return pdfs
        }

        /**
         * Determines where a [href] on a page given by [onPageUrl] should point, if [onPageUrl] is
         * a file (i.e. no forward slash), otherwise when in the folder given by [onPageUrl] it determines
         * where the sub-resource would be located
         *
         * If [href] starts with a "/" we just return the [href] relative to the COSC website
         *
         * If [href] is a https link we just return it, if it starts with http we make it a https link
         *
         * This method does not have any special handling for any .. or repeated ././
         *
         * @param onPageUrl The page that the link is on, or the folder that the [href] is relative to
         * @param href The href
         *
         * @return Where [href] should point if it appears on a page [onPageUrl], or if it is a sub-resource of a folder [onPageUrl]
         */
        fun determinePath(onPageUrl: String, href: String): String {
            //If it's a full URL just return it
            if (href.startsWith("http")) {
                return if (href.startsWith("https")) {
                    href
                } else {
                    href.replace("http", "https")
                }
            }

            //If it starts with ./ then this is the same as starting without that
            val trimHref = if (href.startsWith("./")) {
                href.replaceFirst("./", "")
            } else {
                href
            }

            return when {
                //If we are relative to a folder, just concatenate the folder and sub-directory
                onPageUrl.endsWith("/") -> {
                    "$onPageUrl/$trimHref"
                }
                //If the href is absolute then just concatenate the COSC website and the href
                trimHref.startsWith('/') -> {
                    return "https://cs.otago.ac.nz$trimHref"
                }
                //Otherwise just trim the filename from the onPageUrl (if there is no parent folder, just return "")
                //And add the href to it
                else -> {
                    onPageUrl.substringBeforeLast("/", "") + "/$trimHref"
                }
            }
        }

        /**
         * Retrieve links from a table containing href elements.
         *
         * @see PDFItem for more details on what the [parentFolder], and [url] are part of
         *
         * @param parentFolder The folder in which the downloaded content will be stored
         * @param url The URL at which we will look for folders and PDFs etc
         * @param doFolders Whether we want to include folders. Currently this should only be true if paperSubName is "" or "index.php" due to the COSC website navigation structure
         *
         * @return The list of [FetchResult]s for each PDF (or folder if [doFolders] is true) on the page
         */
        private fun fetchLinks(parentFolder: String, url: String, doFolders: Boolean): ArrayList<FetchResult> {
            val links = ArrayList<FetchResult>()
            Log.d("Jsoup URL", url)

            try {
                val document = Jsoup.connect(url).get()

                //PDF links
                document.select("a").forEach {
                    val href = it.attr("href")
                    val hrefName = href.substringAfterLast('/')
                    Log.d("Found href (PDF)", href)
                    if (href.endsWith(".pdf")) {
                        Log.d("Fetched Link", href)
                        val newUrl = determinePath(url, href)
                        links += FetchResult(
                            "$parentFolder/$hrefName",
                            newUrl,
                            it.text(),
                            FileNavigatorType.PDF
                        )
                        Log.d("Found PDF (URL)", newUrl)
                    }
                }

                if (doFolders) {
                    //Nav links
                    document.select("div#coursepagenavmenu a").forEach {
                        val href = it.attr("href")
                        val hrefName = href.substringAfterLast('/')
                        Log.d("Found href (folder)", "$url;$href")
                        val newUrl = determinePath(url, href)

                        //Don't fetch the home page (we're already there)
                        //Make sure we are still on the COSC website (check for php at the end)
                        if (newUrl.endsWith(".php") && !newUrl.endsWith("index.php")) {
                            val name = it.text()

                            //If it's a marks page adjust the navigator type accordingly
                            val navType = if (newUrl.endsWith("marks.php")) {
                                    FileNavigatorType.MARKS }
                            else {
                                FileNavigatorType.FOLDER
                            }
                            Log.d("Detected Name", name)
                            links += FetchResult("$parentFolder/$hrefName", newUrl, name, navType)
                            Log.d("Found Folder (URL)", newUrl)
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
         * Creates a meta file for the file indicated via [saveFile] based on the [FetchResult] [it]
         * This creates the file [saveFile].meta
         *
         * @param saveFile The file to create a meta file for
         * @param it The [FetchResult] to (partially) save in the meta file
         */
        private fun createMetaFile(saveFile: String, it: FetchResult) {
            //Open a file to write
            val metaStream = BufferedWriter(PrintWriter(FileOutputStream("$saveFile.meta")))

            //Write the COSC name and URL
            metaStream.write(it.coscName)
            metaStream.newLine()
            metaStream.write(it.itemUrl)
            metaStream.newLine() //THE MOST IMPORTANT LINE IN THIS WHOLE APP!!!!!!!!!!!!!!!!
            //Each time you create a file that doesn't end with a newline Ken Thompson and Dennis Ritchie (RIP) shed a single tear

            //Close the file stream
            metaStream.close()
        }

        /**
         * Creates a meta file for the file indicated via [saveFolder] and [fileName]
         * based on the [FetchResult] [it]
         *
         * This creates the file [saveFolder]/[fileName].meta
         */
        fun createMetaFile(saveFolder: String, fileName: String, it: FetchResult) {
            createMetaFile("$saveFolder/$fileName", it)
        }

        /**
         * Loads a meta file for the resource [assFile] (associated file), i.e. the loaded meta file
         * will be [assFile].meta
         *
         * @param assFile The resource which we want to load a meta file for
         *
         * @return A [FetchResult] for [assFile]
         */
        fun loadMetaFile(assFile: String): FetchResult {
            //Get the file name
            val metaName = "$assFile.meta"

            Log.d("Loading Meta File", metaName)

            //Load
            val reader = BufferedReader(FileReader(metaName))
            val coscName = reader.readLine()
            val url = reader.readLine()

            //Determine nav type
            val navType = when {
                assFile.endsWith(".pdf", true) -> {
                    FileNavigatorType.PDF
                }
                assFile.endsWith("marks.php", true) -> {
                    FileNavigatorType.MARKS
                }
                File(assFile).isDirectory -> {
                    FileNavigatorType.FOLDER
                }
                else -> { //Complain
                    //TODO: ????
                    throw Exception("")
                }
            }

            //Return our "pretend" fetch result
            return FetchResult(assFile, url, coscName, navType)

        }

        /**
         * Downloads a file at [url] to the file [parentDir]/[fileName] using a HTTP GET
         *
         * During the download the file is saved at [parentDir]/[fileName].download before
         * being moved to [parentDir]/[fileName]
         *
         * @param url The file to download
         * @param parentDir Where to save the file
         * @param fileName The name for the downloaded file
         *
         * @return The name of the saved file ([parentDir]/[fileName]) if all succeeded, otherwise null
         */
        fun downloadFile(url: String, parentDir: String, fileName: String): String? {
            //Generate the URL for where the PDF is
            val resUrl = URL(url)

            //Make the folder to save the lecture PDF into
            File(parentDir).mkdirs()

            //Create a buffer for the HTTP requests
            val buf = ByteArray(1024)

            //Create the file to save the lecture PDF into
            val outFile  = File(parentDir, "$fileName.download")
            Log.d("PDF Saving", outFile.absolutePath)
            try {
                outFile.createNewFile()
            } catch (e: IOException) {
                //TODO: Something here
                e.printStackTrace()
                return null
            }

            try {
                val outStream = BufferedOutputStream(
                    FileOutputStream(outFile)
                )

                Log.d("PDF Downloading", resUrl.toExternalForm())

                //Open a HTTP connection
                val conn = resUrl.openConnection()
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
                Files.move(outFile.toPath(), File(parentDir, fileName).toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)

                Log.d("Successfully Saved", "$parentDir/$fileName")
                return "$parentDir/$fileName"
            } catch (e: FileNotFoundException) {
                //TODO: Something here
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                //TODO: Something here
                e.printStackTrace()
                return null
            }
        }

        /**
         * Download each PDF from list of url endings retrieved from [fetchLinks].
         *
         * @param links [ArrayList] of the [FetchResult]s from something like [fetchLinks]
         */
        private fun downloadPDF(links: ArrayList<FetchResult>) {
            links.forEach{
                //Common stuff good to know
                val parentDir = it.itemFile.substringBeforeLast('/', "")
                val fileName = it.itemFile.substringAfterLast('/', "")

                //If it's a folder then we just need to create it
                if (it.type == FileNavigatorType.FOLDER || it.type == FileNavigatorType.MARKS) {
                    Log.d("Creating Folder ", it.itemFile)
                    try {
                        File(it.itemFile).mkdirs()

                        createMetaFile(parentDir, fileName, it)

                    } catch (e: FileNotFoundException) {
                        //TODO: Something here
                        e.printStackTrace()
                        return@forEach
                    } catch (e: IOException) {
                        //TODO: Something here
                        e.printStackTrace()
                        return@forEach
                    }
                    return@forEach
                }

                //Sanitise input - in case of a blank href
                if (it.itemUrl.isBlank()) {
                    return@forEach
                }

                val outFile = downloadFile(it.itemUrl, parentDir, fileName)

                if (outFile != null) {
                    Log.d("Creating Download Meta-File", outFile)
                    createMetaFile(outFile, it)
                }
            }
        }
    }
}

