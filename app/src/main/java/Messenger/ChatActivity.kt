package Messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sogating_app.R
import com.example.sogating_app.utils.FirebaseRef
import com.example.sogating_app.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    // 바이딩 객체
    private lateinit var binding : ActivityChatBinding

    // 대화할 상대를 선택하면 대화 상대에 대한 정보를 변수에 담는다.
    private lateinit var receiverName : String
    private lateinit var receiverUid : String

    lateinit var mAuth : FirebaseAuth // 인증 객체 선언
    lateinit var mDbRef : DatabaseReference // DB 객체 선언

    private lateinit var receiverRoom : String // 받는 대화방 변수
    private lateinit var senderRoom : String // 보낸 대화방 변수

    private lateinit var messageList : ArrayList<Message> // 메시지 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 메시지 리스트 초기화
        messageList = ArrayList()

        // 메시지 어뎁터 초기화
        val messageAdapter : MessageAdapter = MessageAdapter(this, messageList)

        // RecyclerView
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

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

        // 음성 채팅 버튼 이벤트 리스너
        binding.voiceBtn.setOnClickListener {

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

                    for(postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!) // 메시지를 메시지 리스트에 저장.
                    }
                    // 적용
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


}
