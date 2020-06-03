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
import android.widget.TextView
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlinx.android.synthetic.main.fragment_fetch.*

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
        PDFService.startService("cs.otago.ac.nz/cosc241/lectures.php", "/data/data/com.otago.open/files/cosc241", this);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fetch, container, false)
    }

    /**
     * Simple message upon finishing fetching.
     */
    fun finished() {
        textFetch.text = "Fetch complete";
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
    fun startService(url : String, storage : String, inFragment : FetchFragment): ArrayList<String> {
        Log.d("startService", "begin PDFService")
        var links = ArrayList<String>()
        coroutineScope.launch {
            links = fetchLinks(url)
            if(links.size > 0) {
                val download = downloadPDF(links, storage/*, context*/)
            }
            inFragment.finished()
            Log.d("startService", "exit coroutine scope")
        }

        return links
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
            val document: Document = Jsoup.connect("https://" + url).get()
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
    private suspend fun downloadPDF(links : ArrayList<String>, storage: String/*, context: Context*/) {
        links.forEach {
            val url = URL("https://cs.otago.ac.nz/cosc241/" + it)
            val fname = it.substring( it.lastIndexOf('/')+1, it.length);
            val buf = ByteArray(1024)
            var byteRead: Int
            val p = File(storage)
            p.mkdirs()
            val f = File(storage, fname)
            f.createNewFile()
            var outStream = BufferedOutputStream(
                FileOutputStream(storage + "/" + fname)
            )

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