package com.example.sogating_app.board

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.sogating_app.R
import com.example.sogating_app.databinding.ActivityBoardEditBinding
import com.example.sogating_app.databinding.ActivityBoardInsideBinding
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class BoardEditActivity : AppCompatActivity() {

    // 바인딩 변수 선언
    private lateinit var binding : ActivityBoardEditBinding

    private lateinit var key: String

    private lateinit var writerUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // alertDialog 게시판 수정 버튼 클릭시 BoardEditActivity 수정화면으로 이동하면서 putExtra로 넘겨준 key값을 활용.!
        key = intent.getStringExtra("key").toString()
        // key값을 이용해 게시 BoardData와 이미지를 보여준다.
        getBoardData(key)
        getImageData(key)


        binding.boardEditEditBtn.setOnClickListener {
            editBoardData(key)
        }

    }

    // 변경할 게시물의 key값을 받아와서 Firebase 내 저장되 있는 데이터를 동기화.
    private fun editBoardData(key: String) {
        FirebaseRef.boardRef
            .child(key)
            .setValue(
                BoardModel(

                    binding.boardEditTitleArea.text.toString(),
                    binding.boardEditContentArea.text.toString(),
                    writerUid,
                    FirebaseAuthUtils.getTime()
                )
            )

        Toast.makeText(this, "수정완료", Toast.LENGTH_LONG).show()

        finish()

    }

    private fun getImageData(key: String) {

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.boardEditImageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {

                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {

            }
        })


    }

    private fun getBoardData(key: String) {

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val dataModel = dataSnapshot.getValue(BoardModel::class.java)


                // 처음 변수를 초기화하는 것이 아닌 변경하는 것이라 setText() 텍스트 변경 이벤트 함수를 이용해 변경.
                binding.boardEditTitleArea.setText(dataModel?.title)
                binding.boardEditContentArea.setText(dataModel?.content)
                writerUid = dataModel!!.uid


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.boardRef.child(key).addValueEventListener(postListener)


    }


}