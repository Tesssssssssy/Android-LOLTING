package Messenger

data class Message(
    val message : String?,
    val sendId : String?

){
    constructor() : this("", "")
}
