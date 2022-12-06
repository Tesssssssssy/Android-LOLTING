package com.example.sogating_app.Messenger

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.R
import com.example.sogating_app.audio.*
import com.example.sogating_app.utils.FirebaseRef
import com.example.sogating_app.databinding.ActivityChatBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.ktx.storage
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.appbar.*
import java.util.*
import kotlin.collections.ArrayList
import android.content.Context

// 음성채팅 변수 초기화


class ChatActivity : AppCompatActivity() {
    var myUID = ""
    var anotherUID = ""

    var isPeerConnected = false
    var firebaseMode = FirebaseDatabase.getInstance("https://sogating-test-default-rtdb.firebaseio.com/")

    var firebaseRef = firebaseMode.getReference("users_voice_chat")

    var isAudio = true
    var isVideo = true
    var check_out = 0
    var check_out_2 = 0

    val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    val requestcode = 1

    var initCheck = 0
    lateinit var  pro :ProgressDialog
    // 바이딩 객체
    private lateinit var binding: ActivityChatBinding

    // 대화할 상대를 선택하면 대화 상대에 대한 정보를 변수에 담는다.
    private lateinit var receiverName: String
    private lateinit var receiverUid: String

    lateinit var mAuth: FirebaseAuth // 인증 객체 선언
    lateinit var mDbRef: DatabaseReference // DB 객체 선언

    private lateinit var receiverRoom: String // 받는 대화방 변수
    private lateinit var senderRoom: String // 보낸 대화방 변수

    private lateinit var messageList: ArrayList<Message> // 메시지 리스트


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //뒤로가기 버튼 누르면 메인액티비티로 감
        var back_button = findViewById<ImageView>(R.id.back_button_img)
        back_button.setOnClickListener {
            val intent = Intent(this, ChatMainActivity::class.java)
            startActivity(intent)
        }


        // 대기 다이얼로그 초기화


        // 메시지 리스트 초기화
        messageList = ArrayList()

        // 메시지 어뎁터 초기화
        val messageAdapter: MessageAdapter = MessageAdapter(this, messageList)

        // RecyclerView
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        val voiceChatBtn = findViewById<Button>(R.id.voice_btn)
        // 넘어온 데이터 변수에 담기
        receiverName = intent.getStringExtra("name").toString()
        receiverUid = intent.getStringExtra("uid").toString()

        mAuth = FirebaseAuth.getInstance() // 인증 객체 생성


        // 접속자 uid
        val senderUid = mAuth.currentUser?.uid

        // 보낸이방
        senderRoom = receiverUid + senderUid

        // 받는이방
        receiverRoom = senderUid + receiverUid

        // 툴바에 상대방 이름 보여주기
        binding.chatId.text = receiverName

        //자신의 uid,상대방 uid
        if (senderUid != null) {
            myUID = senderUid
        }
        anotherUID = receiverUid

        // 음성 채팅 버튼 이벤트 리스너
        voiceChatBtn.setOnClickListener {
            if (!isPermissonGranted()) {
                askPermissions()
            }
            Firebase.initialize(this)

            Log.d("cline voicbtn","ok")

            anotherUID = receiverUid
            check_out = 1
            check_out_2 = 1
            pro = ProgressDialog.show(this, "통화 대기중 입니다.", "")
            sendCallRequest()

        }

        //음성 통화 -> 채팅방 버튼
        back_chatBtn.setOnClickListener {

            switchToControls_back()
            if (check_out == 1) {
                myUID = anotherUID
            }
            firebaseRef.child(myUID).setValue(null)
            webView.loadUrl("about:blank")
            back_chatBtn.visibility = View.GONE

        }

        // 메시지 전송 버튼 이벤트
        binding.sendBtn.setOnClickListener {
            // 전송 버튼을 클릭하면 입력한 메시지는 DB에 저장되고 DB에 저장된 메시지를 화면에 보여준다.
            val message = binding.messageEdit.text.toString() // 입력한 메시지를 메시지 변수에 담는다.
            val messageObject = Message(message, senderUid) // 메시지 변수, 보낸 uid를 메시지 클래스 형식의 값을 넣는다.

            // 데이터 저장 {chats}/{senderRoom}/{messages}/메시지내용 저장.
            FirebaseRef.userChatRef.child(senderRoom).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    // 보낸이(송신) 와 받는이(수신) 동시에 메시지값을 저장한다.
                    // 만약 상대방도 로그인했을 때, 받은 메시지를 확인할 수 있다.
                    // 보낸이(senderRoom)의 메시지 내용이 저장을 성공하면 받는이(receiverRoom)에도 저장한다.
                    FirebaseRef.userChatRef.child(receiverRoom).child("messages").push()
                        .setValue(messageObject)
                }

            // 메시지를 전송하고 입력 부분을 초기화하는 기능을 구현.
            binding.messageEdit.setText("")
        }

        // 메시지 가져와서 화면에 보여주기
        // 데이터를 가져올 때 {chats}/{senderRoom}/{messages}에 접근해서 가져온다.
        FirebaseRef.userChatRef.child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear() // 실행이 되면 빈값으로 초기화

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!) // 메시지를 메시지 리스트에 저장.
                    }
                    // 적용
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        // 음성채팅 구현
        toggleAudioBtn.setOnClickListener {

            isAudio = !isAudio
            callJavascriptFunction("javascript:toggleAudio(\"${isAudio}\")")
            toggleAudioBtn.setImageResource(if (isAudio) R.drawable.ic_baseline_mic_24 else R.drawable.ic_baseline_mic_off_24)
        }

//        toggleVideoBtn.setOnClickListener {
//            isVideo = !isVideo
//            callJavascriptFunction("javascript:toggleVideo(\"${isVideo}\")")
//            toggleVideoBtn.setImageResource(if (isVideo) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_videocam_off_24 )
//        }

        setupWebView()
    }
    private fun sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "연결이 되지 않았습니다 인터넷을 확인해주세요", Toast.LENGTH_LONG).show()
            return
        }
        firebaseRef.child(anotherUID).child("incoming").setValue(myUID)
        Log.e("enter_point", myUID.toString())
        firebaseRef.child(anotherUID).child("isAvailable").addValueEventListener(object :
            ValueEventListener {

            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value.toString() == "true") {
                    listenForConnId()
                }

            }

        })

    }

    private fun listenForConnId() {
        firebaseRef.child(anotherUID).child("connId").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null)
                    return
                pro.dismiss()
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
                check_out = 2
            }
        })
    }

    private fun setupWebView() {

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.addJavascriptInterface(JavascriptInterface(this), "Android")

        loadVideoCall()
        initCheck = 1
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        webView.loadUrl(filePath)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                initializePeer()
            }
        }
    }

    var uniqueId = ""

    private fun initializePeer() {

        uniqueId = getUniqueID()

        callJavascriptFunction("javascript:init(\"${uniqueId}\")")

        //상대방이 통화를 요청 했을때 실행
        firebaseRef.child(myUID).child("incoming").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (initCheck != 0) {
                    Log.d(TAG, "onCallRequest" + snapshot.toString())
                    onCallRequest(snapshot.value as? String)
                }
            }
        })

        //상대가 통화를 거절 할 때 실행
        firebaseRef.child(anotherUID).child("incoming").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null && initCheck !=0 && check_out == 1){
                    Log.d(TAG,"상대가 거절 했습니다.")
                    pro.dismiss()
                    Toast.makeText(this@ChatActivity, "상대가 거절 했습니다.",Toast.LENGTH_SHORT).show();
                    toolbar.visibility = View.VISIBLE
                    check_out = 0
                }
            }
        })

        //상대가 통화가 종료 됬을 때 실행(통화 걸기)
        firebaseRef.child(anotherUID).child("connId").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null && initCheck !=0 && check_out == 2) {
                    Log.d(TAG,"상대가 통화를 종료 했습니다.")
                    back_main_notice.visibility = View.VISIBLE
                    back_chatBtn.visibility= View.VISIBLE
                    check_out = 0

                }
            }
        })


        //상대가 통화가 종료 됬을 때 실행(통화 받기)
        firebaseRef.child(myUID).child("connId").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == null && initCheck !=0 && check_out == 2) {
                    Log.d(TAG,"상대가 통화를 종료 했습니다.")
                    back_main_notice.visibility = View.VISIBLE
                    back_chatBtn.visibility= View.VISIBLE
                    check_out = 0

                }
            }
        })

    }
    //상대의 통화 요청을 받았을 때
    private fun onCallRequest(caller: String?) {
        if (caller == null){
            return
        }
        chattoolbar.visibility = View.GONE
        callLayout.visibility = View.VISIBLE

        incomingCallTxt.text = "$caller is calling..."
        Log.d(TAG,"onCallRequestIn")

        acceptBtn.setOnClickListener {
            firebaseRef.child(myUID).child("connId").setValue(uniqueId)
            firebaseRef.child(myUID).child("isAvailable").setValue(true)

            callLayout.visibility = View.GONE
            switchToControls()
            check_out = 2
        }

        rejectBtn.setOnClickListener {
            firebaseRef.child(myUID).child("incoming").setValue(null)
            callLayout.visibility = View.GONE
            toolbar.visibility = View.VISIBLE
            chattoolbar.visibility = View.VISIBLE
        }
    }
    //UI 제어(통화 상태)
    private fun switchToControls() {
        input_layout.visibility=View.GONE
        callControlLayout.visibility = View.VISIBLE
        chat_recyclerView.visibility = View.GONE
        chattoolbar.visibility = View.GONE


        // Firebase에 저장된 나의 이미지를 가져온다.
        val storageRefmy = Firebase.storage.reference.child(myUID + ".png")

        storageRefmy.downloadUrl.addOnCompleteListener(OnCompleteListener { task->
            if(task.isSuccessful){
                Glide.with(baseContext)
                    .load(task.result)
                    .into(userImgMy);
            }

        })

        // Firebase에 저장된 상대방의 이미지를 가져온다.
        val storageRefan = Firebase.storage.reference.child(anotherUID + ".png")

        storageRefan.downloadUrl.addOnCompleteListener(OnCompleteListener { task->
            if(task.isSuccessful){
                Glide.with(baseContext)
                    .load(task.result)
                    .into(userImgAnother);
            }

        })

        userImg.visibility = View.VISIBLE
    }

    //UI 제어(채팅 상태)
    private fun switchToControls_back() {
        toolbar.visibility = View.VISIBLE
        input_layout.visibility=View.VISIBLE
        callControlLayout.visibility = View.GONE
        chat_recyclerView.visibility = View.VISIBLE
        back_main_notice.visibility=View.GONE
        userImg.visibility = View.GONE
        chattoolbar.visibility  = View.VISIBLE



    }



    private fun getUniqueID(): String {
        return UUID.randomUUID().toString()
    }

    private fun callJavascriptFunction(functionString: String) {
        webView.post { webView.evaluateJavascript(functionString, null) }
    }


    fun onPeerConnected() {
        isPeerConnected = true
    }

    override fun onBackPressed(){
        Log.d(TAG,"MY STATUS :" +check_out)
        if (check_out == 1 || check_out_2 == 1) {
            myUID = anotherUID
        }
        firebaseRef.child(myUID).setValue(null)
        finish()
    }

    override fun onDestroy() {
        if (check_out == 1 || check_out_2 == 1) {
            myUID = anotherUID
        }
        firebaseRef.child(myUID).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()

    }


    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestcode)
    }

    private fun isPermissonGranted(): Boolean {

        com.example.sogating_app.audio.permissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}


