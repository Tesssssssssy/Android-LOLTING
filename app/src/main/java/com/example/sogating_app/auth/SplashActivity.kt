package com.example.sogating_app.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.R
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private val TAG = "SplashActivity"

    // 유저의 uid 값을 가져오기
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)




        // 유저의 uid 정보를 가져온다.
        //val uid = auth.currentUser?.uid.toString()
        val uid = FirebaseAuthUtils.getUid()


        // 유저의 uid 의 null 이면 로그인 정보가 없으므로 인트로 액티비티.
        // 유저의 uid null 이 아니면 로그인 정보가 있으므로 메인 액티비티.

        if (uid == "null") {
            Handler().postDelayed({
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 3000)
        } else {
            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }, 3000)
        }


    }
}