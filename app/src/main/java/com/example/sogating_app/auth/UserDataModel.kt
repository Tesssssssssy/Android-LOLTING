package com.example.sogating_app.auth

data class UserDataModel(
    val uid: String? = null,
    val nickname: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val city: String? = null,
    val position: String? = null,
    val token : String? = null,
    // 롤 데이터
    val lolname : String? = null,
    val loltier : String? = null,
)

