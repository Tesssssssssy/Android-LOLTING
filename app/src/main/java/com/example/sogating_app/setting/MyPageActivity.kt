package com.example.sogating_app.setting

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.github.kimcore.riot.RiotAPI
import com.github.kimcore.riot.enums.Platform
import com.github.kimcore.riot.enums.Region
import com.github.kimcore.riot.errors.RiotException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MyPageActivity : AppCompatActivity() {

    private val TAG = "MyPageActivity::class.java"
    private lateinit var auth: FirebaseAuth
    private val uid = FirebaseAuthUtils.getUid()
    lateinit var myImage: ImageView
    lateinit var myLOLnicknameChange: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        //뒤로가기 버튼 누르면 메인액티비티로 감
        var back_button = findViewById<ImageView>(R.id.back_button_img)
        back_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        getMyData()

        /* 이미지 클릭해서 교체하기 */
        myImage = findViewById(R.id.myImage)
        // 이미지를 클릭하면 핸드폰에 저장되어있는 이미지들을 불러옴.
        // registerForActivityResult()를 통해서 핸드폰에 저장되어있는 저장소에 접근한다.
        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                myImage.setImageURI(uri)
            }
        )
        // getAction.launch() 메소드를 통해서 저장된 이미지를 변경.
        myImage.setOnClickListener {
            getAction.launch("image/*")
        }

        // 이미지 변경버튼
        changebtn.setOnClickListener {
            uploadImage(uid)
            Toast.makeText(this, "변경이 완료 되었습니다", Toast.LENGTH_SHORT).show()
        }

        /* 롤 아이디 등록 및 티어체크 버튼 */

        /* RIOT API setting */
        //임시 api key setting 24시간마다 갱신해야됨!!!
        RiotAPI.setApiKey("RGAPI-3f86d30b-2c4e-4cd7-81ea-592bfabbde78")
        // optional, defaults to KR, ASIA
        RiotAPI.setDefaultPlatform(Platform.KR)
        RiotAPI.setDefaultRegion(Region.ASIA)

        // 티어체크 버튼 클릭
        checktierbtn.setOnClickListener {
            val checkname: String = findViewById<TextInputEditText>(R.id.myLOLnicknameChange).text.toString()
            var tierString: String

            // tiercheck 함수 실행, suspend 함수라서 코루틴 사용해야함
            CoroutineScope(Dispatchers.Main).launch {
                tierString = tierCheck(checkname)

                if (tierString == "NO SUMMONER" || tierString == "UNRANKED") { //롤 아이디가 없거나 솔로랭크 티어가 없는 경우 -> 다시 입력해주세요

                } else { //솔로랭크 티어가 있는경우 -> 롤 계정이 등록 되었습니다.
                    val myTier = findViewById<TextView>(R.id.myTier)
                    val myLOLnickname = findViewById<TextView>(R.id.myLOLnickname)
                    myTier.text = tierString
                    myLOLnickname.text = checkname

                    //파이어베이스 uid 에 롤관련 데이터 추가
                    FirebaseRef.userInfoRef.child(uid).child("lolname").setValue(checkname)
                    FirebaseRef.userInfoRef.child(uid).child("loltier").setValue(tierString)
                }


            }
        }

        // 로그아웃버튼 클릭
        logoutbtn.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    /* tierCheck 해서 문자열로 반환하는 함수 */
    suspend fun tierCheck(name: String): String {
        // 소환사가 없을 경우 NO SUMMONER 반환
        try {
            RiotAPI.summoner.getSummonerByName(name)
        } catch (e: RiotException) {
            Log.d("error", e.statusCode.toString()) // 404
            Log.d("error", e.statusMessage) // Data not found
            return "NO SUMMONER"
        }

        // 소환사 데이터 받아오기
        val summoner = RiotAPI.summoner.getSummonerByName(name)
        // 소환사 리그 데이터 받아오기
        val leagueEntries = RiotAPI.league.getLeagueEntriesBySummonerID(summoner.id)

        // 만약 유저가 unrank 이거나 배치고사를 보지 않아 솔로랭크티어가 없는 경우
        if (leagueEntries.toString() == "[]") {
            return "UNRANKED"
            // 솔로 랭크 티어가 있는 경우
        } else {
            val soloTier: String =
                leagueEntries.first { it.queueType == "RANKED_SOLO_5x5" }.tier
            return soloTier
        }

    }

    // 이미지 재저장
    private fun uploadImage(uid: String) {
        // Firebase 저장되는 경로 지정.
        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".png")

        // Get the data from an ImageView as bytes
        myImage.isDrawingCacheEnabled = true
        myImage.buildDrawingCache()
        val bitmap = (myImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }


    // Firebase에서 회원의 정보를 가져오기.
    private fun getMyData() {
        val myImage = findViewById<ImageView>(R.id.myImage)
        val myUid = findViewById<TextView>(R.id.myUID)
        val myNickname = findViewById<TextView>(R.id.myNickname)
        val myAge = findViewById<TextView>(R.id.myAge)
        val myCity = findViewById<TextView>(R.id.myCity)
        val myGender = findViewById<TextView>(R.id.myGender)
        val myLOLPosition = findViewById<TextView>(R.id.myLOLPostion)
        val myLOLnickname = findViewById<TextView>(R.id.myLOLnickname)
        val myLOLtier = findViewById<TextView>(R.id.myTier)


        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                Log.w(TAG, dataSnapshot.toString())
                val data = dataSnapshot.getValue(UserDataModel::class.java)

                //uid 는 받아와서 저장하지만 레이아웃에선 visiblity 를 gone 하여 감춰둠
                myUid.text = data!!.uid

                myNickname.text = data!!.nickname
                myAge.text = data!!.age
                myCity.text = data!!.city
                myGender.text = data!!.gender
                myLOLPosition.text = data!!.position
                myLOLnickname.text = data!!.lolname
                myLOLtier.text = data!!.loltier

                // Firebase에 저장된 이미지를 가져온다.
                val storageRef = Firebase.storage.reference.child(data.uid + ".png")
                storageRef.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Glide.with(baseContext)
                            .load(task.result)
                            .into(myImage)
                    }
                })

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        // 데이터가 어디에 정의되어 있는 냐?
        FirebaseRef.userInfoRef.child(uid).addValueEventListener(postListener)
    }
}