package com.example.myfirstapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.github.barteksc.pdfviewer.PDFView
import java.io.File


class PDFViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdf_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
            https://github.com/barteksc/AndroidPdfViewer
         */
        val path = context?.filesDir
        // on pixel 2 this dir is /data/data/com.example.myfirstapp/files
        // use adb root; adb push report.pdf /data/data/com.example.myfirstapp/files
        val pdfFilename = arguments?.getString("pdf_file_name")
        val file = File(path, pdfFilename)
        val pdfView = view.findViewById<PDFView>(R.id.pdfView)
        pdfView.fromFile(file).load()
    }
}
