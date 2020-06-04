package com.otago.open

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
 * @see [FetchFragment]
 *
 * @param list The list of [CourseItem]s
 * @param listener The function to execute if an course is selected
 */
class CourseItemRecyclerViewAdapter(
    private val list: List<CourseItem>,
    private val listener: (CourseItem) -> Unit
) : RecyclerView.Adapter<CourseItemRecyclerViewAdapter.ViewHolder>() {

    /**
     * Entry point of [FetchFragment].
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
     * Handles the binding of the [ViewHolder] to a [CourseItem]
     *
     * @param holder The [ViewHolder] to bind to
     * @param position The position in the recycler whose corresponding [CourseItem] will be bound to
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)
        holder.itemView.setOnClickListener { listener(currentItem) }

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


