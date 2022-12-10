package com.example.sogating_app.board

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.sogating_app.R
import com.example.sogating_app.utils.FirebaseAuthUtils

// fragment_talk 의 List 어답터 선언 -> BoardModel 의 리스트를 변수 파라미터로 사용.!
class BoardListLVAdapter(val boardList: MutableList<BoardModel>) : BaseAdapter() {

    override fun getCount(): Int {
        return boardList.size
    }

    override fun getItem(position: Int): Any {
        return boardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view = convertView

        // 뷰를 가져와서 연결하는 부분.-> board_list_item의 뷰를 연결해준다.
        view = LayoutInflater.from(parent?.context).inflate(R.layout.board_list_item, parent, false)

        // 사용자가 작성한 게시글 item 레이아웃 가져옴.
        val itemLinearLayoutView = view?.findViewById<LinearLayout>(R.id.itemView)
        val title = view?.findViewById<TextView>(R.id.titleArea)
        val content = view?.findViewById<TextView>(R.id.contentArea)
        val time = view?.findViewById<TextView>(R.id.timeArea)

        // 게시글 item의 UID값과 나의 UID값이 같으면 -> 내가 쓴 게시글은 색상이 다르게 보이도록 설정.
        if (boardList[position].uid.equals(FirebaseAuthUtils.getUid())) {
            itemLinearLayoutView?.setBackgroundColor(Color.parseColor("#F0FFFF"))
        }

        // 사용자가 작성한 게시글 item들의 title, content, time들을 반영.
        title!!.text = boardList[position].title
        content!!.text = boardList[position].content
        time!!.text = boardList[position].time

        return view!!
    }
}