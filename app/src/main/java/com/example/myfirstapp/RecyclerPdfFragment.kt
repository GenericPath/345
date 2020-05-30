package com.example.myfirstapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.github.barteksc.pdfviewer.PDFView
import java.io.File


class RecyclerPdfFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    //val args : RecyclerPdfFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*
        val file = File(context?.filesDir, args.file)
        val pdfView = view.findViewById<PDFView>(R.id.recyclerPdfView)
        pdfView.fromFile(file).load()
         */
    }
}
