package com.example.sogating_app.viewpage

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sogating_app.R

class MyTutorialPagerRecyclerAdapter(private var pageList: ArrayList<PageItem>)
                                       : RecyclerView.Adapter<MyPagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPagerViewHolder {
        return MyPagerViewHolder(LayoutInflater.from(parent.context).inflate(
                                   R.layout.layout_intro_pager_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyPagerViewHolder, position: Int) {
        //데이터와 뷰를 묶는다.
        holder.bindWithView(pageList[position])
    }

    override fun getItemCount(): Int {
        return pageList.size
    }


}


