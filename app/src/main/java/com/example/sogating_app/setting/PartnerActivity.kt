package com.example.sogating_app.setting

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
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

        //친구와 함께 버튼 클릭
        var friendbtn = findViewById<Button>(R.id.friendbtn)
        friendbtn.setOnClickListener{
            Toast.makeText(this, "동성 친구도 매칭 됩니다!", Toast.LENGTH_SHORT).show()
        }

        //이성과 함께 버튼 클릭
        var loverbtn = findViewById<Button>(R.id.loverbtn)
        loverbtn.setOnClickListener{
            Toast.makeText(this, "이성 친구와 매칭 됩니다!", Toast.LENGTH_SHORT).show()
        }
    }

}