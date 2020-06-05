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

//https://github.com/barteksc/AndroidPdfViewer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
     * Handles the creation of the views.
     * Displays the PDF
     * TODO: Figure out why load() never throws an exception on the current thread
     *
     * @param view The current view
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Displays a PDF
        val pdfView = view.findViewById<PDFView>(R.id.pdfView)
        try {
            val file = File(args.pdfFileName)
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
}
