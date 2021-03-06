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

//https://github.com/barteksc/AndroidPdfViewer
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_pdf_view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

/**
 * A [Fragment] to view PDFs.
 * Based on https://github.com/barteksc/AndroidPdfViewer
 */
class PDFViewFragment : Fragment() {
    /**
     * Args to pass in the PDF to view
     */
    private val args : PDFViewFragmentArgs by navArgs()

    private var filePath : File? = null

    /**
     * Entry point of [PDFViewFragment]
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdf_view, container, false)
    }

    /**
     * Loads a PDF from the local filesystem
     *
     * @param file The file to load
     * @param pdfView The [PDFView] in which to render the PDF
     */
    private fun showPdf(file: File, pdfView: PDFView) {
        //Displays a PDF
        Log.d("View PDF", file.absolutePath)
        filePath = file

        val pdfConfig = pdfView.fromFile(file)

        pdfConfig.onError { //If it fails send a toast and go back
            Toast.makeText(context, "Couldn't load PDF", Toast.LENGTH_SHORT).show()
            fragmentManager?.popBackStack()
        }

        pdfConfig.load()

        pdfView.visibility = View.VISIBLE
    }

    /**
     * Handles saving the instance state of this fragment e.g. when rotating the screen
     *
     * @param outState The output state bundle
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("loaded", http_bar_pdf_view.visibility == View.INVISIBLE)
        if (filePath != null) {
            outState.putString("loadFile", filePath!!.absolutePath)
        }
    }

    /**
     * Loads a PDF from the local filesystem
     *
     * @param url The URL from which to load the PDF
     * @param pdfView The [PDFView] in which to render the PDF
     */
    private fun showPdf(url: String, pdfView: PDFView) {
        Log.d("View PDF (URL)", url)

        http_bar_pdf_view.visibility = View.VISIBLE
        pdfView.visibility = View.INVISIBLE

        TempService.startService(url, ContextWrapper(context).cacheDir.absolutePath, "view.pdf", this, pdfView)
    }

    /**
     * Handles the creation of the views.
     * Displays the PDF
     *
     * @param view The current view
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity!!.toolbar.title = args.navName

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("loaded")) {
                http_bar_pdf_view.visibility = View.INVISIBLE
                val path = savedInstanceState.getString("loadFile")
                if (path != null) {
                    showPdf(File(path), pdf_view)
                }
            }
        }

        //Parse the filename into a File
        val file = File(args.pdfFileName)

        when {
            //If the file exists (i.e. has been downloaded) we will view it here
            //We know that this will work since the files that haven't been fully downloaded yet end in .download
            file.exists() -> {
                showPdf(file, pdf_view)
            }
            //If there is a provided URL try to load it from the CS website
            args.url != null -> {
                showPdf(args.url!!, pdf_view)
            }
            //Otherwise fail?
            else -> {
                Toast.makeText(context, "Could not load PDF from cache or website", Toast.LENGTH_SHORT).show()
                fragmentManager?.popBackStack()
            }
        }
    }

    /**
     * Coroutine to download a PDF file (or any other file) to a temporary directory
     * if it hasn't been downloaded yet
     *
     * This can safely be an object since this represents the end of any navigation
     */
    object TempService {
        /**
         * The coroutine scope for this coroutine service
         */
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Starts the file download
         *
         * @param url The URL from which to download the file
         * @param parentFolder The folder to download the file into
         * @param fileName The name for the downloaded file
         * @param inFragment The instance of [PDFViewFragment], to call in class functions
         * @param pdfView The [PDFView] 
         */
        fun startService(url: String, parentFolder: String, fileName: String, inFragment: PDFViewFragment, pdfView: PDFView) {
            //Launch coroutine
            coroutineScope.launch {
                PDFOperations.downloadFile(url, parentFolder, fileName)

                withContext(Dispatchers.Main) {
                    inFragment.http_bar_pdf_view.visibility = View.INVISIBLE
                    inFragment.showPdf(File(parentFolder, fileName), pdfView)
                }
            }
        }
    }
}
