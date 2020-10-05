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

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.web.assertion.WebViewAssertions.webMatches
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.*
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
    @get:Rule var actRule = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    /**
     * Run test for [PDFOperations.createMetaFile] and [PDFOperations.loadMetaFile]
     */
    @Test
    fun fetchSerialiseCorrect() {
        val testFileDir = InstrumentationRegistry.getInstrumentation().targetContext.getDir("testDir", Context.MODE_PRIVATE)
        testFileDir.mkdirs()
        Log.d("Test", testFileDir.absolutePath)

        //Sample fetches
        val fetches = listOf (
            FetchResult(testFileDir.absolutePath + "/marks.php", "cs.otago.ac.nz/cosc242/marks.php", "Marks", FileNavigatorType.MARKS),
            FetchResult(testFileDir.absolutePath + "/lectures.php", "cs.otago.ac.nz/cosc242/lectures.php", "Lectures", FileNavigatorType.FOLDER),
            FetchResult(testFileDir.absolutePath + "/L01.pdf", "cs.otago.ac.nz/cosc242/pdf/L01.pdf", "Test File", FileNavigatorType.PDF)
        )

        fetches.forEach {
            //Get the filename
            val fName = it.itemFile.substringAfterLast('/')

            //Check filename stuff
            assertEquals(testFileDir.absolutePath + "/" + fName, it.itemFile)

            //Create pretend files
            if (fName.endsWith(".php")) {
                File(it.itemFile).mkdirs()
            } else {
                File(it.itemFile).createNewFile()
            }
            //Create pretend meta file
            PDFOperations.createMetaFile(testFileDir.absolutePath, fName, it)

            //Load pretend meta file
            val fetch = PDFOperations.loadMetaFile(it.itemFile)

            //Check that what we put in is what we get out
            assertEquals(fetch, it)
        }

        val folderListing = PDFListFragment().generateFolderList(testFileDir.absolutePath)
        assertEquals(folderListing, PDFOperations.generatePdfItems(fetches))
    }

    /**
     * A gift from Beelzebub
     * Checks BJ's mark for assignment 1 in COSC344
     */
    @Test
    fun testBjCosc344Mark() {
        var activity: Activity? = null
        actRule.scenario.onActivity { activity = it }

        val sleepTime: Long = 1000

        onView(withId(R.id.textView)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.covid19_notice)))
        onView(withId(R.id.select_items)).perform(click())
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_select)).check(matches(isDisplayed()))
        onView(withId(R.id.http_bar_select)).check(matches(isDisplayed()))

        Thread.sleep(sleepTime)

        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_select)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
        } catch (e: AssertionFailedError) {}

        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Thread.sleep(sleepTime)
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_select)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("COSC344 Database Theory and Applications")),
                click()
            )
        )
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)
        onView(withId(R.id.list_items)).perform(click())

        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_pdf_list)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
            } catch (e: AssertionFailedError) {}

        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Thread.sleep(sleepTime)
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_list)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("COSC344")),
                click()
            )
        )

        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_pdf_list)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
        } catch (e: AssertionFailedError) {}

        onView(withId(R.id.recycler_view_list)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Marks")),
                click()
            )
        )
        Thread.sleep(sleepTime)

        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_mark)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
        } catch (e: AssertionFailedError) {}

        onView(withId(R.id.mark_view)).check(matches(isDisplayed()))
        onWebView().withElement(findElement(Locator.NAME, "stu_id"))
            .perform(webKeys("2367465"))
        Thread.sleep(sleepTime)

        onWebView().withElement(findElement(Locator.NAME, "submit")).perform(webClick())
        Thread.sleep(sleepTime)

        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_mark)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
        } catch (e: AssertionFailedError) {}

        onWebView().withElement(findElement(Locator.TAG_NAME, "body"))
            .check(webMatches(getText(), containsString("Obtained Mark: 7.5")))
        Thread.sleep(sleepTime)

        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)

        onView(withId(R.id.textView)).check(matches(isDisplayed()))
            .check(matches(withText(R.string.covid19_notice)))
        Thread.sleep(sleepTime)

        onView(withId(R.id.list_items)).perform(click())
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_list)).check(matches(isDisplayed()))
        Thread.sleep(sleepTime)

        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Thread.sleep(sleepTime)
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_list)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("COSC344")),
                click()
            )
        )

        Thread.sleep(sleepTime)

        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        Thread.sleep(sleepTime)
        activity!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_list)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Marks")),
                click()
            )
        )
        Thread.sleep(sleepTime)

        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_mark)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
        } catch (e: AssertionFailedError) {}

        onView(withId(R.id.mark_view)).check(matches(isDisplayed()))
        onWebView().withElement(findElement(Locator.NAME, "stu_id")).perform(webKeys("2367465"))
        Thread.sleep(sleepTime)

        onWebView().withElement(findElement(Locator.NAME, "submit")).perform(webClick())
        Thread.sleep(sleepTime)
        try {
            //I HATE THIS
            //I HATE THAT THIS IS THE SIMPLEST WAY TO DO THIS
            while (true) {
                onView(withId(R.id.http_bar_mark)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
            }
        } catch (e: AssertionFailedError) {}

        onWebView().withElement(findElement(Locator.TAG_NAME, "body"))
            .check(webMatches(getText(), containsString("Obtained Mark: 7.5")))
        Thread.sleep(sleepTime)

        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)

        onView(withId(R.id.recycler_view_list)).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Lectures")),
                click()
            )
        )
        Thread.sleep(sleepTime)
        onView(withId(R.id.recycler_view_list)).perform(swipeDown())
        Thread.sleep(sleepTime)

        while (true) {
            try {
                onView(withId(R.id.recycler_view_list)).perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
                Thread.sleep(sleepTime)
                break
            } catch (e: PerformException) { } //Delay here
        }

        while (true) {
            try {
                onView(withId(R.id.pdf_view)).check(matches(isDisplayed()))
                Thread.sleep(sleepTime)
                break
            } catch (e: AssertionError) { } //Delay here
        }

        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)
        pressBack()
        Thread.sleep(sleepTime)
    }
}
