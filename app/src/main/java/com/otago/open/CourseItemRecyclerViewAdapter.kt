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

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_item.view.*

/**
 * A standard [RecyclerView.Adapter] to display courses items.
 * The adapter binds the view holders to their data.
 *
 * @see [CourseItemRecyclerViewAdapter]
 * @see [SelectFragment]
 *
 * @param list The list of [CourseItem]s
 * @param listener The function to execute if an course is selected
 */
class CourseItemRecyclerViewAdapter(
    private val list: List<CourseItem>,
    private val listener: (CourseItem) -> Unit
) : RecyclerView.Adapter<CourseItemRecyclerViewAdapter.ViewHolder>() {
    /**
     * Entry point of [CourseItemRecyclerViewAdapter].
     *
     * @param parent The parent recycler [ViewGroup]
     * @param viewType The type of view
     *
     * @return The layout generated from the XML
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item,
            parent, false)

        return ViewHolder(itemView)
    }

    fun setColour(itemView: View, currentItem: CourseItem) {
        itemView.setBackgroundColor(if (currentItem.selected) Color.parseColor("#bbbbcc") else Color.parseColor("#ffffff"))
    }

    /**
     * Handles the binding of the [ViewHolder] to a [CourseItem]
     *
     * @param holder The [ViewHolder] to bind to
     * @param position The position in the recycler whose corresponding [CourseItem] will be bound to
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]


        holder.bind(currentItem)
        holder.itemView.setOnClickListener { listener(currentItem); setColour(holder.itemView, currentItem); }

        setColour(holder.itemView, currentItem)
        holder.imageView.setImageResource(currentItem.imageResource)
        holder.textView1.text = currentItem.courseName
        holder.textView2.text = currentItem.courseUrl
    }

    /**
     * Gets the amount of items in this recycler
     *
     * @return The size of the internal list
     */
    override fun getItemCount() = list.size

    /**
     * The [RecyclerView.ViewHolder] to bind [CourseItem]s to recycler entries
     *
     * @param itemView The recycler item view to bind to
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        /**
         * The recycler image view
         */
        val imageView: ImageView = itemView.image_view

        /**
         * The recycler's first (main) text view
         */
        val textView1: TextView = itemView.item_text_1

        /**
         * The recycler's second (auxiliary) text view
         */
        val textView2: TextView = itemView.item_text_2

        /**
         * Binds a [CourseItem] to a recycler [View]
         *
         * @param item The item to bind
         */
        fun bind(item: CourseItem) {
            //Sets the image and text to the corresponding item's image and text
            imageView.setImageResource(item.imageResource)
            textView1.text = item.courseName
            textView2.text = item.courseUrl
        }
    }
}


