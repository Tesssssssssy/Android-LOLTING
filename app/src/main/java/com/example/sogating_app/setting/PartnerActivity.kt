package com.example.sogating_app.setting

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.R

class PartnerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partner)

        //뒤로가기 버튼 누르면 메인액티비티로 감
        var back_button = findViewById<ImageView>(R.id.back_button_img)
        back_button.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}