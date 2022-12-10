package com.example.sogating_app.board

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.sogating_app.R
import com.example.sogating_app.comment.CommentLVAdapter
import com.example.sogating_app.comment.CommentModel
import com.example.sogating_app.databinding.ActivityBoardInsideBinding
import com.example.sogating_app.databinding.ActivityBoardWriteBinding
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.lang.Exception

class BoardInsideActivity : AppCompatActivity() {

    // 바인딩 변수 선언
    private lateinit var binding : ActivityBoardInsideBinding

    private lateinit var key: String

    private val commentDataList = mutableListOf<CommentModel>()

    private lateinit var commentAdapter: CommentLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardInsideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // boardSettingIcon 클릭시 showDialog 실행. -> 게시글 수정/삭제 버튼 존재
        binding.boardSettingIcon.setOnClickListener {
            showDialog()
        }

        // 두번째 방법
        // 두번째 방법으로는 Firebase에 있는 board에 대한 고유의 데이터의 id를 기반으로 다시 Firebase를 통해 데이터를 받아오는 방법.
        // TalkFragment에서 item 키값을 넘겨준 값을 받아온다.
        key = intent.getStringExtra("key").toString()

        // TalkFragment에서 넘겨준 해당 item의 key값을 가지고 Board에 대한 item 정보들을 Firebase에서 가져와서 반영한다.
        getBoardData(key)
        getImageData(key)

        // 댓글 작성 버튼 클릭시
        binding.boardInsideCommentBtn.setOnClickListener {
            insertComment(key)
        }

        // key값을 기준으로 작성한 댓글 데이터를 가져오는 함수.
        getCommentData(key)

        // 댓글 리스트 어답터 연결.
        commentAdapter = CommentLVAdapter(commentDataList)
        binding.commentLV.adapter = commentAdapter

    }

    // key값을 기준으로 작성한 댓글 데이터를 가져오는 함수.
    fun getCommentData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                commentDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    val item = dataModel.getValue(CommentModel::class.java)
                    commentDataList.add(item!!)
                }
                // commentAdapter 동기화.
                commentAdapter.notifyDataSetChanged()


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.commentRef.child(key).addValueEventListener(postListener)


    }

    fun insertComment(key: String) {
        // comment
        //   - BoardKey
        //        - CommentKey ( push() 랜덤하게 생성된 key 값)
        //            - CommentData
        //            - CommentData
        //            - CommentData
        FirebaseRef.commentRef
            .child(key)
            .push()
            .setValue(
                CommentModel(
                    binding.boardInsideCommentArea.text.toString(),
                    FirebaseAuthUtils.getTime()
                )
            )

        Toast.makeText(this, "댓글 입력 완료", Toast.LENGTH_SHORT).show()

        // 댓글의 작성이 완료하면 Firebase에 저장하고 사용자의 댓글 입력란 초기화.
        binding.boardInsideCommentArea.setText("")

    }

    private fun showDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.board_custom_dialog, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("게시글 수정/삭제")

        // Dialog mBuilder 실행.
        val alertDialog = mBuilder.show()

        // alertDialog 게시판 수정 버튼 클릭시 BoardEditActivity 수정화면으로 이동.
        alertDialog.findViewById<Button>(R.id.board_dialog_editBtn)?.setOnClickListener {
            Toast.makeText(this, "수정 버튼을 눌렀습니다", Toast.LENGTH_LONG).show()

            val intent = Intent(this, BoardEditActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        // alertDialog 게시판 삭제 버튼 클릭시
        alertDialog.findViewById<Button>(R.id.board_dialog_removeBtn)?.setOnClickListener {
            // 해당 게시판 item의 키값을 찾아와서 해당 key값을 삭제한다.
            FirebaseRef.boardRef.child(key).removeValue()
            Toast.makeText(this, "삭제완료", Toast.LENGTH_LONG).show()
            finish() // BoardInsideActivity 종료.

        }


    }

    // Firebase Storage에 업로드된 이미지를 가져와서 반영하는 함수.
    private fun getImageData(key: String) {

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.boardInsideGetImageArea

        // storageReference에 업로드된 key값 이미지를 URL 형식으로 불러온다.
        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {

                // 글라이드를 이용하여 이미지를 불러옴.
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {
                // 이미지를 불러오지 못하면 getImageArea를 안보이게 한다.
                binding.boardInsideGetImageArea.isVisible = false
            }
        })


    }

    // 작성된 게시판에 대한 내용을 Firebase를 통해서 가져오고 사용자가 해당 게시글을 클릭 시 화면에 보여줌.
    private fun getBoardData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // try{} catch{}문을 이용해서 alertDialog 게시판 삭제 버튼 클릭시 해당 item이 변경이 일어날 경우 onDataChange(dataSnapshot: DataSnapshot) 실행.
                // onDataChange(dataSnapshot: DataSnapshot) 실행됬을 때 item의 key값이 이미 삭제되었기 때문에 예외 처리를 해줘야 애플리케이션이 죽어버리는 경우를 방지할 수 있다.
                try {
                    // BoardModel 데이터를 가져온다.
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)
                    Log.d(TAG, dataModel!!.title)

                    // 가져온 BoardModel의 정보를 반영한다.
                    binding.boardInsideTitleArea.text = dataModel!!.title
                    binding.boardInsideTextArea.text = dataModel!!.content
                    binding.boardInsideTimeArea.text = dataModel!!.time

                    // 글쓴이의 Uid 와 나의 UID를 가져온다.
                    val myUid = FirebaseAuthUtils.getUid()
                    val writerUid = dataModel.uid

                    // 내가 쓴 글만 게시글 수정 및 삭제가 가능.!
                    // 글쓴이의 UID와 나의 UID가 동일하면 boardSettingIcon이 Visible 되고 게시글 수정 및 삭제가 가능하도록 한다.
                    if (myUid.equals(writerUid)) {
                        Log.d(TAG, "내가 쓴 글")
                        binding.boardSettingIcon.isVisible = true
                    } else {
                        Log.d(TAG, "내가 쓴 글 아님")
                    }

                } catch (e: Exception) {

                    Log.d(TAG, "삭제완료")

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.boardRef.child(key).addValueEventListener(postListener)


    }



}