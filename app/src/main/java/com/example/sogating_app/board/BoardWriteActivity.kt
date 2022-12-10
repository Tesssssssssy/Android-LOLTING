package com.example.sogating_app.board

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.sogating_app.databinding.ActivityBoardWriteBinding
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class BoardWriteActivity : AppCompatActivity() {

    // 바인딩 변수 선언
    private lateinit var binding : ActivityBoardWriteBinding

    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 글쓰기 아이콘 버튼 클릭시
        binding.BoardWriteBtn.setOnClickListener {

            // 제목, 내용, 작성자 UID, 작성 시간을 받아온다.
            val title = binding.BoardTitleArea.text.toString()
            val content = binding.BoardContentArea.text.toString()
            val uid = FirebaseAuthUtils.getUid()
            val time = FirebaseAuthUtils.getTime()

            // 파이어베이스 store에 이미지를 저장하고 싶습니다
            // 만약에 내가 게시글을 클릭했을 때, 게시글에 대한 정보를 받아와야 하는데
            // 이미지 이름에 대한 정보를 모르기 때문에 -> 작성한 게시글 및 item 들을 key값으로 인식함.
            // 작성한 게시글 및 item 들을 key값으로 인식과 비슷하게 이미지 이름을 문서의 key값으로 해줘서 이미지에 대한 정보를 찾기 쉽게 해놓음.

            // Firebase에 push()하면서 랜덤하게 생성된 key값을 key 변수에 저장. -> 이미지 를 key값으로 활용.!
            val key = FirebaseRef.boardRef.push().key.toString()

            // 데이터 구조.
            // board
            //    - key (임의로 push 할때 생기는 uid)
            //        - boardModel(title, content, uid, time) 으로 저장.

            FirebaseRef.boardRef
                .child(key)
                .setValue(BoardModel(title, content, uid, time))

            Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

            if(isImageUpload == true) {
                // Firabase Storage에 이미지를 업로드 한다.
                imageUpload(key)
            }

            finish() // 사용자가 게시글 입력 완료시 해당 액티비티 종료.


        }

        // 사용자가 이미지 등록 클릭시 -> 이미지를 업로드 True
        binding.BoardImageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true // 이미지 업로드 Boolean 변수 true.
        }

    }

    // Firebase Storage에 이미지를 업로드하는 함수.
    private fun imageUpload(key : String){
        // Get the data from an ImageView as bytes

        val storage = Firebase.storage
        val storageRef = storage.reference
        // 이미지 이름을 문서의 key값으로 해줘서 이미지에 대한 정보를 찾기 쉽게 해놓기 위해 이미지를 key값 형식으로 Firebase에 저장.
        val mountainsRef = storageRef.child(key + ".png")

        val imageView = binding.BoardImageArea
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == 100) {
            // 내 갤러리에서 이미지 URI을 받아와서 imageArea에 적용한다.
            binding.BoardImageArea.setImageURI(data?.data)
        }

    }


}