package com.example.sogating_app.Message

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.sogating_app.Message.fcm.NotiAPI
import com.example.sogating_app.Message.fcm.NotiModel
import com.example.sogating_app.Message.fcm.PushNotification
import com.example.sogating_app.Message.fcm.RetrofitInstance
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.example.sogating_app.utils.MyInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.Dispatcher


// 내가 좋아요한 사람들이 나를 좋아요. 한 리스트
// ex) 내가 채영, 기연, 민지 ->  좋아요.
// ex ) 채영 -> 동환, 기연 -> 동환
// 리스트 목록 -> 채영, 기연

class MyLikeListActivity : AppCompatActivity() {

    private val TAG = "MyLikeListActivity"
    private val uid = FirebaseAuthUtils.getUid()

    // 좋아요한 사람의 저장할 리스트 정의
    private val likeUsersDataListUid = mutableListOf<String>()
    private val likeUsersDataList = mutableListOf<UserDataModel>()

    lateinit var listViewAdapter : ListViewAdapter
    lateinit var getterUid : String
    lateinit var getterToken : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like_list)

        // ListViewAdapter 연결하는 부분
        val userListView = findViewById<ListView>(R.id.userListView)

        listViewAdapter = ListViewAdapter(this, likeUsersDataList)
        userListView.adapter = listViewAdapter



        // 내가 좋아요한 사람들 리스트.
        getMyLikeList()

        // 나를 좋아요한 사람들의 리스트를 받아와야 한다.
        // 이 사람이 나와 매칭이 되어있는지 확인해야 한다.!

        // 만약에 내가 천명을 좋아요를 했으면
        // 천명을 모두 찾아내고 , 그중에 내 uid 정보가 있는지 (즉, 나를 좋아요를 했는지 확인해야 한다.)
        // 이 연산은 1000 * 1000 = 1,000,000 번의 연산을 필요로 한다.
        // 해결방법 ) 좋아요 매칭 리스트에서 내가 그 사람을 클릭했을 때 그사람도 나를 좋아요했는지 한번만 확인하도록 로직을 구성한다.

       /* userListView.setOnItemClickListener{ parent, view, position, id ->
          //  Log.d(TAG,likeUsersDataList[position].uid.toString())
            checkMatching(likeUsersDataList[position].uid.toString())

            val notiModel = NotiModel("a", "b")
            val pushModel = PushNotification(notiModel, likeUsersDataList[position].token.toString())
            testPush(pushModel)
        }*/

        userListView.setOnItemLongClickListener{ parent, view, position, id ->

            checkMatching(likeUsersDataList[position].uid.toString())
            // 클릭한 사람의 uid 정보를 저장.
            getterUid = likeUsersDataList[position].uid.toString()
            getterToken = likeUsersDataList[position].token.toString()
            return@setOnItemLongClickListener(true)

        }

        // 내가 좋아요한 유저를 클릭하면(Long Click)
        // 서로 좋아요한 사람이 아니면, 메시지를 못 보내도록 함.
        // 메시지 보내기 창이 꺼서 메시지를 보낼 수 있게 하고
        // 메시지 보내고 상대방에서 Push 알람을 띄워주고


    }

    // 좋아요 매칭 리스트에서 내가 그 사람을 클릭했을 때 그 사람의 uid를 받아와서 매칭 체크하는 함수.
    private fun checkMatching(otherUid : String){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if(dataSnapshot.children.count() == 0){
                    Toast.makeText(this@MyLikeListActivity,"상대방이 좋아요한 사람이 아무도 없어요.",Toast.LENGTH_LONG).show()
                }else{
                    for (dataModel in dataSnapshot.children){

                        val likeUserKey  = dataModel.key.toString()

                        if(likeUserKey.equals(uid)){
                            Toast.makeText(this@MyLikeListActivity,"매칭이 되었습니다.",Toast.LENGTH_LONG).show()
                            // Dialog 띄우기
                            showDialog()

                        }else{
                            //Toast.makeText(this@MyLikeListActivity,"매칭이 실패하였습니다.",Toast.LENGTH_LONG).show()
                        }

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

    // 내가 좋아요한 사람의 uid 정보리스트를 가져오는 함수.
    private fun getMyLikeList(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (dataModel in dataSnapshot.children){
                    //Log.d(TAG, dataModel.key.toString())
                    // 좋아요 리스트에 나를 좋아요한 사람들의 uid 정보를 저장.
                    likeUsersDataListUid.add(dataModel.key.toString())
                }
                // 전체 회원의 정보를 받아오기.
                getUserDataList()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 좋아요한 친구의 uid 밑에 저장되있는 UID 값
        FirebaseRef.userLikeRef.child(uid).addValueEventListener(postListener)
    }

    // 전체 회원 정보를 가져오는 함수.
    private fun getUserDataList(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                //val post = dataSnapshot.getValue<Post>()

                for (dataModel in dataSnapshot.children){

                    val user = dataModel.getValue(UserDataModel::class.java)
                    // 전체 회원중 만약에 likeUserList에 포함되어있다면 내가 좋아요한 사람들의 정보들만 뽑을 수 있다.
                    if(likeUsersDataListUid.contains(user?.uid)){
                        // 내가 좋아요한 사람들의 정보만 추출.
                        likeUsersDataList.add(user!!)
                    }


                }
                listViewAdapter.notifyDataSetChanged() // listview 어뎁터 동기화.
                Log.d(TAG, likeUsersDataList.toString())


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 데이터가 어디에 정의되어 있는 냐???
        FirebaseRef.userInfoRef.addValueEventListener(postListener)
    }

    // Push 메시지 보내기
    private fun testPush(notification : PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.api.postNotification(notification)
    }


    //Dialog
    private fun showDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog,null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("메시지 보내기")
        val mAlertDialog = mBuilder.show()
        val btn = mAlertDialog.findViewById<Button>(R.id.sendBtnArea)
        val textArea = mAlertDialog.findViewById<EditText>(R.id.sendTextArea)
        btn?.setOnClickListener {
            val msgText = textArea!!.text.toString()
            val mgsModel = MsgModel(MyInfo.myNickName, textArea!!.text.toString())
            FirebaseRef.userMsgRef.child(getterUid).push().setValue(mgsModel)

            // 쪽지 보내고 Push 알림.!!
            val notiModel = NotiModel(MyInfo.myNickName, msgText)
            val pushModel = PushNotification(notiModel, getterToken)
            testPush(pushModel)


            mAlertDialog.dismiss()


        }

        // Firebase 에 메시지를 받는 사람 uid 밑에 message, 누가 보냈는지 uid를 저장한다.
    }

}