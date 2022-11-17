package com.example.sogating_app.MAIN

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.example.sogating_app.R
import kotlinx.android.synthetic.main.menu_child.view.*
import kotlinx.android.synthetic.main.menu_parent.view.*

class ExpandableListAdapter(
    private val context: Context,
    private val parents: MutableList<String>,
    private val childList: MutableList<MutableList<String>>,
) : BaseExpandableListAdapter() {

    override fun getGroupCount() = parents.size
    override fun getChildrenCount(parent: Int) = childList[parent].size
    //    override fun getChildrenCount(parent: Int): Int {
//        if(childList.size == 1){
//            return childList[0].size
//        }
//        return childList[parent].size
//    }
    override fun getGroup(parent: Int) = parents[parent]
    override fun getChild(parent: Int, child: Int): String = childList[parent][child]
    //    override fun getChild(parent: Int, child: Int): String{
//        if(childList.size == 1){
//            return childList[0][child]
//        }
//        return childList[parent][child]
//    }
    override fun getGroupId(parent: Int) = parent.toLong()
    override fun getChildId(parent: Int, child: Int) = child.toLong()
    override fun hasStableIds() = false
    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true

    /* 부모 계층 레이아웃 설정 */
    override fun getGroupView(
        parent: Int,
        isExpanded: Boolean,
        convertView: View?,
        parentview: ViewGroup
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val parentView = inflater.inflate(R.layout.menu_parent, parentview, false)

        parentView.tv_list_title.text = parents[parent]

        return parentView
    }

    /* 자식 계층 레이아웃 설정 */
    override fun getChildView(
        parent: Int,
        child: Int,
        isLastChild: Boolean,
        convertView: View?,
        parentview: ViewGroup
    ): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val childView = inflater.inflate(R.layout.menu_child, parentview, false)

        childView.tv_child_title.text = getChild(parent, child)

        return childView
    }


}
