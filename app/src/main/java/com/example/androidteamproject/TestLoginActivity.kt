package com.example.androidteamproject

import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.androidteamproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class TestLoginActivity : AppCompatActivity() { // 로그인 화면
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    /*val db = Firebase.firestore
    var myNickName =""
    val userProfiles = db.collection("userProfiles")
    var timer = Timer()
    var timeCnt = 0*/

    /*fun activateTimer(){
        //타이머 동작 시간 지정 및 작업 내용 지정
        timer.schedule(object : TimerTask(){
            override fun run(){
                //카운트 값 증가
                timeCnt ++
                if(timeCnt > 30){
                    runOnUiThread {
                        Toast.makeText(
                            //this@LoginActivity,
                            this@TestLoginActivity,
                            "30초가 넘었습니다. 네트워크 상태를 확인해 보세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    timer.cancel()
                }
            }
        },1000, 1000) //1초뒤 실행, 1초 마다 반복
    }
    fun inActivateTimer(){
        Toast.makeText(
            //this@LoginActivity,
            this@TestLoginActivity,
            "로그인을 성공했습니다.",
            Toast.LENGTH_SHORT
        ).show()
        timer.cancel()
    }
    fun inActivateTimer2(){
        Toast.makeText(
            //this@LoginActivity,
            this@TestLoginActivity,
            "로그인을 실패했습니다.",
            Toast.LENGTH_SHORT
        ).show()
        timer.cancel()
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        setContentView(binding.root)
        //findViewById<Button>(R.id.LoginButton).setOnClickListener(){
        /*binding.LoginButton.setOnClickListener(){
            println("로그인 버튼 클릭")
            if(binding.LoginEmailEditText.getText().toString()=="") {    // nickname을 입력하지 않았을 때
                println("이메일을 입력하지않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("로그인 실패")
                    .setMessage("이메일을 입력해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if(binding.LoginPWEditText.getText().toString()=="") {    // nickname을 입력하지 않았을 때
                println("비밀번호를 입력하지않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("로그인 실패")
                    .setMessage("비밀번호를 입력해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            } else {
                activateTimer()// 로그인 시간 30초이하로 성공 타이머 종료
                Firebase.auth.signInWithEmailAndPassword(
                    binding.LoginEmailEditText.getText().toString(),
                    binding.LoginPWEditText.getText().toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            inActivateTimer() // 로그인 시간 30초이하로 성공 타이머 종료
                            userProfiles.get().addOnSuccessListener {
                                for (user in it) {
                                    if ("${user["email"]}" == binding.LoginEmailEditText.getText()
                                            .toString()
                                    ) {
                                        myNickName = "${user["nickName"]}"
                                        Toast.makeText(
                                            this,
                                            "${myNickName}님 환영합니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                println("로그인 성공")
                                // 로그인 성공을 알리는 팝업창
                                val builder = AlertDialog.Builder(this)
                                builder.setTitle("로그인 성공")
                                    .setMessage("로그인에 성공하였습니다.")
                                    .setPositiveButton(
                                        "확인",
                                        DialogInterface.OnClickListener { dialog, id ->
                                            // 게시물 리스트 화면으로 이동 (PostingsActivity)
                                            //val intent = Intent(this, PostingsActivity::class.java)
                                            val intent = Intent(this, PostingsActivity2::class.java)
                                            intent.putExtra("userNickName", myNickName)
                                            startActivity(intent)
                                        })
                                builder.create()
                                builder.show()
                            }
                        } else {
                            //로그인 실패 오류 출력
                            println("로그인 실패 ${it.exception?.message}")
                            // 로그인 실패를 알리는 팝업창
                            inActivateTimer2() // 로그인 시간 30초이하로 실패 타이머 종료
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("로그인 실패")
                                .setMessage("로그인에 실패하였습니다.")
                                .setPositiveButton(
                                    "확인",
                                    DialogInterface.OnClickListener { dialog, id ->
                                        finish()
                                    })
                            builder.create()
                            builder.show()
                        }
                    }
            }
        }

        //findViewById<Button>(R.id.ToSignupButton).setOnClickListener(){
        binding.ToSignupButton.setOnClickListener(){
            println("회원가입 버튼 클릭")
            //회원가입 화면으로 이동 (SignUpActivity)
            //val intent = Intent(this, SignUpActivity::class.java)
           // startActivity(intent)
        }*/

    }
}