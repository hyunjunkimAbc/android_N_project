package com.example.snsproject

import android.app.Application
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() { // 로그인 화면
    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        setContentView(binding.root)


        //findViewById<Button>(R.id.LoginButton).setOnClickListener(){
        binding.LoginButton.setOnClickListener(){
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
            }
            Firebase.auth.signInWithEmailAndPassword(
                binding.LoginEmailEditText.getText().toString(),
                binding.LoginPWEditText.getText().toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        println("로그인 성공")
                        // 로그인 성공을 알리는 팝업창
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("로그인 성공")
                            .setMessage("로그인에 성공하였습니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                // 게시물 리스트 화면으로 이동 (PostingsActivity)
                                val intent = Intent(this, WritePostActivity::class.java)
                                startActivity(intent)
                            })
                        builder.create()
                        builder.show()
                    }
                    else {
                        //로그인 실패 오류 출력
                        println("로그인 실패 ${it.exception?.message}")
                        // 로그인 실패를 알리는 팝업창
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("로그인 실패")
                            .setMessage("로그인에 실패하였습니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                finish()
                            })
                        builder.create()
                        builder.show()
                    }
                }
        }

        //findViewById<Button>(R.id.ToSignupButton).setOnClickListener(){
        binding.ToSignupButton.setOnClickListener(){
            println("회원가입 버튼 클릭")
            //회원가입 화면으로 이동 (SignUpActivity)
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}