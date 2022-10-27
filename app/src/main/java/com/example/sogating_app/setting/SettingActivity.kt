package com.example.sogating_app.setting

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import com.example.sogating_app.Message.MyLikeListActivity
import com.example.sogating_app.Message.MyMsgActivity
import com.example.sogating_app.R
import com.example.sogating_app.auth.IntroActivity
import com.example.sogating_app.databinding.ActivityIntroBinding
import com.example.sogating_app.databinding.ActivitySettingBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.installations.Utils
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private var _binding: ActivitySettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        _binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 구글 로그아웃을 위해 로그인 세션 가져오기
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()


        // 푸시 메시지 보내는 방법
        // 1. 앱에서 코드로 notification 띄우기
        // 2. Firebase 콘솔에서 모든 앱에게 PUSH 보내기
        // 3. 매칭이 완료된 특정 사용자에게 메시지 보내기 (Firebase console에서)
        // 4. Firebase console이 아니라 , 앱에서 직접 다른 사람에게 푸시메시지 보내기.


        fun kakaoLogout() {
            // 로그아웃
            UserApiClient.instance.logout { error ->
                if (error != null) {
                    Log.e("LOGOUT", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                } else {
                    Log.i("LOGOUT", "로그아웃 성공. SDK에서 토큰 삭제됨")
                }
            }
        }

        fun kakaoUnlink() {
            // 연결 끊기
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.e("UNLINK", "연결 끊기 실패", error)
                } else {
                    Log.i("UNLINK", "연결 끊기 성공. SDK에서 토큰 삭제 됨")
                }
            }
            finish()
        }
        var google_sign_out_button = findViewById<Button>(R.id.google_logout_Btn)

        google_sign_out_button.setOnClickListener {
            //로그아웃
            FirebaseAuth.getInstance().signOut()
            googleSignInClient?.signOut()
            var logoutIntent = Intent(this, IntroActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(logoutIntent)

            kakaoLogout()
            val intent2 = Intent(this, IntroActivity::class.java)
            startActivity(intent2)

        }

        findViewById<Button>(R.id.google_logout_Btn).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, IntroActivity::class.java))
        }

        btn_start_kakao_unlink.setOnClickListener {
            kakaoUnlink()

            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }
    }
}