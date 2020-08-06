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
     * Run test for [PDFListFragment.PDFService.determinePath]
     */
    @Test
    fun urlProcessIsCorrect() {
        assertEquals("https://cs.otago.ac.nz/cosc242/pdf/L02.pdf", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "pdf/L02.pdf"))
        assertEquals("https://cs.otago.ac.nz/cosc242/pdf/L03.pdf", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "./pdf/L03.pdf"))
        assertEquals("https://cs.otago.ac.nz/cosc242/pdf/L04.pdf", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "/cosc242/pdf/L04.pdf"))
        assertEquals("https://cs.otago.ac.nz/cosc244/pdf/L05.pdf", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "/cosc244/pdf/L05.pdf"))
        assertEquals("https://cs.otago.ac.nz/comp160/contacts.php", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/comp160/contacts.php", "./contacts.php"))
        assertEquals("https://cs.otago.ac.nz/comp160/information.php", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/comp160/contacts.php", "./information.php"))
        assertEquals("https://example.com", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc242/lectures.php", "https://example.com"))
        assertEquals("https://example.com", PDFListFragment.PDFService.determinePath("https://adsb.whitburn.nz", "http://example.com"))
        assertEquals("https://cs.otago.ac.nz/cosc244/pdf/L01.pdf", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc244/pdf/", "L01.pdf"))
        assertEquals("https://cs.otago.ac.nz/L04.pdf", PDFListFragment.PDFService.determinePath("https://cs.otago.ac.nz/cosc244/pdf", "/L04.pdf"))
    }

    @Test
    fun checkUrlFetchFolder() {
        val fetchResult = PDFListFragment.PDFService.fetchLinks("testFolder", "https://cs.otago.ac.nz/cosc242/index.php", true)
        val fetchList = listOf(
            PDFListFragment.FetchResult("testFolder/assessment.php", "https://cs.otago.ac.nz/cosc242/assessment.php", "Assessment", FileNavigatorType.FOLDER),
            PDFListFragment.FetchResult("testFolder/lectures.php", "https://cs.otago.ac.nz/cosc242/lectures.php", "Lectures", FileNavigatorType.FOLDER),
            PDFListFragment.FetchResult("testFolder/marks.php", "https://cs.otago.ac.nz/cosc242/marks.php", "Marks", FileNavigatorType.MARKS),
            PDFListFragment.FetchResult("testFolder/resources.php", "https://cs.otago.ac.nz/cosc242/resources.php", "Resources", FileNavigatorType.FOLDER),
            PDFListFragment.FetchResult("testFolder/staff.php", "https://cs.otago.ac.nz/cosc242/staff.php", "Staff", FileNavigatorType.FOLDER),
            PDFListFragment.FetchResult("testFolder/tutorials.php", "https://cs.otago.ac.nz/cosc242/tutorials.php", "Tutorials", FileNavigatorType.FOLDER)
        )

        val pdfList = listOf(
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER,"testFolder/assessment.php", "https://cs.otago.ac.nz/cosc242/assessment.php", "Assessment"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolder/lectures.php", "https://cs.otago.ac.nz/cosc242/lectures.php", "Lectures"),
            PDFItem(R.drawable.ic_thumb, FileNavigatorType.MARKS, "testFolder/marks.php", "https://cs.otago.ac.nz/cosc242/marks.php", "Marks"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolder/resources.php", "https://cs.otago.ac.nz/cosc242/resources.php", "Resources"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolder/staff.php", "https://cs.otago.ac.nz/cosc242/staff.php", "Staff"),
            PDFItem(R.drawable.ic_folder, FileNavigatorType.FOLDER, "testFolder/tutorials.php", "https://cs.otago.ac.nz/cosc242/tutorials.php", "Tutorials")
        )
        assertEquals(fetchResult, fetchList)
        assertEquals(PDFListFragment.PDFService.generatePdfItems(fetchResult), pdfList)
    }
}
