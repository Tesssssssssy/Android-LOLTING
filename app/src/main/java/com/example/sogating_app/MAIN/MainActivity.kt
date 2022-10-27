package com.example.sogating_app.MAIN

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.sogating_app.Message.MyLikeListActivity
import com.example.sogating_app.Message.MyMsgActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.setting.MyPageActivity
import com.example.sogating_app.setting.PartnerActivity
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var header : TextView
    private val uid = FirebaseAuthUtils.getUid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar) // toolBar를 통해 App Bar 생성
        setSupportActionBar(toolbar) // 툴바 적용

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 드로어를 꺼낼 홈 버튼 활성화
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_dehaze_24) // 홈버튼 이미지 변경
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 네비게이션 드로어 생성
        drawerLayout = findViewById(R.id.drawer_layout)

        // 네비게이션 드로어 내에있는 화면의 이벤트를 처리하기 위해 생성
        navigationView = findViewById(R.id.nv_drawer)
        navigationView.setNavigationItemSelectedListener(this) //navigation 리스너

        //헤더 부분 이름을 사용자 이름을 변경
        val header: View = navigationView.getHeaderView(0)
        val headertext : TextView = header.findViewById(R.id.header_text)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(UserDataModel::class.java)
                headertext.text = data!!.nickname + "님"
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
        
        // 초기 프래그먼트 셋팅
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.frame_layout, fragment, "Home")
        fragmentTransaction.commit()

        // expandableList 실행
        setExpandableList()
    }

    // 툴바 메뉴 버튼이 클릭 됐을 때 실행하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // 클릭한 툴바 메뉴 아이템 id 마다 다르게 실행하도록 설정
        when (item!!.itemId) {
            android.R.id.home -> {
                // 햄버거 버튼 클릭시 네비게이션 드로어 열기
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 드로어 내 아이템 클릭 이벤트 처리하는 함수 없으면 에러남
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }

    /* ExpandableListView 설정 */
    private fun setExpandableList() {
        val parentList = mutableListOf("내정보","매칭하러가기","내가좋아요한 사람목록", "메시지함")
        val childList = mutableListOf(mutableListOf("내정보","원하는 상대 정보"))

        val expandableAdapter = ExpandableListAdapter(this, parentList,childList)
        el_menu.setAdapter(expandableAdapter)

        /* parent 클릭 이벤트 설정 */
        el_menu.setOnGroupClickListener { parent, v, groupPosition, id ->
            when (groupPosition) {
                //내정보
                0 -> {}
                //매칭하러가기
                1 -> {
                    val intent = Intent(this, MatchingActivity::class.java)
                    startActivity(intent)
                }
                //나에게 온 매칭
                2 -> {
                    val intent = Intent(this, MyLikeListActivity::class.java)
                    startActivity(intent)
                }
                //친구목록
                3 -> {
                    val intent = Intent(this, MyMsgActivity::class.java)
                    startActivity(intent)
                }
                
            }
            false
        }
        /* child 클릭 이벤트 설정 */
        el_menu.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            when (groupPosition) {
                //내정보
                0 -> when(childPosition){
                    //내정보
                    0 ->{
                        val intent = Intent(this, MyPageActivity::class.java)
                        startActivity(intent)
                    }
                    //원하는 상대 정보
                    1 ->{
                        val intent = Intent(this, PartnerActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            false
        }

    }

}