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
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_mark_view.*


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
     * JavaScript function to tidy up the marks page
     */
    private val javaScript =
        "function() {" +
            "document.getElementsByTagName(\"body\")[0].innerHTML = document.getElementById('coursepagefullwidth').innerHTML;" +
            "document.querySelectorAll('link[rel=\"stylesheet\"]').forEach(el => el.parentNode.removeChild(el));" +
            "var footer = document.getElementsByClassName(\"footer\");" +
            "if (footer && footer[0]) {" +
                "footer[0].innerHTML = \"\"" +
            "}" +
            "document.getElementsByTagName(\"body\")[0].style=\"margin: 5px; width: 100vw; box-sizing: border-box;\";" +
            "window.MarkJS.jsDone()" +
        "}"

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

        mark_view.settings.javaScriptEnabled = true

        mark_view.addJavascriptInterface(MarkJS(mark_view, http_bar_mark, activity as MainActivity), "MarkJS")

        mark_view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                super.shouldOverrideUrlLoading(view, request)
                return true
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                mark_view.visibility = View.GONE
                super.onLoadResource(view, url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                //No questions please
                view.loadUrl("javascript:($javaScript)()")
            }
        }

        mark_view.loadUrl(args.postUrl)
    }

    /**
     *
     */
    class MarkJS(private val markView: WebView, private val httpBar: ProgressBar, private val activity: MainActivity) {

        /**
         *
         */
        @android.webkit.JavascriptInterface
        fun jsDone() {
            activity.runOnUiThread {
                markView.visibility = View.VISIBLE
                httpBar.visibility = View.INVISIBLE
            }
        }
    }
}
