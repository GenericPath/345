package com.otago.open

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.io.File

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
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
}
