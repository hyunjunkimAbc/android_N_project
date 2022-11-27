package com.example.snsproject

import android.content.DialogInterface
import android.os.Bundle
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityLoginBinding
import com.example.snsproject.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() { // 회원가입 화면
    private val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        //setContentView(R.layout.activity_signup)

        binding.SignUpButton.setOnClickListener(){
            if(binding.NicknameEditText.getText().toString()==""){    // nickname을 입력하지 않았을 때
                println("닉네임을 입력하지않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원가입 실패")
                    .setMessage("닉네임을 입력해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if (binding.EmailEditText.getText().toString()==""){     // email을 입력하지 않았을 때
                println("이메일을 입력하지 않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원가입 실패")
                    .setMessage("이메일을 입력해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if (binding.PWEditText.getText().toString()==""){     // 비밀번호를 입력하지 않았을 때
                println("비밀번호를 입력하지않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원가입 실패")
                    .setMessage("비밀번호를 입력해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if (binding.NicknameEditText.length()>15){     // 닉네임의 글자수가 15자 초과하면
                println("닉네임 글자수 15자 초과")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원가입 실패")
                    .setMessage("닉네임이 너무 깁니다.\n다시 입력해주시기바랍니다.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if (binding.PWEditText.length()>15){     // 비밀번호 글자수가 15자 초과하면
                println("비밀번호 글자수 15자 초과")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원가입 실패")
                    .setMessage("비밀번호가 너무 깁니다.\n다시 입력해주시기바랍니다.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if (binding.PWEditText.getText().toString()!=binding.PWEditText2.getText().toString()){     // 첫번째 적은 비밀번호와 두번째 적은 비밀번호가 같지 않을때
                println("비밀번호가 같지않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("회원가입 실패")
                    .setMessage("비밀번호가 같지 않습니다." + "다시 입력해주시기바랍니다.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            println("가입하기 버튼 클릭")
            Firebase.auth.createUserWithEmailAndPassword(
                binding.EmailEditText.getText().toString(),
                binding.PWEditText.getText().toString())
                //findViewById<EditText>(R.id.EmailEditText).getText().toString(),
                //findViewById<EditText>(R.id.PWEditText).getText().toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        println("회원가입 성공")
                        // DB postings 컬렉션 레퍼런스 가져오기
                        var user = FirebaseAuth.getInstance().currentUser
                        val col = db.collection("userProfils")
                        val itemMap = hashMapOf(
                            "email" to binding.EmailEditText.getText().toString(),
                            "password" to binding.PWEditText.getText().toString(),
                            "nickName" to binding.NicknameEditText.getText().toString(),
                            "Uid" to (user?.uid ?: String)
                        )
                        col.add(itemMap)
                        //추가: friendCommit에 friendArr 과 nickName
                        val col_friendCommit = db.collection("friendCommit")
                        val itemMap_friendCommit = hashMapOf(
                            "nickName" to binding.NicknameEditText.getText().toString(),
                            "friendArr" to arrayListOf<String>()
                        )
                        col_friendCommit.add(itemMap_friendCommit)

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("회원가입 성공")
                            .setMessage("${binding.NicknameEditText.getText().toString()}님\n회원가입에 성공하였습니다.")
                            //findViewById<EditText>(R.id.NicknameEditText).getText().toString()}님\n회원가입에 성공하였습니다.")
                            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                                finish()
                            })
                        builder.create()
                        builder.show()
                    }
                    else {
                        println("회원가입 실패 ${it.exception?.message}")
                    }
                }
        }
    }
}