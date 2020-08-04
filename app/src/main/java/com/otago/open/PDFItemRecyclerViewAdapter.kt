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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_item.view.*

/**
 * A standard [RecyclerView.Adapter] to display PDF items.
 * The adapter binds the view holders to their data.
 *
 * @see [PDFItemRecyclerViewAdapter]
 * @see [PDFListFragment]
 *
 * @param list The list of [PDFItem]s
 * @param listener The function to execute if a PDF is selected
 */
class PDFItemRecyclerViewAdapter(
    private val list: List<PDFItem>,
    private val listener: (PDFItem) -> Unit
) : RecyclerView.Adapter<PDFItemRecyclerViewAdapter.ViewHolder>() {
    /**
     * Entry point of [PDFItemRecyclerViewAdapter].
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

    /**
     * Handles the binding of the [ViewHolder] to a [PDFItem]
     *
     * @param holder The [ViewHolder] to bind to
     * @param position The position in the recycler whose corresponding [PDFItem] will be bound to
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)
        holder.itemView.setOnClickListener { listener(currentItem) }

        holder.imageView.setImageResource(currentItem.imageResource)
        holder.textView1.text = currentItem.prettyPath
        holder.textView2.text = when (currentItem.pathType) {
            FileNavigatorType.PDF -> "PDF"
            FileNavigatorType.FOLDER -> "folder"
        }
    }

    /**
     * Gets the amount of items in this recycler
     *
     * @return The size of the internal list
     */
    override fun getItemCount() = list.size

    /**
     * The [RecyclerView.ViewHolder] to bind [PDFItem]s to recycler entries
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
         * Binds a [PDFItem] to a recycler [View]
         *
         * @param item The item to bind
         */
        fun bind(item: PDFItem) {
            //Sets the image and text to the corresponding item's image and text
            imageView.setImageResource(item.imageResource)
            textView1.text = item.prettyPath
            textView2.text = when (item.pathType) {
                FileNavigatorType.PDF -> "PDF"
                FileNavigatorType.FOLDER -> "folder"
            }
        }
    }
}


