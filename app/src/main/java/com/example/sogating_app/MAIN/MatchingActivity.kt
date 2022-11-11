package com.example.sogating_app.MAIN

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.setting.MyPageActivity
import com.example.sogating_app.slider.CardStackAdapter
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.example.sogating_app.utils.MyInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class
MatchingActivity : AppCompatActivity() {

    lateinit var cardStackAdapter: CardStackAdapter
    lateinit var manager : CardStackLayoutManager

    private val TAG = "MatchingActivity"

    // 접속한 유저의 정보를 저장할 리스트 정의
    private val usersDataList = mutableListOf<UserDataModel>()

    private var userCount = 0

    private lateinit var currentUserGender : String
    private lateinit var currentUserLOLnickname: String
    private lateinit var currentUserLOLtier: String
    private lateinit var currentUserAGE: String

    private val uid = FirebaseAuthUtils.getUid()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        // 좋아요.를 통해서 매칭하는 방법.
        // 나는 동환, 내가 좋아하는 여자는 채영이다.
        // 내가 채영을 좋아요 하면은, 채영의 좋아요 리스트 중에서 내가 있는지만 확인하면 됨.


        // 나와 다른 성별의 유저를 받아오는 법
        // 1. 일단 나의 성별을 알고 전체 유저중에서 나와 다른 성별을 가져온다.

        //뒤로가기 버튼 누르면 메인액티비티로 감
        var back_button = findViewById<ImageView>(R.id.back_button_img)
        back_button.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val cardStackView = findViewById<CardStackView>(R.id.cardStackView)
        
        manager = CardStackLayoutManager(baseContext, object : CardStackListener{
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?) {
                // 카드를 다 넘기면 새롭게 생기게하는 메소드
                if(direction == Direction.Right){
                    //Toast.makeText(this@MainActivity,"right",Toast.LENGTH_LONG).show()
                    // 카드를 오른쪽으로 넘기면 내가 좋아요한 사람의UID값을 넘겨준다.
                    userLikeOtherUser(uid, usersDataList[userCount].uid.toString())
                }
                if(direction == Direction.Left){
                    //Toast.makeText(this@MainActivity,"left",Toast.LENGTH_LONG).show()
                }

                userCount += 1 // 넘기면 유저의 숫자를 counting

                if(userCount == usersDataList.count()){
                    getUserDataList(currentUserGender)
                    Toast.makeText(this@MatchingActivity,"유저를 새롭게 받아옵니다.",Toast.LENGTH_LONG).show()
                }
            }

            override fun onCardRewound() {

            }

            override fun onCardCanceled() {

            }

            override fun onCardAppeared(view: View?, position: Int) {

            }

            override fun onCardDisappeared(view: View?, position: Int) {

            }


        })

        // 카드 스텍 어뎁터에 연결하는 부분
        cardStackAdapter = CardStackAdapter(baseContext, usersDataList)
        cardStackView.layoutManager = manager
        cardStackView.adapter  = cardStackAdapter


        getMyUserData() //나의 정보를 가져오는 함수 호출.
    }

    /* 매칭 알고리즘 함수 
    * 1. 나와 다른 성별 남 -> 여, 여 -> 남
    * 2. firebase realtime database 내에서 lolname 이 "롤닉네임" 이 아니어야 함 즉 롤닉네임이 등록 되어 있어야함
    * 3. 나이 +- 10살 (추후 구현 예정)
    * 4. 티어 차이 +- 1티어 브론즈라면 브론즈, 실버, 골드 만 매칭 (추후 구현 예정)
    * */
    
    // 나의 유저 데이터 가져오기
    private fun getMyUserData(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.w(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)


                currentUserGender = data?.gender.toString()
                currentUserAGE = data?.age.toString()
                currentUserLOLnickname = data?.lolname.toString()
                currentUserLOLtier = data?.loltier.toString()

                MyInfo.myNickName = data?.nickname.toString()
                getUserDataList(currentUserGender)

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 데이터가 어디에 정의되어 있는 냐?
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }

    // 회원 정보를 가져오는 함수.
    private fun getUserDataList(currentUserGender : String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                //val post = dataSnapshot.getValue<Post>()

                for (dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)

                    // 매칭 알고리즘!
                    //1. 나와 다른 성별 남 -> 여, 여 -> 남
                    //2. firebase realtime database 내에서 lolname 이 "롤닉네임" 이 아니어야 함 즉 롤닉네임이 등록 되어 있어야함
                    //3. 나이 +- 10살 (추후 변경 예정)
                    //4. 티어 차이 +- 1티어 브론즈라면 브론즈, 실버, 골드 만 매칭 (추후 구현 예정)
                    if(user!!.gender.toString().equals(currentUserGender) || user!!.lolname == "롤닉네임" ){

                    }else{
                        usersDataList.add(user!!) // 유저의 정보 리스트에 추가.
                    }



                }

                cardStackAdapter.notifyDataSetChanged() // 현재 회원가입된 유저의 정보로 카드스택어뎁터 동기화.
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 데이터가 어디에 정의되어 있는 냐???
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }

    // 유저의 좋아요를 표시하는 부분
    // 데이터베이스에 값을 저장해야 하는데, 어떤 값을 저장할까.,,?
    // 나의 uid, 좋아요한 사람의 uid값이 필요하다.
    private fun userLikeOtherUser(myUid : String, otherUid : String ){
        FirebaseRef.userLikeRef.child(myUid).child(otherUid).setValue("좋아요.")
    }

    // 내가 좋아요한 사람의 uid 정보리스트를 가져오는 함수.
    private fun getOhterUserLikeList(otherUid : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // 내가 좋아요한 사람의 uid 리스트 안에서 나의 UID가 있는 지 확인만 하면 매칭성공!!
                // 내가 좋아요한 사람(채영)의 좋아요 리스트를 불러와서
                // (채영) 좋아요 리스트에 내 UID가 있는지 체크만 해주면 됨.
                for (dataModel in dataSnapshot.children){

                    val likeUserKey  = dataModel.key.toString()
                    if(likeUserKey.equals(uid)){
                        Toast.makeText(this@MatchingActivity,"매칭 완료",Toast.LENGTH_LONG).show()
                        createNotificationChannel() // 매칭이 완료되었을 떄 Notification 알림
                        sendNotification()
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 좋아요한 친구의 uid 밑에 저장되있는 UID 값
        FirebaseRef.userLikeRef.child(otherUid).addValueEventListener(postListener)
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("testChannel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(){
        var builder = NotificationCompat.Builder(this, "Test_Channel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("매칭완료")
            .setContentText("매칭이 완료되었습니다. 저 사람도 나를 좋아해요.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)){
            notify(123,builder.build())
        }

    }
}