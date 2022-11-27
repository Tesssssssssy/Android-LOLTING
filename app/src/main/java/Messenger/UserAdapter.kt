package Messenger

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel

// UserViewHolder를 적용하기 위해서 RecyclerView.Adapter에 넣습니다.
class UserAdapter(private val context : Context, private val userList : ArrayList<UserDataModel>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>(){

    // onCreateViewHolder 함수는 user layout을 연결하는 기능 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // view 객체 안에 user_layout을 넣고 view 변수를 userViewHolder 생성자에 넣어서 반환한다.
        val view : View = LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false)
        return UserViewHolder(view)
    }

    // onBindViewHolder함수는 데이터를 전달 받아 user_layout에 보여주는 기능 함수.
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position] // userList에 있는 데이터를 순서대로 currentUser에 넣는다. ( 데이터 담기 )
        holder.nameText.text = currentUser.nickname // 화면에 데이터 보여주기

        // 아이템 클릭 이벤트 ( 대화할 상대 클릭시 이벤트 발생 -> 채팅 액티비티로 이동 )
        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)

            // 넘길 데이터를 ChatActivity로 넘겨줌.
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("name", currentUser.nickname)


            context.startActivity(intent)

        }
    }

    // getItemCount함수는 userList의 갯수를 돌려주는 함수.
    override fun getItemCount(): Int {
        return userList.size // 유저 리스트의 갯수를 반환
    }

    // User_layout 화면과 데이터를 연결해 줄 어뎁터 설정.
    class UserViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val nameText : TextView = itemView.findViewById(R.id.name_text)

    }


}