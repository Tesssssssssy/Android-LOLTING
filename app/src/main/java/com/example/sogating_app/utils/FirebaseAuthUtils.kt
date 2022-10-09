package com.example.sogating_app.utils

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthUtils {

    // companion object = public static 과 같은 의미로 사용된다.

    companion object {
        private lateinit var auth: FirebaseAuth

        fun getUid(): String {
            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }
    }
}