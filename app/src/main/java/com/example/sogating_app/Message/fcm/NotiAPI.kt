package com.example.sogating_app.Message.fcm

import com.example.sogating_app.Message.fcm.Repo.Companion.CONTENT_TYPE
import com.example.sogating_app.Message.fcm.Repo.Companion.SERVER_KEY

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import okhttp3.ResponseBody
import retrofit2.Response

interface NotiAPI {
    @Headers("Authorization: key =$SERVER_KEY","Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification) : Response<ResponseBody>
}