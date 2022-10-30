package com.example.sogating_app.viewpage

import android.media.Image
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_intro_pager_item.view.*

class MyPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val itemImage : ImageView = itemView.pager_item_image
    private val itemContent : TextView = itemView.pager_item_text

    fun bindWithView(pageItem: PageItem) {
        itemImage.setImageResource(pageItem.imageSrc)
        itemContent.text = pageItem.content


    }


}