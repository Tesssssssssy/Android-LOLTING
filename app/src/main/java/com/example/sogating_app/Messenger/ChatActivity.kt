package com.example.sogating_app.Messenger

import android.Manifest
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sogating_app.R
import com.example.sogating_app.audio.*
import com.example.sogating_app.utils.FirebaseRef
import com.example.sogating_app.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*
import kotlin.collections.ArrayList
// 음성채팅 변수 초기화
var myUID = ""
var anotherUID = ""

var isPeerConnected = false
var firebaseMode = FirebaseDatabase.getInstance("https://sogating-test-default-rtdb.firebaseio.com/")

var firebaseRef = firebaseMode.getReference("users_voice_chat")

var isAudio = true
var isVideo = true
var check_out = 0

val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
val requestcode = 1


class ChatActivity : AppCompatActivity() {
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

        if (senderUid != null) {
            Log.d("my uid",senderUid)
        }
        Log.d("another uid",receiverUid)
        //            자신의 uid,상대방 uid
        if (senderUid != null) {
            myUID = senderUid
        }

        // 음성 채팅 버튼 이벤트 리스너
        voiceChatBtn.setOnClickListener {
            if (!isPermissonGranted()) {
                askPermissions()
            }
            Firebase.initialize(this)

            Log.d("cline voicbtn","ok")

            anotherUID = receiverUid
            check_out = 1
            sendCallRequest()

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
        firebaseRef.addChildEventListener(childEventListener)
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
                switchToControls()
                callJavascriptFunction("javascript:startCall(\"${snapshot.value}\")")
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

        firebaseRef.child(myUID).child("incoming").addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG,"onCallRequest" + snapshot.toString())
                onCallRequest(snapshot.value as? String)
            }


        })

    }

    private fun onCallRequest(caller: String?) {
        if (caller == null){
            return
        }
        toolbar.visibility = View.GONE
        callLayout.visibility = View.VISIBLE

        incomingCallTxt.text = "$caller is calling..."
        Log.d(TAG,"onCallRequestIn")

        acceptBtn.setOnClickListener {
            firebaseRef.child(myUID).child("connId").setValue(uniqueId)
            firebaseRef.child(myUID).child("isAvailable").setValue(true)

            callLayout.visibility = View.GONE
            switchToControls()
        }

        rejectBtn.setOnClickListener {
            firebaseRef.child(myUID).child("incoming").setValue(null)
            callLayout.visibility = View.GONE
            toolbar.visibility = View.VISIBLE

        }

    }

    private fun switchToControls() {
        toolbar.visibility = View.GONE
        input_layout.visibility=View.GONE
        callControlLayout.visibility = View.VISIBLE
        chat_recyclerView.visibility = View.GONE

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

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        if (check_out == 1) {
            myUID = anotherUID
        }
        firebaseRef.child(myUID).setValue(null)
        webView.loadUrl("about:blank")
        super.onDestroy()

    }

    private val childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.e("Listeners", "ChildEventListener-onChildAdded : ${snapshot.value}",)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {
            if(snapshot.child("connId").value.toString().equals(anotherUID)){
                Toast.makeText(getApplicationContext(), "상대방이 나갔습니", Toast.LENGTH_SHORT).show();
                Log.d(TAG,snapshot.child("connId").value.toString())
            }

        }


        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}

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


