package com.otago.open

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

/**
 * The first navigation fragment the user sees
 */
class FirstFragment : Fragment() {
    /**
     * Entry point of [FirstFragment].
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    /**
     * Handles the creation of the views
     * Adds handlers to the buttons
     *
     * @param view The current view
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
