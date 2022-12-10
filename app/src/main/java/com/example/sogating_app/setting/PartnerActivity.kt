package com.example.sogating_app.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_partner.*

class PartnerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val uid = FirebaseAuthUtils.getUid()

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
            match.text = "친구와"
            Toast.makeText(this, "동성 친구도 매칭 됩니다!", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("match").setValue(1)
        }

        //이성과 함께 버튼 클릭
        var loverbtn = findViewById<Button>(R.id.loverbtn)
        loverbtn.setOnClickListener{
            match.text = "이성과"
            Toast.makeText(this, "이성 친구와 매칭 됩니다!", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("match").setValue(0)
        }

        getMyData()

        /* 롤 포지션 정하기 */
        btn_1.setOnClickListener{
            wantLOLPostion.text = "탑"
            Toast.makeText(this, "상대방의 포지션은 탑 입니다", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("wantposition").setValue(wantLOLPostion.text)
        }
        btn_2.setOnClickListener{
            wantLOLPostion.text = "정글"
            Toast.makeText(this, "상대방의 포지션은 정글 입니다", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("wantposition").setValue(wantLOLPostion.text)
        }
        btn_3.setOnClickListener{
            wantLOLPostion.text = "미드"
            Toast.makeText(this, "상대방의 포지션은 미드 입니다", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("wantposition").setValue(wantLOLPostion.text)
        }
        btn_4.setOnClickListener{
            wantLOLPostion.text = "원딜"
            Toast.makeText(this, "상대방의 포지션은 원딜 입니다", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("wantposition").setValue(wantLOLPostion.text)
        }
        btn_5.setOnClickListener{
            wantLOLPostion.text = "서폿"
            Toast.makeText(this, "상대방의 포지션은 서폿 입니다", Toast.LENGTH_SHORT).show()
            FirebaseRef.userInfoRef.child(uid).child("wantposition").setValue(wantLOLPostion.text)
        }
    }

    fun getMyData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)
                wantLOLPostion.text = data!!.wantposition
                if(data!!.match == 0){
                    match.text = "이성과"
                }else if(data!!.match == 1){
                    match.text = "친구와"
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        // 데이터가 어디에 정의되어 있는 냐?
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

}