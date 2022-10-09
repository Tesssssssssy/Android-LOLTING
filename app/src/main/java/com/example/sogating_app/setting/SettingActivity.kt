package com.example.sogating_app.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.sogating_app.Message.MyLikeListActivity
import com.example.sogating_app.Message.MyMsgActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.IntroActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 푸시 메시지 보내는 방법
        // 1. 앱에서 코드로 notification 띄우기
        // 2. Firebase 콘솔에서 모든 앱에게 PUSH 보내기
        // 3. 매칭이 완료된 특정 사용자에게 메시지 보내기 (Firebase console에서)
        // 4. Firebase console이 아니라 , 앱에서 직접 다른 사람에게 푸시메시지 보내기.

        val mybtn = findViewById<Button>(R.id.myPageBtn)
        mybtn.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }

        val myMsg = findViewById<Button>(R.id.myMessage)
        myMsg.setOnClickListener {
            val intent = Intent(this,MyMsgActivity::class.java)
            startActivity(intent)
        }


        val myLikeBtn = findViewById<Button>(R.id.myLikeListBtn)
        myLikeBtn.setOnClickListener{
            val intent = Intent(this,MyLikeListActivity::class.java)
            startActivity(intent)
        }

        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        logoutBtn.setOnClickListener {
            //로그아웃
            val auth = Firebase.auth
            auth.signOut()

            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }


    }
}