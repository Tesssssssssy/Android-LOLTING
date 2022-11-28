package com.example.sogating_app.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.sogating_app.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*
import java.util.Arrays.toString
import kotlin.collections.HashMap


class MultiVoiceActivity() : AppCompatActivity() {
    val permissions = arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA)
    val requestcode = 1

    var externalMeetingId: String? = " "
    var meetingId: String? = " "
    var attendeeId: String? = " "

    var send_count = 0
    var count=""
    var room_id : DataSnapshot? = null
    var firebaseMode = FirebaseDatabase.getInstance("https://sogating-test-default-rtdb.firebaseio.com/")
    var firebaseRef = firebaseMode.getReference("multi_voice_chat")
    val UUID = java.util.UUID.randomUUID()
    var isAudio = true

    constructor(parcel: android.os.Parcel) : this() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_voice)

//      권한이 없다면 유저에게 요청
        if (!isPermissonGranted()) {
            askPermissions()
        }
        Firebase.initialize(this)

        val myWebView: WebView = findViewById(R.id.webView2)
        val login_button : Button = findViewById(R.id.loginBtn)
        val user_name : EditText = findViewById(R.id.usernameEdit)



        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.domStorageEnabled = true;


        myWebView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
        myWebView.webViewClient = object: WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {

            }

            override fun onLoadResource(view: WebView?, url: String?) {

                view?.evaluateJavascript("document.getElementById('externalMeetingId').value;") { value ->
//                        Log.d("showINFO", "externalMeetingId : " + value.toString())
                    externalMeetingId = value.toString()
                }
                view?.evaluateJavascript("document.getElementById('meetingId').value;") { value ->
//                        Log.d("showINFO", "meetingId : " + value.toString())
                    meetingId = value.toString()
                }
                view?.evaluateJavascript("document.getElementById('attendeeId').value;") { value ->
//                        Log.d("showINFO", "attendeeId : " + value.toString())
                    attendeeId = value.toString()
                }


                if(externalMeetingId.toString().length > 10){
                    if(send_count == 0) {
                        Log.d(
                            "showINFO1",
                            externalMeetingId.toString() + " " + meetingId.toString() + " " + attendeeId + " "
                        )
                        send_count =1

//                        val UUID = UUID.randomUUID()

                        val UUID = 1
                        firebaseRef.child(UUID.toString()).child("multiroom").setValue(externalMeetingId.toString())




                    }
                }



            }
        }
        myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false)
        myWebView.loadUrl("https://13vu8cduya.execute-api.ap-northeast-2.amazonaws.com/Prod/js-client.html")


        toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio

            if(isAudio){
                myWebView.post{myWebView.evaluateJavascript("javascript:on()", null)}

            }else{
                myWebView.post{myWebView.evaluateJavascript("javascript:mute()", null)}
            }
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)

        }

        login_button.setOnClickListener(){

            firebaseRef.child("1").child("User").child(UUID.toString()).setValue(user_name.text.toString())
            firebaseRef.child("1").child("User").child(UUID.toString()).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    Log.d("TAG","RUNNING ROOM_ID")

                    if(dataSnapshot.child("1").value == null || dataSnapshot.child("1")!!.child("User").value == null){
                        myWebView.post{ myWebView.evaluateJavascript("javascript:joinMeeting()", null)}
                        Log.d("enterd_count",dataSnapshot.child("1")?.value.toString())
                    }else {
                        Log.d("made room","ok")
                        myWebView.post{ myWebView.evaluateJavascript("javascript:document.getElementById('externalMeetingId').value = '" + dataSnapshot.child("1").child("multiroom").value  + "';", null)}
                        myWebView.post{ myWebView.evaluateJavascript("javascript:joinMeeting()", null)}
                    }
                }

                override fun onCancelled(error: DatabaseError) {
// Failed to read value
                    Log.w("find_room_uid", "Failed to read value.", error.toException())
                }
            })



        }

    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestcode)
    }

    //  안드로이드 권한 여부 파악
    private fun isPermissonGranted(): Boolean {

        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onStop() {
        firebaseRef.child("1").child("User").child(UUID.toString()).setValue(null)
        finish()
        super.onStop()
    }


    override fun onDestroy() {
//        Log.d("onDestroy",(room_id!!.child("1").child("Enterd").value.toString()))
        firebaseRef.child("1").child("User").child(UUID.toString()).setValue(null)
        super.onDestroy()
    }


}