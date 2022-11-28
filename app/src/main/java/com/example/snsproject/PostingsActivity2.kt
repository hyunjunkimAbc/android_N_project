package com.example.snsproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snsproject.databinding.ActivityPostBinding


import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostingsActivity2 : AppCompatActivity(){
    private val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }
    //private lateinit var viewModel : PostingsViewModel
    private val viewModel : PostingsViewModel2 by viewModels<PostingsViewModel2>()
    val db = Firebase.firestore

    var isInit =false
    var myNickName ="star1"//닉네임은 일단 알고 있다고 가정함 login Activity에서 넘겨 받아야 함


    var rootRef = Firebase.storage.reference
    val posts = db.collection("postings")

    val friendCommit = db.collection("friendCommit")
    val userProfiles = db.collection("userProfils")
    var postId : String = "1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        postId = intent?.getStringExtra("postId") ?: ""
        val adapter = PostingsAdapter2(viewModel)
        binding.recyclerViewComment.adapter = adapter
        binding.recyclerViewComment.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewComment.setHasFixedSize(true)
        //myNickName = intent?.getStringExtra("userNickName") ?: ""
        //viewModel.loginUserName = myNickName

        posts.get().addOnSuccessListener {
            for(post in it){
                if("${post.id}" == postId){
                    myNickName = "${post["nickName"]}"
                    var ref = rootRef.child("user_profile_image/${myNickName}.jpg")
                    ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
                        if(it.isSuccessful){
                            val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                            binding.profileImg.setImageBitmap(bmp)
                        }else{
                            var ref = rootRef.child("user_profile_image/default.jpg")
                            ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
                                if(it.isSuccessful){
                                    val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                                    binding.profileImg.setImageBitmap(bmp)
                                }else{
                                    println("undefined err")
                                }
                            }
                        }
                    }
                }
            }
        }


        /*binding.helpButton.setOnClickListener {
            //help activity로 이동
            val intent = Intent(this,PostingsHelpActivity::class.java)
            startActivity(intent)
        }
        binding.profileButton.setOnClickListener {
            //profile 등록 activity로 이동
            val intent = Intent(this, ProfileEditActivity::class.java)
            intent.putExtra("userNickName",myNickName)
            startActivity(intent)
        }
        binding.logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            //로그인 activity로 강제 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.postButton.setOnClickListener {
            //게시글 작성 activity로 강제 이동
            val intent = Intent(this, WritePostActivity::class.java)
            startActivity(intent)
        }*/


        viewModel.itemsListData.observe(this){
            adapter.notifyDataSetChanged()
        }

        registerForContextMenu(binding.recyclerViewComment)

        //테스트용 로그인
        /*
        userProfiles.get().addOnSuccessListener {
            for(user in it){
                if(user["nickName"] == myNickName){
                    Firebase.auth.signInWithEmailAndPassword(user["email"] as String,user["password"] as String)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                println("login success")
                                println(Firebase.auth.currentUser?.uid)
                            }else{
                                println("login faild")
                            }
                        }
                }
            }
        }*/

        /*Uid로 게시글 받아오기
        content 본문
        image 프로필 사진
        nickname
        title*/

        posts.get().addOnSuccessListener {
            for(post in it) {
                var imgUrl = ""
                var nickname = ""
                var title = ""
                var content = ""
                //
                if(post.id ==null){
                    continue
                }
                if( "${post.id}" == postId ){
                    imgUrl = "${post["image"]}"
                    nickname = "${post["nickName"]}"
                    title = "${post["title"]}"
                    content = "${post["content"]}"
                    //commentList = "${post["commentList"]}"
                    // 게시글 정보
                    addPostingData(imgUrl,nickname, title, content)
                    //addPostingData(imgUrl,nickname, title, content, commentList)
                }
            }
            //println("${post.id}  ${post["title"]} ${post["nickName"]}");
            isInit = true
        }
        //게시글 id title nickName 변동시 혹은 문서 자체가 삭제 되었거나 추가 되었을때

        //컬랙션 postingsActivityData
        // 문서이름 = 유니크한 번호(post1 post2)
        // 문서 속성 = user이름,title,본문 , 첨부파일 url(실제 이미지는 firebaseStorage 별도 폴더에)

        //user프로필 사진은 'user의 닉네임'.jpg로 저장되어 있음

    }



    // imgUrl,nickname, title, content
    fun addPostingData(imgUrl: String, nickname: String, title:String,content: String) {
        var ref = rootRef.child("image/${imgUrl}")

        binding.posterName.setText(nickname)
        binding.postTitle.setText(title)
        binding.postingContent.setText(content)

        ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
            if(it.isSuccessful){
                val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                binding.postingImg.setImageBitmap(bmp)
                //viewModel.addItem(Item(bmp, nickname,comment))
            }
            else{
                //신규 유저라 프사가 없는 경우 기본 이미지 (title, nickname)
                //processAddDefaultIcon(title, nickname)
            }
        }

    }



}