package com.example.sogating_app.auth


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sogating_app.MAIN.MainActivity

import com.example.sogating_app.R
import com.example.sogating_app.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_join.*
import java.io.ByteArrayOutputStream


class JoinActivity : AppCompatActivity() {

    private val TAG = "JoinActivity"

    // Initialize Firebase Auth
    private lateinit var auth: FirebaseAuth


    private var nickname = ""
    private var gender = ""
    private var age = ""
    private var uid = ""
    private var position = ""

//    lateinit var profileImage: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        // 파이어베이스 인증 초기화
        auth = Firebase.auth

        // 이미지를 클릭하면 핸드폰에 저장되어있는 이미지들을 불러옴.
        // registerForActivityResult()를 통해서 핸드폰에 저장되어있는 저장소에 접근한다.
        // 회원가입딴에는 이미지 변경이 불가하도록 변경, 자동으로 기본이미지로 일단 저장
        // 후에 마이페이지에서 이미지 바꾸고 사람인지 아닌지 인공지능으로 검증

//        val getAction = registerForActivityResult(
//            ActivityResultContracts.GetContent(),
//            ActivityResultCallback { uri ->
//                profileImage.setImageURI(uri)
//            }
//        )
//
//        // getAction.launch() 메소드를 통해서 저장된 이미지를 변경.
//        profileImage.setOnClickListener {
//            getAction.launch("image/*")
//        }

        //회원가입 버튼
        val joinBtn = findViewById<Button>(R.id.ButtonArea)
        joinBtn.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailArea)
            val pwd = findViewById<TextInputEditText>(R.id.pwdArea)
            val pwdCheck = findViewById<TextInputEditText>(R.id.pwdCheckArea)

            // 회원가입시 예외처리 부분
            val emailCheck = email.text.toString()

            if (emailCheck.isEmpty()) {
                Toast.makeText(this, "비어있음.", Toast.LENGTH_LONG).show()
            } else if (pwd.text.toString() != pwdCheck.text.toString()) {
                Toast.makeText(this, "passWord 가 동일하지 않음.", Toast.LENGTH_LONG).show()
            }


            // 닉네임 , 성별 , 지역 , 나이 , UID 정보를 저장한다.
            nickname = findViewById<TextInputEditText>(R.id.nicknameArea).text.toString()

            // 성별의 경우 남, 여 로만 저장
            val genderbuff = findViewById<TextInputEditText>(R.id.genderArea).text.toString()
            if (genderbuff == "M" || genderbuff == "m") {
                gender = "남"
            } else if (genderbuff == "W" || genderbuff == "w") {
                gender = "여"
            }
            //나이, 롤포지션 저장
            age = findViewById<TextInputEditText>(R.id.ageArea).text.toString()
            position = findViewById<TextInputEditText>(R.id.gamepositionArea).text.toString()


            // 회원가입시 Firebase에 유저의 정보들을 저장한다. -  회원가입 기능 구현.
            auth.createUserWithEmailAndPassword(email.text.toString(), pwd.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser

                        // 유저의 uid 값을 저장한다.
                        uid = user?.uid.toString()

                        // 토큰을 받아오는 부분. - >  매칭이 완료된 특정 사용자에게 메시지 보내기 (Firebase console에서)
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w(
                                        TAG,
                                        "Fetching FCM registration token failed",
                                        task.exception
                                    )
                                    return@OnCompleteListener
                                }

                                // Get new FCM registration token
                                val token = task.result

                                // Log and toast
                                Log.e(TAG, token.toString())

                                // 유저의 데이터 모델
                                val userModel = UserDataModel(
                                    uid,
                                    nickname,
                                    age,
                                    gender,
                                    position,
                                    token,
                                    "내얼굴",
                                    "사는곳",
                                    "롤닉네임",
                                    "롤티어"
                                )


                                // Firebase 실시간 데이터베이스에 가입된 유저의 정보를 Write
                                FirebaseRef.userInfoRef.child(uid).setValue(userModel)

                                // 회원가입시 Firebase storage 에 uid명에 따른 이미지파일을 업로드.
//                                uploadImage(uid)


                            })

                        // 회원가입시 메인 액티비티로 이동.
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)

                    }
                }
        }

    }
}

//    private fun uploadImage(uid: String) {
//        // Firebase 저장되는 경로 지정.
//        val storage = Firebase.storage
//        val storageRef = storage.reference.child(uid + ".png")
//
//        // Get the data from an ImageView as bytes
//        profileImage.isDrawingCacheEnabled = true
//        profileImage.buildDrawingCache()
//        val bitmap = (profileImage as BitmapDrawable).bitmap
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val data = baos.toByteArray()
//
//        var uploadTask = storageRef.putBytes(data)
//        uploadTask.addOnFailureListener {
//            // Handle unsuccessful uploads
//        }.addOnSuccessListener { taskSnapshot ->
//            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
//            // ...
//        }
//



