package com.example.sogating_app.Message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MyMsgActivity : AppCompatActivity() {

    private val TAG = "MyMsgActivity"
    lateinit var listViewAdapter: MsgAdapter
    val msgList = mutableListOf<MsgModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_msg)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setTitle("")

        val listView = findViewById<ListView>(R.id.msgListView)
        listViewAdapter = MsgAdapter(this,msgList)
        listView.adapter = listViewAdapter
        getMyMsg()
    }

    private fun getMyMsg(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                //val post = dataSnapshot.getValue<Post>()
                msgList.clear() // 기존의 메시지를 정리하고 새로운 메시지만 보냄.
                for (dataModel in dataSnapshot.children){
                    val msg = dataModel.getValue(MsgModel::class.java)
                    msgList.add(msg!!)
                    Log.w(TAG, msg.toString())


                }
                msgList.reverse() // 쪽지함을 최신 순서대로 정렬!
                listViewAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 데이터가 어디에 정의되어 있는 냐???
        FirebaseRef.userMsgRef.child(FirebaseAuthUtils.getUid()).addValueEventListener(postListener)
    }

    //뒤로가기 시 메인액티비티 다시 실행
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}