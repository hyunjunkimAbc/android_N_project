package com.example.snsproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityImageCheckBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.net.URL

class imageCheck : AppCompatActivity() { // (임시테스트)파이어스토어에서 이미지 가져오는 화면
    private val binding by lazy {
        ActivityImageCheckBinding.inflate(layoutInflater)
    }
    val db = Firebase.firestore
    val rootRef = Firebase.storage.reference

    //val st = FirebaseStorage.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //setContentView(R.layout.activity_image_check)

        var user = FirebaseAuth.getInstance().currentUser
        var user_uid = user?.uid ?: String


        var imageName : String? = null
        val postCol = db.collection("postings")
        postCol.get().addOnSuccessListener {
            for (d in it) {
                println("자신의 문서(document) 찾는중")
                if(user_uid.toString()==d["uid"]) {    //d["nickname"]=="hi1"
                    println("자신의 문서(document)에서 이미지 이름을 찾음")
                    imageName = d["image"].toString()
                    println(imageName)
                    val ref = rootRef.child("image/"+imageName!!) // 이미지 파일 이름 가져옴
                    println("check1")
                    ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
                        println("check2")
                        if (it.isSuccessful){
                            println("check3")
                            val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
                            binding.imageView2.setImageBitmap(bmp)
                        }
                    }
                }
            }
        }
    }
}