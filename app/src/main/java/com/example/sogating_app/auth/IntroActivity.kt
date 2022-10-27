package com.example.sogating_app.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.sogating_app.MAIN.MatchingActivity
import kotlinx.android.synthetic.main.activity_intro.*
import com.example.sogating_app.R
import com.example.sogating_app.databinding.ActivityIntroBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class IntroActivity : AppCompatActivity() {

    private var _binding: ActivityIntroBinding? = null
    private val binding get() = _binding!!
    private lateinit var user: FirebaseAuth

    //GOOGLE LOGIN
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    val GOOGLE_REQUEST_CODE = 99
    private val TAG = "Google Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        _binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //GOOGLE LOGIN
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.googleLoginBtn.setOnClickListener {
            signIn()
        }


        //카카오 로그인을 위한 keyHash 확인 코드
        val keyHash = Utility.getKeyHash(this)
        Log.d("Hash", keyHash)
        //keyHash= n4nStQm5anIyI5Alzl2q/jhwDZo=

        /** KakoSDK init */
        KakaoSdk.init(this, this.getString(R.string.kakao_app_key))


        /** Click_listener */
        binding.btnStartKakaoLogin.setOnClickListener {
            kakaoLogin() //로그인

        }
//        binding.btnStartKakaoLogout.setOnClickListener {
//            kakaoLogout() //로그아웃
//        }
//        binding.btnStartKakaoUnlink.setOnClickListener {
//            kakaoUnlink() //연결해제
//        }


        // DataBinding, ViewBinding
        joinbtn.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        val loginBtn: Button = findViewById(R.id.loginbtn)
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    //google login 관련 함수
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...)
        if (requestCode == GOOGLE_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                //Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "로그인 성공")
                    val user = auth!!.currentUser

                    loginSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun loginSuccess(){
        val intent = Intent(this, JoinActivity::class.java)
        startActivity(intent)
        finish()
    }

    //    private fun updateUI(user: FirebaseUser?) {
//        if(user != null) {
//            val intent = Intent(applicationContext, MyPageActivity::class.java)
//            intent.putExtra(EXTRA_NAME, user.displayName)
//            startActivity(intent)
//        }
//    }

    //    private fun signInGoogle() {
//        val signInIntent = googleSignInClient.signInIntent
//        launcher.launch(signInIntent)
//    }
//
//    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//                    handleResults(task)
//                }
//    }
//
//    private fun handleResults(task: Task<GoogleSignInAccount>) {
//        if(task.isSuccessful) {
//            val account : GoogleSignInAccount? = task.result
//            if(account != null) {
//                updateUI(account)
//            }
//        }else {
//            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun updateUI(account: GoogleSignInAccount) {
//        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//        auth.signInWithCredential(credential).addOnCompleteListener {
//            if(it.isSuccessful) {
//                val intent : Intent = Intent(this, MainActivity::class.java)
////                intent.putExtra("email", account.email)
////                intent.putExtra("name", account.displayName)
//                startActivity(intent)
//            }else {
//                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    //google login에 필요한 함수들 끝



    //kakao login에 필요한 함수들 시작
    private fun setLogin(bool: Boolean){
        binding.btnStartKakaoLogin.visibility = if(bool) View.GONE else View.VISIBLE
//            binding.btnStartKakaoLogout.visibility = if(bool) View.VISIBLE else View.GONE
//            binding.btnStartKakaoUnlink.visibility = if(bool) View.VISIBLE else View.GONE
    }

    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("KAKAO_LOGIN", "카카오계정으로 로그인 실패 : ${error}")
                setLogin(false)
            } else if (token != null) {
                //TODO: 최종적으로 카카오로그인 및 유저정보 가져온 결과
                UserApiClient.instance.me { user, error ->
                    Log.i("KAKAO_LOGIN", "카카오계정으로 로그인 성공")
                    Log.i("KAKAO_LOGIN", "token: ${token.accessToken}")
                    Log.i("KAKAO_LOGIN", "me: ${user}")
                    setLogin(true)
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("KAKAO_LOGIN", "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Log.i("KAKAO_LOGIN", "카카오톡으로 로그인 성공 ${token.accessToken}")

                    val intent = Intent(this, MatchingActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }

    }

    private fun kakaoLogout(){
        // 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("KAKAO_LOGOUT", "로그아웃 실패. SDK에서 토큰 삭제됨: ${error}")
            }
            else {
                Log.i("KAKAO_LOGOUT", "로그아웃 성공. SDK에서 토큰 삭제됨")
                setLogin(false)
            }
        }
    }

    private fun kakaoUnlink(){
        // 연결 끊기
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e("KAKAO_UNLINK", "연결 끊기 실패: ${error}")
            }
            else {
                Log.i("KAKAO_UNLINK", "연결 끊기 성공. SDK에서 토큰 삭제 됨")
                setLogin(false)
            }
        }
        finish()
    }


}