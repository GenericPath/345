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

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * A [Fragment] to view marks.
 * Uses a WebView
 */
class MarkViewFragment : Fragment() {
    /**
     * Args to pass in the marks view
     */
    private val args : MarkViewFragmentArgs by navArgs()

    /**
     * Entry point of [MarkViewFragment]
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
        return inflater.inflate(R.layout.fragment_mark_view, container, false)
    }

    /**
     * Handles the creation of the views.
     * Displays the marks
     *
     * @param view The current view
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Create an alert dialog
        val builder = AlertDialog.Builder(this.context)

        //Input validation is the CS website's problem - but we will restrict them to numbers
        val input = EditText(this.context)
        input.inputType = InputType.TYPE_CLASS_NUMBER

        //Add the text input to the alert
        builder.setView(input)

        //Add an OK button
        builder.setPositiveButton("Submit") { _: DialogInterface, _: Int ->
            MarksService.startService(args.postUrl, input.text.toString(), this)
        }

        //Add a cancel buton to take us back
        builder.setNegativeButton("Cancel") { _: DialogInterface, _: Int ->
            findNavController().popBackStack() //Go back?
        }

        builder.show()
    }

    /**
     * Sends an HTTP POST request to the CS website to grab some marks HTML.
     */
    object MarksService {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Sends the POST request.
         *
         * @param postUrl The url to send the request to
         * @param id The student ID (who's marks to check)
         * @param inFragment The instance of [MarkViewFragment], to call in class functions
         */
        fun startService(postUrl: String, id: String, inFragment: Fragment) {
            coroutineScope.launch {
                //TODO: Another inappropriate blocking method call
                //Open a connection with jsoup - we need to clean up the returned data.
                val document: Document =
                    Jsoup.connect(postUrl).data("stu_id", id).data("submit", "Submit").post()

                //css selector for filtering - see https://try.jsoup.org/
                val html = document.select("div#coursepagefullwidth ul").first().html()

                //Get the web view with which to render the marks and load the filtered HTML into it
                val markView = inFragment.view!!.findViewById<WebView>(R.id.markView)
                withContext(Dispatchers.Main) {
                    markView.loadData(html, "text/html", "UTF-8")
                }
            }
        }
    }
}