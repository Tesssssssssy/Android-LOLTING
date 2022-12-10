package com.example.sogating_app.board

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.sogating_app.R
import com.example.sogating_app.databinding.ActivityBoardWriteBinding
import com.example.sogating_app.databinding.ActivityCommunityBinding
import com.example.sogating_app.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CommunityActivity : AppCompatActivity() {

    // fragment_talk 바인딩 연결.
    private lateinit var binding : ActivityCommunityBinding

    // boardList 데이터와 키값들을 넣어놓는 리스트를 선언.
    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()

    // BoardAdapter 변수 선언.
    private lateinit var boardRVAdapter : BoardListLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // boardListView 어답터 연결.
        boardRVAdapter = BoardListLVAdapter(boardDataList)
        binding.boardListView.adapter = boardRVAdapter

        // BoardListView 클릭 리스너 -> List에 있는 해당 item을 클릭시 작성한 BoardInsideActivity 내부로 이동.
        binding.boardListView.setOnItemClickListener { parent, view, position, id ->

            // 첫번째 방법으로는 listview에 있는 데이터 title content time 다 다른 액티비티로 전달해줘서 만들기 (putExtra 활용)
//            val intent = Intent(context, BoardInsideActivity::class.java)
//            intent.putExtra("title", boardDataList[position].title)
//            intent.putExtra("content", boardDataList[position].content)
//            intent.putExtra("time", boardDataList[position].time)
//            startActivity(intent)

            // TalkFragment의 ListView의 item을 클릭시 BoardInsideActivity로 액티비티 이동.
            // BoardInsideActivity로 이동시 해당 item의 Key값을 넘겨주고 BoardInsideActivity 53줄에서 넘겨준 key 값을 받아온다.
            // 두번째 방법으로는 Firebase에 있는 board에 대한 고유의 데이터의 id를 기반으로 다시 Firebase를 통해 데이터를 받아오는 방법.
            val intent = Intent(this, BoardInsideActivity::class.java)
            intent.putExtra("key", boardKeyList[position])
            startActivity(intent)
        }

        // 글쓰기 아이콘 클릭시 BoardWriteActivity 이동.
        binding.writeBtn.setOnClickListener {
            val intent = Intent(this, BoardWriteActivity::class.java)
            startActivity(intent)
        }

        getFBBoardData() // Firebase에 저장되어 있는 데이터를 게시판으로 가져오는 함수 실행.

    }

    // Firebase에 저장되어 있는 데이터를 게시판으로 가져오는 함수.
    private fun getFBBoardData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                // Firebase는 비동기 처리하기 때문에 Firebase에 저장되어 있는 이전의 데이터까지 동시에 변경된다.
                // 이 문제를 해결하기 위해서 이전에 반영된 데이터를 삭제해야 한다.
                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    Log.d(TAG, dataModel.toString())
//                    dataModel.key

                    // BoardModel 데이터 형식으로 item들을 가져온다.
                    // item의 데이터와 키값 boardList 리스트에 저장한다.
                    val item = dataModel.getValue(BoardModel::class.java)
                    boardDataList.add(item!!)
                    boardKeyList.add(dataModel.key.toString())

                }

                // 작성한 최신글이 맨 위로 보여주기위해서 BoardList들을 Reverse()로 뒤집어 준다.
                boardKeyList.reverse()
                // Reverse()되고 최신글을 맨 위로 보여준 뒤 다시 정상적으로 데이터들에 접근하기 위해서 Reverse()를 해준다.
                boardDataList.reverse()

                // 비동기 처리하기 때문에 데이터를 반영하기 위해 -> boardRvAdapter 어답터를 동기화
                boardRVAdapter.notifyDataSetChanged()

                Log.d(TAG, boardDataList.toString())


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.boardRef.addValueEventListener(postListener)

    }
}