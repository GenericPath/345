package com.otago.open

import android.content.ContextWrapper
import org.junit.Test

import org.junit.Assert.*
import java.io.File

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
    }
}
