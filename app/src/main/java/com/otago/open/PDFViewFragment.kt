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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.github.barteksc.pdfviewer.PDFView
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

    /**
     * Entry point of [PDFViewFragment]
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        try {
            Log.d("View PDF", file.absolutePath)
            pdfView.fromFile(file).load()
        } catch (e : IOException) {
            //If it fails send a toast and go back
            Toast.makeText(context, "Couldn't load PDF", Toast.LENGTH_SHORT).show()
            activity?.finish()
        } catch (e: Exception) {
            //If it fails send a toast and go back
            Toast.makeText(context, "PDF corrupt?", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }

    /**
     * Loads a PDF from the local filesystem
     *
     * @param url The URL from which to load the PDF
     * @param pdfView The [PDFView] in which to render the PDF
     */
    private fun showPdf(url: String, pdfView: PDFView) {
        try {
            Log.d("View PDF (URL)", url)
            pdfView.fromUri(Uri.parse(url)).load()
        } catch (e : IOException) {
            //If it fails send a toast and go back
            Toast.makeText(context, "Couldn't load PDF", Toast.LENGTH_SHORT).show()
            activity?.finish()
        } catch (e: Exception) {
            //If it fails send a toast and go back
            Toast.makeText(context, "PDF corrupt?", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
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

        //Parse the filename into a File
        val file = File(args.pdfFileName)

        //TODO: Figure out why load() never throws an exception on the current thread
        when {
            //If the file exists (i.e. has been downloaded) we will view it here
            //We know that this will work since the files that haven't been fully downloaded yet end in .download
            file.exists() -> {
                showPdf(file, view.findViewById<PDFView>(R.id.pdfView))
            }
            //If there is a provided URL try to load it from the CS website
            args.url != null -> {
                showPdf(args.url!!, view.findViewById<PDFView>(R.id.pdfView))
            }
            //Otherwise fail?
            else -> {
                Toast.makeText(context, "Could not load PDF from cache or website?", Toast.LENGTH_SHORT).show()
                activity?.finish()
            }
        }
    }
}
