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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.activity_main.*
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
            "try {" +
                "document.getElementsByTagName(\"body\")[0].innerHTML = document.getElementById('coursepagefullwidth').innerHTML;" +
                "document.querySelectorAll('link[rel=\"stylesheet\"]').forEach(el => el.parentNode.removeChild(el));" +
                "var footer = document.getElementsByClassName(\"footer\");" +
                "if (footer && footer[0]) {" +
                    "footer[0].innerHTML = \"\"" +
                "}" +
                "document.getElementsByTagName(\"body\")[0].style=\"margin: 5px; width: 100vw; box-sizing: border-box;\";" +
            "} catch (err) {" +
                "console.log(err);" +
            "}" +
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

        activity!!.toolbar.title = args.navName

        mark_view.settings.javaScriptEnabled = true
        mark_view.settings.domStorageEnabled = true

        mark_view.addJavascriptInterface(MarkJS(mark_view, http_bar_mark, activity as MainActivity), "MarkJS")

        mark_view.webViewClient = object : WebViewClient() {
            /**
             * Returns true if a given request should be overridden
             * Always returns true
             *
             * @param view The associated [WebView]
             * @param request The request to consider
             *
             * @return Whether to override the given request
             */
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return true
            }

            /**
             * Handles an overridden request.
             * Returns a dummy response if the request isn't for a marks.php web-page
             *
             * @param view The associated [WebView]
             * @param request The request to consider
             *
             * @return A response to the given request
             */
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                if (request != null) {
                    if (!request.url.toString().endsWith("marks.php")) {
                        Log.d("Ignoring URL", request.url.toString())
                        //Return a dummy URL
                        return WebResourceResponse("text/javascript", "UTF-8", null)
                    }
                }

                //If we don't care then return the default behaviour
                return super.shouldInterceptRequest(view, request)
            }

            /**
             * Handles loading of a resource
             * Hides the WebView while resources are loading
             *
             * @param view The associated [WebView]
             * @param url The URL for the resource that is being loaded
             */
            override fun onLoadResource(view: WebView?, url: String?) {
                //Hide while loading - do this always as for some reason loading the favicon causes weird UI issues
                mark_view.visibility = View.INVISIBLE
                http_bar_mark.visibility = View.VISIBLE
                super.onLoadResource(view, url)
            }

            /**
             * Handles the completion of the loading of a resource
             * Starts the JavaScript function to tidy up the page
             *
             * @param view The associated [WebView]
             * @param url The URL for the resource that has been loaded
             */
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                Log.d("MarkJS", "JS Start")
                //No questions please
                view.loadUrl("javascript:($javaScript)()")
            }
        }

        mark_view.loadUrl(args.postUrl)
    }

    /**
     * Android JavaScript interface for setting the visibility of the [WebView] only once most of the COSC website has been deleted
     *
     * @param markView The [WebView] to hide / show
     * @param httpBar The [ProgressBar] to show / hide
     * @param activity The activity (required to call run things on the UI thread)
     */
    class MarkJS(private val markView: WebView, private val httpBar: ProgressBar, private val activity: MainActivity) {

        /**
         * A JavaScript interface for running when the loadUrl JS has run
         */
        @android.webkit.JavascriptInterface
        fun jsDone() {
            Log.d("MarkJS", "JS Done")
            activity.runOnUiThread {
                markView.visibility = View.VISIBLE
                httpBar.visibility = View.INVISIBLE
            }
        }
    }
}
