package com.otago.open

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * The main app activity
 */
class MainActivity : AppCompatActivity() {
    /**
     * Delegates to default onCreate, and sets the content view
     * @param savedInstanceState The state of the application (e.g. if it has been reloaded)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Sets the layout
        setContentView(R.layout.activity_main)

        //Sets the toolbar layout (incl. the app name).
        setSupportActionBar(toolbar)
    }
}
