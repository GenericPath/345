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

import android.content.ContextWrapper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_first.*

/**
 * The first navigation fragment the user sees
 */
class FirstFragment : Fragment() {
    /**
     * Whether we have updated the course selection
     */
    private var invalidated = false
    /**
     * Entry point of [FirstFragment].
     *
     * @param inflater The inflater to parse the XML
     * @param container The base view that this fragment may be a subview of
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     *
     * @return The layout generated from the XML
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        select_items.setOnClickListener {
            val action = FirstFragmentDirections.actionFirstFragmentToSelectFragment()
            findNavController().navigate(action)
            invalidated = true;
        }

        //Go to fetch view
        list_items.setOnClickListener {
            //Only list files if we are not invalidated, otherwise fetch
            val action = FirstFragmentDirections.actionFirstFragmentToPDFListFragment(ContextWrapper(context).filesDir.absolutePath, null, !invalidated, "OpenOtago")
            findNavController().navigate(action)
            invalidated = false;
        }
    }
}
