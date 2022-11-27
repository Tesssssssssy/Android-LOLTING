package com.example.sogating_app.MAIN

import Messenger.ChatMainActivity
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
import androidx.viewpager2.widget.ViewPager2
import com.example.sogating_app.Message.MyLikeListActivity
import com.example.sogating_app.Message.MyMsgActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.setting.MyPageActivity
import com.example.sogating_app.setting.PartnerActivity
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.example.sogating_app.viewpage.MyTutorialPagerRecyclerAdapter
import com.example.sogating_app.viewpage.PageItem
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var header : TextView
    private val uid = FirebaseAuthUtils.getUid()

    companion object {
        const val TAG: String = "튜토리얼 로그"
    }

    //데이터 배열 선언
    private var pageItemList = ArrayList<PageItem>()
    private lateinit var myTutorialPagerRecyclerAdapter: MyTutorialPagerRecyclerAdapter

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


        //Tutorial
        Log.d(TAG, "MainActivity - onCreate() called")

        previous_btn.setOnClickListener {
            Log.d(TAG, "MainActivity - 이전 버튼 클릭")
            my_tutorial_view_pager.currentItem = my_tutorial_view_pager.currentItem - 1
        }

        next_btn.setOnClickListener {
            Log.d(TAG, "MainActivity - 다음 버튼 클릭")
            my_tutorial_view_pager.currentItem = my_tutorial_view_pager.currentItem + 1
        }


        //데이터 배열 준비
        pageItemList.add(PageItem(R.drawable.myinfo_1, "내 정보 클릭!"))
        pageItemList.add(PageItem(R.drawable.change_profile_img_2,
                "프로필 이미지를 변경할 때\n" + "얼굴 인식 불가 시\n" + "사진이 거부될 수 있습니다" ))
        pageItemList.add(PageItem(R.drawable.check_tier_3, "자신의 롤 아이디를 입력한 후\n" + "자신의 롤 tier를 확인하세요!"))
        pageItemList.add(PageItem(R.drawable.my_wannabe_partner_4, "원하는 상대를 특정하기 위해\n" + "원하는 상대 정보 버튼 클릭!"))
        pageItemList.add(PageItem(R.drawable.with_my_friends_5, "동성 친구와의 게임을 원한다면\n" + "친구와 함께 버튼 클릭!"))
        pageItemList.add(PageItem(R.drawable.with_diff_gender_friends_6, "이성 친구와의 게임을 원한다면\n" + "이성친구와 함께 버튼 클릭!"))
        pageItemList.add(PageItem(R.drawable.go_matching_7, "이제 매칭하러 가볼까요?\n" + "매칭하러 가기 버튼 클릭!"))
        pageItemList.add(PageItem(R.drawable.matching_main_8, "함께 게임할 이성 친구를\n" + "찾아보세요!"))
        pageItemList.add(PageItem(R.drawable.matching_like_9, "화면 속 이성이 마음에 든다면\n" + "오른쪽으로 화면을 넘기세요!"))
        pageItemList.add(PageItem(R.drawable.matching_dislike_10, "화면 속 이성이\n" + "마음에 들지 않는다면\n" + "왼쪽으로 화면을 넘기세요!"))
        pageItemList.add(PageItem(R.drawable.navi_my_like_list_11, "내가 좋아요한 이성을\n" + "확인하고 싶다면\n" + "좋아요 목록 버튼 클릭!"))
        pageItemList.add(PageItem(R.drawable.my_like_list_12, "내가 좋아요한 이성을\n" + "확인할 수 있어요!"))
        pageItemList.add(PageItem(R.drawable.navi_messagelist_13, "이성과 대화를 하고 싶다면\n" + "메세지함 버튼 클릭!"))

        //어댑터 인스턴스 생성
        myTutorialPagerRecyclerAdapter = MyTutorialPagerRecyclerAdapter(pageItemList)

        my_tutorial_view_pager.apply {
            adapter = myTutorialPagerRecyclerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            dots_indicator.setViewPager2(this)
        }



        // expandableList 실행
        setExpandableList() //그대로.
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
        val parentList = mutableListOf("내정보","매칭하러가기","좋아요한 친구 목록","매칭된 친구 목록", "메시지함")
        val childList = mutableListOf(
            mutableListOf("내정보","원하는 상대 정보"),
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )

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
                //좋아요한 친구 목록
                2 -> {
                    val intent = Intent(this, MyLikeListActivity::class.java)
                    startActivity(intent)
                }
                // 매칭된 친구 목록
                3 -> {
                    val intent = Intent(this, ChatMainActivity::class.java)
                    startActivity(intent)
                }
                // 메시지함
                4 -> {
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