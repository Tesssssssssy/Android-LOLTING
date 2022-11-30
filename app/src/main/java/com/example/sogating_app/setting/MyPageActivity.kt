package com.example.sogating_app.setting

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.sogating_app.MAIN.MainActivity
import com.example.sogating_app.Message.img.ImgApi
import com.example.sogating_app.Message.img.ResponseData
import com.example.sogating_app.R
import com.example.sogating_app.auth.IntroActivity
import com.example.sogating_app.auth.UserDataModel
import com.example.sogating_app.utils.FirebaseAuthUtils
import com.example.sogating_app.utils.FirebaseRef
import com.github.kimcore.riot.RiotAPI
import com.github.kimcore.riot.enums.Platform
import com.github.kimcore.riot.enums.Region
import com.github.kimcore.riot.errors.RiotException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*


class MyPageActivity : AppCompatActivity() {
    //키 24시간 마다 초기화!
    val RIOTKEY = "RGAPI-56bd1b93-c675-45c8-855d-57c107c61a04"

    private val TAG = "MyPageActivity::class.java"
    private lateinit var auth: FirebaseAuth
    private val uid = FirebaseAuthUtils.getUid()
    lateinit var myImage: ImageView
    lateinit var myLOLnicknameChange: TextInputEditText

    // 도시 검색 변수 선언
    private var mFusedLocationProviderClient: FusedLocationProviderClient? =
        null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10
    lateinit var mylocation: LatLng
    lateinit var addr: String
    lateinit var pro : ProgressDialog

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


        //음성 채팅 시작
//        voiceChatBtn.setOnClickListener{
//            val intent = Intent(this, VoiceChatActivity::class.java)
//
////            자신의 uid,상대방 uid 필요
////            intent.putExtra("my_uid", my_uid)
////            intnet.putExtra("another_uid",another_uid)
//            startActivity(intent)
//        }
//
//        multiVoiceChatBtn.setOnClickListener {
//            val intent = Intent(this, MultiVoiceActivity::class.java)
//
//            startActivity(intent)
//        }


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

            scanImg()
//            uploadImage(uid)
//            Toast.makeText(this, "변경이 완료 되었습니다", Toast.LENGTH_SHORT).show()
        }

        /* 롤 아이디 등록 및 티어체크 버튼 */

        /* RIOT API setting */
        //임시 api key setting 24시간마다 갱신해야됨!!!
        RiotAPI.setApiKey(RIOTKEY)
        // optional, defaults to KR, ASIA
        RiotAPI.setDefaultPlatform(Platform.KR)
        RiotAPI.setDefaultRegion(Region.ASIA)

        // 티어체크 버튼 클릭
        checktierbtn.setOnClickListener {
            val checkname: String =
                findViewById<TextInputEditText>(R.id.myLOLnicknameChange).text.toString()
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
        // 주소 변경 버튼 클릭
        mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        var citybtn: Button = findViewById(R.id.citybtn)
        citybtn.setOnClickListener {
            if (checkPermissionForLocation(this)) {
                startLocationUpdates()
            }
        }

        // 로그아웃버튼 클릭
        logoutbtn.setOnClickListener {
            val auth = Firebase.auth
            auth.signOut()
            finish()

            val intent = Intent(this, IntroActivity::class.java)
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

    //유저의 사진을 받아  flask_얼굴 인신 서버에 전송하여 얼굴 사진의 여부 파악
    private fun scanImg() {
        //img를 byteArray로 변환
        myImage.isDrawingCacheEnabled = true
        myImage.buildDrawingCache()
        val bitmap = (myImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos)
        val data = baos.toByteArray()
        val imageString: String = Base64.encodeToString(data, Base64.DEFAULT)
        pro = ProgressDialog.show(this, "얼굴인식중입니다","")

        //retrofit를 이용하여 flask_server로 byteArray를 전송
        val api = ImgApi.create();
        val send_json = JSONObject()
        send_json.put("data", imageString)
        Log.d(TAG, "OK_IMG")
        api.getScanResult(send_json).enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var body = response.body().toString()
                Log.d(TAG,"response data :"+body)

                if (body.toString().indexOf('1') != -1){
                    uploadImage(uid)
                    var cel_name = body.substring(34,body.lastIndex)
                    pro.dismiss()
                    Toast.makeText(getApplicationContext(), "얼굴인식 완료, 이미지 변경이 완료 되었습니다.",Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "당신의 닮은꼴 연예인은 " + cel_name + "입니다",Toast.LENGTH_SHORT).show();
                    val myFace = findViewById<TextView>(R.id.myFace)
                    myFace.text = cel_name + " 닮은꼴"
                    FirebaseRef.userInfoRef.child(uid).child("face").setValue(myFace.text)

                }else if(body.toString().indexOf('2') != -1){
                    pro.dismiss()
                    Toast.makeText(getApplicationContext(), "얼굴인식 실패 혼자만 있는 사진을 선택해 주세요",Toast.LENGTH_SHORT).show();
                }
                else{
                    pro.dismiss()
                    Toast.makeText(getApplicationContext(), "얼굴인식 실패 다른 사진을 선택해 주세요",Toast.LENGTH_SHORT).show();
                }

            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Log.d("log", "fail")
                pro.dismiss()
                Toast.makeText(getApplicationContext(), "서버가 불안정 합니다", Toast.LENGTH_SHORT).show();

            }

        })

    }

    // Firebase에서 회원의 정보를 가져오기.
    private fun getMyData() {
        val myImage = findViewById<ImageView>(R.id.myImage)
        val myUid = findViewById<TextView>(R.id.myUID)
        val myFace = findViewById<TextView>(R.id.myFace)
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

                myFace.text = data!!.face
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

    /* 주소 찾기 */
    //주소로 위도,경도 구하는 GeoCoding
    private fun getLatLng(address: String): LatLng {
        val geoCoder = Geocoder(this, Locale.KOREA)   // Geocoder 로 자기 나라에 맞게 설정
        val list = geoCoder.getFromLocationName(address, 3)

        var location = LatLng(37.554891, 126.970814)     //임시 서울역

        if (list != null) {
            if (list.size == 0) {
                Log.d("GeoCoding", "해당 주소로 찾는 위도 경도가 없습니다. 올바른 주소를 입력해주세요.")
            } else {
                val addressLatLng = list[0]
                location = LatLng(addressLatLng.latitude, addressLatLng.longitude)
                return location
            }
        }

        return location
    }

    //위도 경도로 주소 구하는 Reverse-GeoCoding
    private fun getAddress(position: LatLng): String {
        val geoCoder = Geocoder(this, Locale.KOREA)
        var addr = "주소 오류"
        //GRPC 오류? try catch 문으로 오류 대처
        try {
            addr = geoCoder.getFromLocation(position.latitude, position.longitude, 1).first()
                .getAddressLine(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return addr
    }

    private fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치정보를 화면에 갱신해주는 메소드
    fun onLocationChanged(location: Location) {
        mLastLocation = location
//        text2.text = "위도 : " + mLastLocation.latitude // 갱신 된 위도
//        text1.text = "경도 : " + mLastLocation.longitude // 갱신 된 경도

        mylocation = LatLng(mLastLocation.latitude, mLastLocation.longitude) // mylocation 변수 저장
        var buff = getAddress(mylocation).split(" ")
        if(buff[1] == "서울특별시"){
            myCity.text = buff[0] + " " + buff[1] + " " + buff[2]  //내주소
        }else{
            myCity.text = buff[0] + " " + buff[1] + " " + buff[2] + " " + buff[3]  //내주소
        }
        FirebaseRef.userInfoRef.child(uid).child("city").setValue(myCity.text)
    }


    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()

            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}