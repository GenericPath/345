package com.example.myfirstapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_item.view.*

class PDFItemRecyclerViewAdapter(
    private val list: List<PDFItem>,
    private val listener: (PDFItem) -> Unit
) : RecyclerView.Adapter<PDFItemRecyclerViewAdapter.ViewHolder>() {

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
        holder.textView1.text = currentItem.pathName
        holder.textView2.text = currentItem.pathType
    }

    override fun getItemCount() = list.size

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.image_view
        val textView1: TextView = itemView.item_text_1
        val textView2: TextView = itemView.item_text_2

        fun bind(item: PDFItem) {
            imageView.setImageResource(item.imageResource)
            textView1.text = item.pathName
            textView2.text = item.pathType
        }
    }
}


