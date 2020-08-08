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

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_mark_view.*

/**
 * The main app activity
 */
class MainActivity : AppCompatActivity() {
    /**
     * Delegates to default onCreate, and sets the content view
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)

        //Sets the layout
        setContentView(R.layout.activity_main)

        //Sets the toolbar layout (incl. the app name).
        setSupportActionBar(toolbar)
    }

    /**
     * Handles the back button being pressed
     * Directs the back event towards the mark web view if it exists
     */
    override fun onBackPressed() {
        if (mark_view != null) {
            //If the mark view exists and can go back the go back
            if (mark_view.canGoBack()) {
                mark_view.goBack()
                return
            }
        }
        super.onBackPressed()
    }
}
