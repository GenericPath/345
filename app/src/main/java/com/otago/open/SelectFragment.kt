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

import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_select.*
import kotlinx.coroutines.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import java.io.*
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList

/**
 * The class for the fragment to select / fetch the courses
 */
class SelectFragment : Fragment() {
    /**
     * The items in the recycler
     */
    private var adapterItems : List<CourseItem> = emptyList()

    /**
     * Whether the [adapterItems] parameter is valid
     */
    private var hasItems: Boolean = false

    /**
     * The coroutine to use for checking for courses
     */
    private var courseService: CourseService? = null

    /**
     * Entry point for creating a [SelectFragment]
     * Ensure that the instance is retained on back button press
     *
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    /**
     * Sets the fragment up normally
     */
    private fun newState() {
        //We are checking the UoO website then show the progress bar and start the service
        http_bar_select.visibility = View.VISIBLE
        startCourseService(false)
    }

    /**
     * Loads the saved course instances so that we don't need to send any requests if the user navigates backward
     * Also called when the screen is rotated
     *
     * @param savedInstanceState The (saved) state of the application
     */
    private fun restoreState(savedInstanceState: Bundle) {
        Log.d("Select Fragment Saving", "Restoring bundle")
        val code = savedInstanceState.getStringArray("courseCodes")
        val name = savedInstanceState.getStringArray("courseNames")
        val url = savedInstanceState.getStringArray("courseUrls")
        val resId = savedInstanceState.getIntArray("courseResIds")
        val selected = savedInstanceState.getBooleanArray("courseSelected")

        if (code == null || name == null || url == null || resId == null || selected == null) {
            //If something fails then complain then just run it again
            Log.d("Select Fragment Saving", "Bundle restore failed")
            Toast.makeText(context, "Failed to load previous state - fetching again", Toast.LENGTH_SHORT).show()
            http_bar_select.visibility = View.VISIBLE
            startCourseService(true)
            return
        }

        val incomingItems = ArrayList<CourseItem>()
        code.forEachIndexed { i, it ->
            incomingItems.add(CourseItem(resId[i], name[i], url[i], it, selected[i]))
        }

        //Load the recycler
        setRecyclerItems(incomingItems.toList())
    }

    /**
     * Determine whether to start a course fetch (again)
     */
    private fun startCourseService(alwaysTry: Boolean) {
        //If we don't have a course service create one and run it
        if (courseService == null) {
            courseService = CourseService()
        } else if (courseService!!.isActive or !alwaysTry) {
            //If it's running or we don't have to always try to run it again then don't bother
            return
        }

        courseService!!.startService(this)

    }

    /**
     * Sets the [adapterItems] and marks it as being correct
     * Also updates the UI if it exists
     *
     * @param links The items to add
     */
    fun notify(links: List<CourseItem>) {
        //Update the UI on completion of paper fetch
        if (http_bar_select != null) {
            http_bar_select.visibility = View.INVISIBLE
        }
        setRecyclerItems(links)
        hasItems = true
    }

    /**
     * Event handler for saving the state when the user navigates away or rotates the screen
     *
     * @param outState The bundle in which to package any relevant information
     */
    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("Select Fragment Saving", "Creating bundle")
        if (adapterItems.isEmpty()) {
            outState.putString("emptyState", if (http_bar_select.visibility == View.VISIBLE) "fetching" else "empty")
        } else {
            val code = Array(adapterItems.size) {
                adapterItems[it].courseCode
            }
            val name = Array(adapterItems.size) {
                adapterItems[it].courseName
            }
            val url = Array(adapterItems.size) {
                adapterItems[it].courseUrl
            }
            val resId = IntArray(adapterItems.size) {
                adapterItems[it].imageResource
            }
            val selected = BooleanArray(adapterItems.size) {
                adapterItems[it].selected
            }

            outState.putStringArray("courseCodes", code)
            outState.putStringArray("courseNames", name)
            outState.putStringArray("courseUrls", url)
            outState.putIntArray("courseResIds", resId)
            outState.putBooleanArray("courseSelected", selected)
        }

        super.onSaveInstanceState(outState)
    }

    /**
     * Entry point of the [SelectFragment] view.
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity!!.toolbar.title = getString(R.string.app_name)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select, container, false)
    }

    /**
     * Handles the creation of this activity, and starts the coroutine service to list the courses
     *
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //If we have items from a previous state, then just re-add them here
        //Otherwise, try to restore, or if there is nothing to restore then just re-fetch
        if (savedInstanceState == null) {
            Log.d("Select Activity Created", "Fetching")
            if (hasItems) {
                setRecyclerItems(adapterItems)
            } else {
                newState()
            }
        }
        else  {
            //Restore state
            Log.d("Select Activity Created", "Restoring from saved state")

            //We want to handle restoring state when there are no items
            when (savedInstanceState.getString("emptyState")) {
                null -> { //If it's null, we have items
                    restoreState(savedInstanceState)
                }
                "fetching" -> { //We are still fetching here
                    Log.d("Restoring", "Still fetching")
                    http_bar_select.visibility = View.VISIBLE
                }
                "empty" -> { //We have no items
                    setRecyclerItems(adapterItems)
                }
            }
        }
    }

    /**
     * Sets the items in the recycler based on the provided [CourseItem]s
     *
     * @param links The links to add
     */
    private fun setRecyclerItems(links: List<CourseItem>) {
        adapterItems = links
        if (recycler_view_select == null) {
            return
        }
        //Create our recycler view adapter and the lambda to handle selection
        recycler_view_select.adapter = CourseItemRecyclerViewAdapter(links) { link -> setSelected(link) }
        recycler_view_select.layoutManager = LinearLayoutManager(context)
        recycler_view_select.setHasFixedSize(true)
    }

    /**
     * Creates / deletes the course folder and meta file
     *
     * @param item The course to delve into
     */
    private fun setSelected(item: CourseItem) {
        //Change selected status
        item.selected = !item.selected

        //Want to have it to save into a COSC*** folder, and download from https://cs.otago.ac.nz/cosc***
        val courseFolder = ContextWrapper(context).filesDir.absolutePath + "/" + item.courseCode

        //If we've selected, create the folder and meta file
        if (item.selected) {
            File(courseFolder).mkdirs()

            //Create the course meta file
            //Create a "fake" fetch result which corresponds to the course folder, the index.php url, the course code (uppercase when presented), and make sure it's a folder
            val metaFileFetchResult = FetchResult(
                courseFolder,
                item.courseUrl,
                item.courseCode.toUpperCase(Locale.ROOT),
                FileNavigatorType.FOLDER
            )
            //Save the meta file using the fetch result
            PDFOperations.createMetaFile(
                ContextWrapper(context).filesDir.absolutePath,
                item.courseCode,
                metaFileFetchResult
            )
        } else { //If we're unselected then delete the meta file. Keep the other files just in case
            File("$courseFolder.meta").delete()
        }
    }

    /**
     * Coordinates fetching the course web-pages from the university website
     * Use [CourseService.startService] to begin
     */
    class CourseService {
        /**
         * The coroutine scope for this coroutine service
         */
        private val coroutineScope = CoroutineScope(Dispatchers.IO)

        /**
         * Whether the current coroutine is running
         */
        val isActive: Boolean
            get() = coroutineScope.isActive

        /**
         * Starts this service
         *
         * @param inFragment The instance of [SelectFragment], to call in class functions
         */
        @Suppress("BlockingMethodInNonBlockingContext") //TODO: Why this is needed here
        fun startService(inFragment: SelectFragment) {
            coroutineScope.launch {
                val links = ArrayList<CourseItem>()
                try {
                    //URL for the COSC papers page
                    val document = Jsoup.connect("https://www.otago.ac.nz/computer-science/study/otago673578.html").get()
                    //Content div
                    val contents = document.select("#content")
                    contents.forEach { docIt ->
                        //Links
                        val hrefs = docIt.select("a[href]")
                        hrefs.forEach { linkIt ->
                            val infoUrl = linkIt.attr("href")
                            val courseCode = infoUrl.substring(infoUrl.length - 7).toLowerCase(Locale.ROOT)
                            val courseUrl = "https://cs.otago.ac.nz/$courseCode/index.php" //Need to include index at end because of weird redirects if we don't
                            val courseName = linkIt.html()
                            links.add(
                                CourseItem(
                                    R.drawable.ic_folder,
                                    courseName,
                                    courseUrl,
                                    courseCode,
                                    File(ContextWrapper(inFragment.context).filesDir.absolutePath + "/$courseCode.meta").exists()
                                )
                            )
                            Log.d("Added Course", "$courseName with URL $courseUrl")
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

                withContext(Dispatchers.Main) {
                    inFragment.notify(links.toList())
                }
            }
        }
    }
}