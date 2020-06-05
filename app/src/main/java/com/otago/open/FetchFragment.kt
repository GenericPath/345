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
import java.lang.IllegalStateException
import java.lang.NullPointerException
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
        return inflater.inflate(R.layout.fragment_fetch, container, false)
    }

    /**
     * Handles the creation of this activity, and starts the coroutine service to list the courses
     *
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        CourseService.startService(this)
    }

    /**
     * Currently just downloads the lecture PDFs and moved to the [PDFListFragment]
     * TODO: Check tutorials / lectures / etc
     *
     * @param item The course to delve into
     */
    fun selectType(item: CourseItem) {
        //will do something more interesting here later
        //And this will take a parameter to replace "lectures.php"
        //And to replace "/lec"
        val action =
            FetchFragmentDirections.actionFetchFragmentToPDFListFragment(ContextWrapper(context).filesDir.absolutePath + "/" + item.courseCode + "/lec", item.courseUrl, "lectures.php")
        NavHostFragment.findNavController(nav_host_fragment)
            .navigate(action)
    }

    /**
     * Coordinates fetching the course webpages from the university website
     * Use [CourseService.startService] to begin
     */
    object CourseService {
        /**
         * The coroutine scope for this coroutine service
         */
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Starts this service
         *
         * @param inFragment The instance of FetchFragment, to call in class functions
         */
        fun startService(inFragment: FetchFragment) {
            coroutineScope.launch {
                runService(inFragment)
            }
        }

        /**
         * Runs this service
         *
         * @param inFragment The instance of FetchFragment, to call in class functions
         */
        private suspend fun runService(inFragment: FetchFragment) {
            val links = ArrayList<CourseItem>()
            try {
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
                        val courseUrl =
                            "https://cs.otago.ac.nz/" + courseCode.toLowerCase(Locale.ROOT)
                        val courseName = linkIt.html()
                        //TODO: Update icon here
                        links.add(
                            CourseItem(
                                R.drawable.ic_folder,
                                courseName,
                                courseUrl,
                                courseCode
                            )
                        )
                        Log.d("Added Course", courseName + "with URL " + courseUrl)
                    }
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

            try {
                withContext(Dispatchers.Main) {
                    //Update the UI on completion of paper fetch
                    inFragment.http_bar.visibility = View.GONE
                    inFragment.recycler_view.adapter =
                        CourseItemRecyclerViewAdapter(links.toList()) { link ->
                            inFragment.selectType(
                                link
                            )
                        }
                    inFragment.recycler_view.layoutManager = LinearLayoutManager(inFragment.context)
                    inFragment.recycler_view.setHasFixedSize(true)
                }
            } catch (e: IllegalStateException) {
                //Just stop if the fragment is gone
                return
            } catch (e: NullPointerException) {
                //Just stop if the fragment is gone
                return
            }
        }
    }
}