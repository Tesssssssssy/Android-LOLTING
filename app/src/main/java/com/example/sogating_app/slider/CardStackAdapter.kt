package com.example.sogating_app.slider

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CardStackAdapter(val context: Context, val items: MutableList<UserDataModel>) : RecyclerView.Adapter<CardStackAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardStackAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view : View = inflater.inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardStackAdapter.ViewHolder, position: Int) {
        holder.binding(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        // 회원가입시 저장된 이미지를 찾아서 가져온다.
        val image = itemView.findViewById<ImageView>(R.id.profileImageArea)

        // 카드에 있는 유저의 정보들을 가져온다.
        val face = itemView.findViewById<TextView>(R.id.itemFace)
        val nickname = itemView.findViewById<TextView>(R.id.itemName)
        val age = itemView.findViewById<TextView>(R.id.itemAge)
        val city = itemView.findViewById<TextView>(R.id.itemCity)

        val lolnickname = itemView.findViewById<TextView>(R.id.itemLOLNickname)
        val lolposition = itemView.findViewById<TextView>(R.id.itemLOLPostion)
        val loltier = itemView.findViewById<TextView>(R.id.itemLOLTier)


        // 카드의 정보에 연결하는 binding 연결 함수.
        fun binding(data : UserDataModel){

            // Firebase에 저장된 이미지를 가져온다.
            val storageRef = Firebase.storage.reference.child(data.uid + ".png")
            storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task->
                if(task.isSuccessful){
                    Glide.with(context)
                        .load(task.result)
                        .into(image)
                }

            })
            face.text = data.face
            nickname.text = data.nickname
            age.text = data.age
            city.text = data.city
            lolnickname.text = data.lolname
            lolposition.text = data.position
            loltier.text = data.loltier
        }
    }
}