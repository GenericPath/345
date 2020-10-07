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

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTest {
    /**
     * Run test for [PDFOperations.determinePath]
     */
    @Test
    fun urlProcessIsCorrect() {
        assertEquals("https://cs.otago.ac.nz/cosc242/pdf/L02.pdf", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "pdf/L02.pdf"))
        assertEquals("https://cs.otago.ac.nz/cosc242/pdf/L03.pdf", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "./pdf/L03.pdf"))
        assertEquals("https://cs.otago.ac.nz/cosc242/pdf/L04.pdf", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "/cosc242/pdf/L04.pdf"))
        assertEquals("https://cs.otago.ac.nz/cosc244/pdf/L05.pdf", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "/cosc244/pdf/L05.pdf"))
        assertEquals("https://cs.otago.ac.nz/comp160/contacts.php", PDFOperations.determinePath("https://cs.otago.ac.nz/comp160/contacts.php", "./contacts.php"))
        assertEquals("https://cs.otago.ac.nz/comp160/information.php", PDFOperations.determinePath("https://cs.otago.ac.nz/comp160/contacts.php", "./information.php"))
        assertEquals("https://example.com", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "https://example.com"))
        assertEquals("https://example.com", PDFOperations.determinePath("https://adsb.whitburn.nz", "http://example.com"))
        assertEquals("https://cs.otago.ac.nz/cosc244/pdf/L01.pdf", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc244/pdf/", "L01.pdf"))
        assertEquals("https://cs.otago.ac.nz/L04.pdf", PDFOperations.determinePath("https://cs.otago.ac.nz/cosc244/pdf", "/L04.pdf"))
    }

    /**
     * Run test for [PDFOperations.generatePdfItems]
     */
    @Test
    fun checkUrlFetchFolder() {
        val fetchResult = PDFOperations.fetchLinks("testFolderUrlFetch", "https://cs.otago.ac.nz/cosc242/index.php", true)
        val fetchList = listOf(
            FetchResult("testFolderUrlFetch/assessment.php", "https://cs.otago.ac.nz/cosc242/assessment.php", "Assessment", FileNavigatorType.FOLDER),
            FetchResult("testFolderUrlFetch/lectures.php", "https://cs.otago.ac.nz/cosc242/lectures.php", "Lectures", FileNavigatorType.FOLDER),
            FetchResult("testFolderUrlFetch/marks.php", "https://cs.otago.ac.nz/cosc242/marks.php", "Marks", FileNavigatorType.MARKS),
            FetchResult("testFolderUrlFetch/resources.php", "https://cs.otago.ac.nz/cosc242/resources.php", "Resources", FileNavigatorType.FOLDER),
            FetchResult("testFolderUrlFetch/staff.php", "https://cs.otago.ac.nz/cosc242/staff.php", "Staff", FileNavigatorType.FOLDER),
            FetchResult("testFolderUrlFetch/tutorials.php", "https://cs.otago.ac.nz/cosc242/tutorials.php", "Tutorials", FileNavigatorType.FOLDER)
        )

        val pdfList = listOf(
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER,"testFolderUrlFetch/assessment.php", "https://cs.otago.ac.nz/cosc242/assessment.php", "Assessment"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolderUrlFetch/lectures.php", "https://cs.otago.ac.nz/cosc242/lectures.php", "Lectures"),
            PDFItem(R.drawable.ic_thumb, FileNavigatorType.MARKS, "testFolderUrlFetch/marks.php", "https://cs.otago.ac.nz/cosc242/marks.php", "Marks"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolderUrlFetch/resources.php", "https://cs.otago.ac.nz/cosc242/resources.php", "Resources"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolderUrlFetch/staff.php", "https://cs.otago.ac.nz/cosc242/staff.php", "Staff"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolderUrlFetch/tutorials.php", "https://cs.otago.ac.nz/cosc242/tutorials.php", "Tutorials")
        )
        assertEquals(fetchResult, fetchList)
        if (fetchResult == null) {
            assertEquals(true, false)
            return
        }
        assertEquals(PDFOperations.generatePdfItems(fetchResult), pdfList)
    }

    /**
     * Run test for [PDFOperations.generatePdfItems]
     */
    @Test
    fun checkUrlFetchPdf() {
        val fetchResult = PDFOperations.fetchLinks("testFolderPdfFetch", "https://cs.otago.ac.nz/cosc244/lectures.php", false)

        if (fetchResult == null) {
            assertEquals(true, false)
            return
        }

        val pdfResult = PDFOperations.generatePdfItems(listOf (fetchResult[0], fetchResult[1])) //Don't test stuff that isn't checked

        val fetchList = listOf(
            FetchResult("testFolderPdfFetch/L1.pdf", "https://cs.otago.ac.nz/cosc244/pdf/L1.pdf", "Introduction to Data Communications", FileNavigatorType.PDF),
            FetchResult("testFolderPdfFetch/L2.pdf", "https://cs.otago.ac.nz/cosc244/pdf/L2.pdf", "Signals and Encoding", FileNavigatorType.PDF)
        )

        val pdfList = listOf(
            PDFItem(R.drawable.ic_pdf, FileNavigatorType.PDF,"testFolderPdfFetch/L1.pdf", "https://cs.otago.ac.nz/cosc244/pdf/L1.pdf", "Introduction to Data Communications"),
            PDFItem(R.drawable.ic_pdf, FileNavigatorType.PDF, "testFolderPdfFetch/L2.pdf", "https://cs.otago.ac.nz/cosc244/pdf/L2.pdf", "Signals and Encoding")
        )

        //Only check first two...
        assertEquals(fetchResult[0], fetchList[0])
        assertEquals(fetchResult[1], fetchList[1])
        assertEquals(pdfResult[0], pdfList[0])
        assertEquals(pdfResult[1], pdfList[1])

    }
}
