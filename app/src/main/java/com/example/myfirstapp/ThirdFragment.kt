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


class ThirdFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_third, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.home).setOnClickListener {
            findNavController().navigate(R.id.action_ThirdFragment_to_FirstFragment)
        }

        /*
            https://github.com/barteksc/AndroidPdfViewer
         */
        view.findViewById<Button>(R.id.openPDF).setOnClickListener {
            val path = context?.filesDir
            // on pixel 2 this dir is /data/data/com.example.myfirstapp/files
            val file = File(path, "report.pdf")

            //findNavController().navigate(R.id.action_ThirdFragment_to_pdfView2)

            // TODO make this open on a new fragment
            val pdfView = view.findViewById<PDFView>(R.id.pdfView)
            pdfView.fromFile(file).load()

        }
    }
}
