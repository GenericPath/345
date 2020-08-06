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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs

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
    @SuppressLint("SetJavaScriptEnabled") //Otherwise it complains about XSS - but that's the CS department's problem
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val markView = view.findViewById<WebView>(R.id.markView)

        val width = "100vw"

        markView.settings.javaScriptEnabled = true

        markView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                //super.onPageFinished(view, url)

                //No questions please
                //Okay, fine
                //It first trims the header and footer,
                //Then replaces the content with only the main portion
                //Then constrains the width of certain divs and any form
                //Then trims the footer so that poor Sandy from COMP160 isn't incorrectly associated
                //With this app
                view.loadUrl("javascript:(function(){if (document.getElementById('coursepage_topbar')) document.getElementById('coursepage_topbar').outerHTML=\"\";if (document.getElementById('coursepage_footer')) document.getElementById('coursepage_footer').outerHTML=\"\";if (document.getElementById('centralbar')) document.getElementById('centralbar').innerHTML = document.getElementById('coursepagefullwidth').innerHTML; if (document.getElementById('outline-container-sec-1')) document.getElementById('centralbar').style = \"width: $width\"; if (document.getElementById('outline-container-sec-1')) document.getElementById('outline-container-sec-1').style = \"width: $width\"; if (document.getElementsByTagName('form')) document.getElementsByTagName('form')[0].style=\"width:$width\"; if (document.getElementsByClassName('footer')) document.getElementsByClassName('footer')[0].outerHTML=\"\";})()")
            }
        }

        markView.loadUrl(args.postUrl)
    }
}
