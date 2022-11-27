package Messenger

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sogating_app.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val context: Context, private val messageList : ArrayList<Message>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 하나의 ViewHolder의 객체를 다루면 객체.ViewHolder를 사용하겠지만 어떤 ViewHolder든 받을 수 있도록 RecylerView.ViewHolder를 사용.

    // 메시지(사용자 uid)에 따라 어떤 ViewHolder를 사용할 지 정하기 위해서 두개의 변수 선언
    private val receive = 1 // 받는 타입
    private val send = 2 // 보내는 타입

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) { // 받는 화면 연결
            val view: View = LayoutInflater.from(context).inflate(R.layout.receive, parent, false)
            ReceiveViewHolder(view)  // 받는 ViewHolder 객체 생성
        } else { // 보내는 화면 연결
            val view: View = LayoutInflater.from(context).inflate(R.layout.send, parent, false)
            sendViewHolder(view) // 보내는 ViewHolder 객체 생성
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) // 데이터를 연결.
    {
        // 현재 메시지
        val currentMessage = messageList[position]

        // 보내는 데이터
        if (holder.javaClass == sendViewHolder::class.java) {
            val viewHolder =
                holder as sendViewHolder // holder를 sendViewHolder 타입으로 변경해서 ViewHolder에 담습니다.
            viewHolder.sendMessage.text =
                currentMessage.message // viewHolder로 sendMessage(TextView)에 접근해서 currentMessage.message값을 변경한다.
        } else {
            // 받는 데이터
            val viewHolder =
                holder as ReceiveViewHolder // holder를 ReceiveViewHolder 타입으로 변경해서 ViewHolder에 담습니다.
            viewHolder.receiveMessage.text =
                currentMessage.message // viewHolder로 receiveMessage(TextView)에 접근해서 currentMessage.message값을 변경한다.
        }

    }

    // getItemCount 함수는 messageList의 갯수를 리턴.
    override fun getItemCount(): Int {
        return messageList.size
    }

    // getItemViewType 함수를 이용하여 어떤 ViewHolder를 사용할지 정한다.
    override fun getItemViewType(position: Int): Int {

        // 메시지 값
        val currentMessage = messageList[position]

        // 전달된 메시지를 currentMessage에 담아서 접속자 uid와 currentMessage의 uid와 비교한다.
        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.sendId)) {
            // 접속자 uid와 currentMessage의 uid 같다면
            send
        } else {
            // 접속자 uid와 currentMessage의 uid 다르면
            receive
        }
    }

    // sendViewHolder는 보낸쪽 View를 전달받아 객체를 생성
    class sendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.send_message_text)
    }

    // ReceiveViewHolder는 받는 쪽 View를 전달받아 객체를 생성.
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_message_text)
    }

}