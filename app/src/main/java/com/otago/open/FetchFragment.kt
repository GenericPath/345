package com.otago.open

import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_fetch.*
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
import java.util.*
import kotlin.collections.ArrayList

/**
 * The class for the fragment to fetch the courses
 */
class FetchFragment : Fragment() {
    /**
     * Entry point of [FetchFragment].
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     * @return The layout generated from the XML
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fetch, container, false)
    }

    /**
     * Handles the creation of this activity, and starts the coroutine service to list the courses
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        CourseService.startService(this)
    }

    /**
     * Currently just downloads the lecture PDFs and moved to the [PDFListFragment]
     * TODO: Check tutorials / lectures / etc
     * @param item The course to delve into
     */
    fun selectType(item: CourseItem) {
        //will do something more interesting here later


        //We want to hide the recycler and show the progress bar while we download the PDFs
        http_bar.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE

        //And this will take a paramater to replace "/lectures.php"
        //And to replace "/lec"
        PDFService.startService(item.courseUrl, item.courseUrl + "/lectures.php", ContextWrapper(context).filesDir.absolutePath + "/" + item.courseCode + "/lec", this)
    }
}

object CourseService {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun startService(inFragment: FetchFragment) {
        coroutineScope.launch {
            runService(inFragment)
        }
    }

    private suspend fun runService(inFragment: FetchFragment) {
        val links = ArrayList<CourseItem>()
        //URL for the COSC papers page
        val document: Document =
            //TODO: Fix inappropriate blocking call
            Jsoup.connect("https://www.otago.ac.nz/computer-science/study/otago673578.html")
                .get()
        //Content div
        val contents = document.select("#content")
        contents.forEach { docIt ->
            //Links
            val hrefs = docIt.select("a[href]")
            hrefs.forEach { linkIt ->
                val infoUrl = linkIt.attr("href")
                val courseCode = infoUrl.substring(infoUrl.length - 7)
                val courseUrl = "https://cs.otago.ac.nz/" + courseCode.toLowerCase(Locale.ROOT)
                val courseName = linkIt.html()
                //TODO: Update icon here
                links.add(CourseItem(R.drawable.ic_folder, courseName, courseUrl, courseCode))
                Log.d("Added Course", courseName + "with URL " + courseUrl)
            }
        }

        withContext(Dispatchers.Main) {
            inFragment.http_bar.visibility = View.GONE
            inFragment.recycler_view.adapter =
                CourseItemRecyclerViewAdapter(links.toList()) { link -> inFragment.selectType(link) }
            inFragment.recycler_view.layoutManager = LinearLayoutManager(inFragment.context)
            inFragment.recycler_view.setHasFixedSize(true)
        }
    }
}

/**
 * Coordinates fetching and downloading PDF files from a url.
 * Use [PDFService.startService] to begin coroutines will fetch,
 * and then download each PDF.
 */
object PDFService {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Starts fetching and downloading PDFs.
     * Utilises coroutines to run networking on a separate threads to UI.
     *
     * @see fetchLinks for link logic
     * @see downloadPDF for downloading logic
     *
     * @param url the url to search for pdf links from
     * @param storage the location to store PDFs in
     * @param inFragment the instance of FetchFragment, to call in class functions
     */
    fun startService(baseUrl: String, url : String, storage : String, inFragment : FetchFragment) {
        //Launch coroutine
        coroutineScope.launch {
            //Fetch the links
            val links = fetchLinks(url)

            //If we don't have any links, don't do anything
            if(links.size == 0) {
                withContext(Dispatchers.Main) {
                    inFragment.http_bar.visibility = View.GONE
                    inFragment.recycler_view.visibility = View.VISIBLE
                    Toast.makeText(inFragment.activity, "No PDFs for that course - maybe it's not running or it went to blackboard due to COVID-19",
                        Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            //If we do have links, download them
            downloadPDF(baseUrl, links, storage)

            //When finished, navigate to the PDFs
            withContext(Dispatchers.Main) {
                val action = FetchFragmentDirections.actionFetchFragmentToPDFListFragment(storage)
                NavHostFragment.findNavController(inFragment.nav_host_fragment).navigate(action)
            }
        }
    }

    /**
     * Retrieve links from a table containing href elements.
     * @param url the url to search for pdf links from
     * @return list of urls for each PDF on the page
     */
    private fun fetchLinks(url : String): ArrayList<String> {
        val links = ArrayList<String>()
        try {
            val document: Document = Jsoup.connect(url).get()
            val elements = document.select("tr a")
            for(i in 0 until elements.size) {
                Log.d("Fetched Link", elements[i].attr("href"))
                links += elements[i].attr("href")
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
     * @param baseUrl the URL relative to which each item in links refers to a PDF
     * @param links ArrayList of urls that correspond to the endings of urls to PDFs
     * @param storage the location to store PDFs in
     */
    private fun downloadPDF(baseUrl: String, links : ArrayList<String>, storage: String) {
        Log.d("pdf downloading", baseUrl)
        links.forEach {
            //Sanitise input - in case of a blank href
            if (it.isBlank()) {
                return@forEach
            }

            //Generate the URL for where the PDF is
            val url = URL("$baseUrl/$it")

            //Create a buffer for the HTTP requests
            val buf = ByteArray(1024)

            //Make the folder and file to save the lecture PDF into
            File(storage).mkdirs()
            try {
                File(storage, url.file).createNewFile()
            } catch (e: IOException) {
                //TODO: Something here
                e.printStackTrace()
                return@forEach
            }

            Log.d("pdf saving", "$storage/" + url.file)
            try {
                val outStream = BufferedOutputStream(
                    FileOutputStream("$storage/" + url.file)
                )

                Log.d("pdf downloading", url.toExternalForm())

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
        }
        Log.d("Download PDF", "reached")
    }
}