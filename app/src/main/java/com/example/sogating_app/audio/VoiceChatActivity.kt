package com.example.sogating_app.audio

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.sogating_app.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.android.synthetic.main.activity_voice_chat.*

val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
val requestcode = 1

class VoiceChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_chat)

        if (!isPermissonGranted()) {
            askPermissions()
        }
        Firebase.initialize(this)
        loginBtn.setOnClickListener {
            val username = usernameEdit.text.toString()
            val intent = Intent(this, CallActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
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
}
