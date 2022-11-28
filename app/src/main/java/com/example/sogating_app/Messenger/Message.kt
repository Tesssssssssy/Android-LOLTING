package com.example.sogating_app.Messenger

data class Message(
    val message : String?,
    val sendId : String?

){
    constructor() : this("", "")
}
