package com.otago.open

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements
import android.util.Log
import android.content.Context
import android.content.ContextWrapper
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.android.synthetic.main.fragment_fetch.*
import kotlinx.android.synthetic.main.fragment_fetch.recycler_view
import kotlinx.android.synthetic.main.fragment_item_list.*

/**
 * A simple [Fragment] subclass.
 * Use the [FetchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FetchFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    /**
     * Entry point of[FetchFragment].
     * Starts [PDFService] with hard coded addresses and storage locations.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fetch, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        CourseService.startService(this)

    }

    fun selectType(item: CourseItem) {
        //will do something more interesting here later
        //And this will take a paramater to replace "/lectures.php"
        //And to replace "/lec"
        http_bar.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE
        PDFService.startService(item.courseUrl, item.courseUrl + "/lectures.php", ContextWrapper(context).filesDir.absolutePath + "/" + item.courseCode + "/lec", this);
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
        var links = ArrayList<CourseItem>()
        //URL for the COSC papers page
        val document: Document =
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
                val courseUrl = "https://cs.otago.ac.nz/" + courseCode.toLowerCase()
                val courseName = linkIt.html()
                //TODO: Update icon here
                links.add(CourseItem(R.drawable.ic_folder, courseName, courseUrl, courseCode))
                Log.d("Added course", courseName + "with URL " + courseUrl)
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
     * @return list of urls for each PDF on the page, retrieved from [fetchLinks]
     */
    fun startService(baseUrl: String, url : String, storage : String, inFragment : FetchFragment) {
        Log.d("startService", "begin PDFService")
        var links = ArrayList<String>()
        coroutineScope.launch {
            links = fetchLinks(url)
            if(links.size > 0) {
                val download = downloadPDF(baseUrl, links, storage)
            }
            Log.d("startService", "exit coroutine scope")
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
    private suspend fun fetchLinks(url : String): ArrayList<String> {
        val links = ArrayList<String>()
        try {
            Log.d("fetchLinks", url)
            val document: Document = Jsoup.connect(url).get()
            val elements = document.select("tr a")
            for(i in 0 until elements.size) {
                Log.d("fetchLinks", elements[i].attr("href"))
                links += elements[i].attr("href") // not sure if this works or not
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return links
    }

    /**
     * Download each PDF from list of url endings retrieved from [fetchLinks].
     * @param links ArrayList of urls that correspond to the endings of urls to PDFs
     * @param storage the location to store PDFs in
     */
    private suspend fun downloadPDF(baseUrl: String, links : ArrayList<String>, storage: String) {
        Log.d("pdf downloading", baseUrl)
        links.forEach {
            if (it.isNullOrBlank()) {
                return@forEach
            }
            val url = URL(baseUrl + "/" + it)
            val fname = it.substring( it.lastIndexOf('/')+1, it.length);
            val buf = ByteArray(1024)
            var byteRead: Int
            val p = File(storage)
            p.mkdirs()
            val f = File(storage, fname)
            f.createNewFile()
            Log.d("pdf saving", storage + "/" + fname)
            var outStream = BufferedOutputStream(
                FileOutputStream(storage + "/" + fname)
            )

            Log.d("pdf downloading", url.toExternalForm())
            val conn = url.openConnection()
            var inStream = conn.getInputStream()
            byteRead = inStream!!.read(buf)
            while (byteRead != -1) {
                outStream.write(buf, 0, byteRead)
                byteRead = inStream.read(buf)
            }
            inStream.close()
            outStream.close()
        };
        Log.d("downloadPDF", "reached")
    }
}