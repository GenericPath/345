package com.otago.open

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Go to example pdf view
        view.findViewById<Button>(R.id.pdfTest).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_PDFViewFragment)
        }

        //Go to list view
        view.findViewById<Button>(R.id.listTest).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_recyclerViewFragment)
        }

        //Go to fetch view
        view.findViewById<Button>(R.id.fetchTest).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_fetchFragment)
        }
    }
}
