package com.example.sogating_app.Message.img

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("img_byte") private val result : String,
    @SerializedName("img_cel") private val result_cel : String
)
