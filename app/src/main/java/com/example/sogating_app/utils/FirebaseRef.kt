package com.example.sogating_app.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseRef {
    // 범용적으로 Firebase 데이터를 가져오기 위한 참조변수들을 정의해놓은 class 이다.
    companion object {
        val database = Firebase.database
        val userInfoRef = database.getReference("userInfoRef")
        val userLikeRef = database.getReference("userLike")
        val userMsgRef = database.getReference("userMsg")
        val userChatRef = database.getReference("userChat")
        val boardRef = database.getReference("board")
        val commentRef = database.getReference("comment")
    }

}