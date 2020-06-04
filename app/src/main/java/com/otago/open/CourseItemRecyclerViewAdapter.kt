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
 * @see [PDFListFragment]
 */
class CourseItemRecyclerViewAdapter(
    private val list: List<CourseItem>,
    private val listener: (CourseItem) -> Unit
) : RecyclerView.Adapter<CourseItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item,
            parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)
        holder.itemView.setOnClickListener { listener(currentItem) }

        holder.imageView.setImageResource(currentItem.imageResource)
        holder.textView1.text = currentItem.courseName
        holder.textView2.text = currentItem.courseUrl
    }

    override fun getItemCount() = list.size

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.image_view
        val textView1: TextView = itemView.item_text_1
        val textView2: TextView = itemView.item_text_2

        fun bind(item: CourseItem) {
            imageView.setImageResource(item.imageResource)
            textView1.text = item.courseName
            textView2.text = item.courseUrl
        }
    }
}


