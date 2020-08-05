package com.otago.open

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun url_process_is_correct() {
        assertEquals("pdf/L02.pdf", PDFListFragment.PDFService.determinePath("lectures.php", "pdf/L02.pdf", "https://cs.otago.ac.nz/cosc242/"))
        assertEquals("pdf/L02.pdf", PDFListFragment.PDFService.determinePath("lectures.php", "/cosc242/pdf/L02.pdf", "https://cs.otago.ac.nz/cosc242/"))
        assertEquals(null, PDFListFragment.PDFService.determinePath("lectures.php", "/cosc244/pdf/L02.pdf", "https://cs.otago.ac.nz/cosc242/"))
        //assertEquals("pdf/L02.pdf", PDFListFragment.PDFService.determinePath("lectures.php", "pdf/L02.pdf", "https://cs.otago.ac.nz/cosc242/"))
    }
}
