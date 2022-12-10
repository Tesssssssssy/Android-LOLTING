package com.example.sogating_app.utils

import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class FirebaseAuthUtils {
    // 범용적으로 Firebase의 auth를 이용한 getUid(), getTime()를 정의해 놓은 class 이다.
    // companion object = public static 과 같은 의미로 사용된다.

    companion object {
        private lateinit var auth: FirebaseAuth

        fun getUid(): String {
            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }

        fun getTime() : String {

            val currentDateTime = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).format(currentDateTime)

            return dateFormat

        }
    }
}