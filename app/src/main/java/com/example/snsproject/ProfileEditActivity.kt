package com.example.snsproject

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityProfileEditBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.regex.Pattern


class ProfileEditActivity : AppCompatActivity(){
    private val binding by lazy {
        ActivityProfileEditBinding.inflate(layoutInflater)
    }
    var rootRef = Firebase.storage.reference
    val db = Firebase.firestore
    val userProfils = db.collection("userProfils")
    val friendCommit = db.collection("friendCommit")
    val postings = db.collection("postings")
    var myNickName :String =""
    var newNickName = ""
    var newEmail=""
    var newPassWord =""

    var currentImageUrl : Uri? = null

    var timer = Timer()
    var timeCnt = 0


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("requestCode: $requestCode  resultCode  $resultCode")
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == 1){
                currentImageUrl = data?.data
                println("image " + currentImageUrl)

                try{
                    var bitmapUserSelect = MediaStore.Images.Media.getBitmap(contentResolver,currentImageUrl)
                    binding.imageButton.setImageBitmap(bitmapUserSelect)

                } catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }
    fun activateTimer(){
        //????????? ?????? ?????? ?????? ??? ?????? ?????? ??????
        timer.schedule(object : TimerTask(){
            override fun run(){
                //????????? ??? ??????
                timeCnt ++
                if(timeCnt > 30){
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileEditActivity,
                            "30?????? ???????????????. ???????????? ????????? ????????? ?????????",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    timer.cancel()
                }
            }
        },1000, 1000) //1?????? ??????, 1??? ?????? ??????
    }
    fun inActivateTimer(){
        Toast.makeText(
            this@ProfileEditActivity,
            "????????? ????????? ??????????????????.",
            Toast.LENGTH_SHORT
        ).show()
        println("--????????? ????????? ??????")
        timer.cancel()
    }

    fun uploadProfileEdit(){
        newNickName =  binding.nickNameEditText.text.toString()
        newEmail = binding.emailEditText.text.toString()
        newPassWord = binding.passwordEditText.text.toString()
        activateTimer() // ????????? ??????
        /*
        if(file_id !=null && fileName != null){
            uploadFile(file_id,fileName)
        }*/

        if(currentImageUrl != null){
            val imageRef = rootRef.child("user_profile_image/${newNickName}.jpg")
            //val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,file_id)
            imageRef?.putFile(currentImageUrl!!)?.addOnSuccessListener{
                println("upload complete")

            }
        }

        //userProfil?????? ?????? auth?????? ?????? ???
        //Firebase.auth.createUserWithEmailAndPassword("b@b.com","123456")

        userProfils.get().addOnSuccessListener {
            for(user in it){
                if("${user["nickName"]}" ==myNickName){
                    println("login success")
                    println("currentUser-------------------"+Firebase.auth.currentUser?.uid)
                    Firebase.auth.currentUser?.updateEmail(newEmail)?.addOnSuccessListener {
                        Firebase.auth.currentUser?.updatePassword(newPassWord)?.addOnSuccessListener {
                            val docData = hashMapOf(
                                "Uid" to (Firebase.auth.currentUser!!.uid ?: String),
                                "nickName" to newNickName,
                                "email" to newEmail,
                                "password" to newPassWord
                            )
                            userProfils.add(docData).addOnSuccessListener {
                                userProfils.document("${user.id}").delete().addOnSuccessListener {
                                    var arrayList = arrayListOf<String>()
                                    friendCommit.get().addOnSuccessListener {
                                        //System.out.println("it ${it}");
                                        for(user in it){//???????????? ????????????
                                            //?????? ????????? ????????? ???????????? ??????  ex> ?????? -?????????
                                            println("${user.id}:id arrayOf(temp[friendArr ${user["friendArr"]}")
                                            var tempStr = "${user["friendArr"]}"
                                            tempStr = tempStr.substring(1,tempStr.length-1)
                                            var friendArr :List<String>
                                            friendArr = tempStr.split(',')
                                            arrayList = arrayListOf<String>()
                                            var docData = hashMapOf(
                                                "nickName" to newNickName,
                                                "friendArr" to arrayList
                                            )
                                            if("${user["nickName"]}"== myNickName){//?????? ????????? ??????
                                                for(friend in friendArr){
                                                    arrayList.add("${friend.trim()}")

                                                }

                                                docData = hashMapOf(
                                                    "nickName" to newNickName,
                                                    "friendArr" to arrayList
                                                )

                                            }else{//?????? ?????? ????????? ????????? ??????
                                                for(friend in friendArr){
                                                    if("${friend.trim()}" == myNickName){
                                                        arrayList.add(newNickName)
                                                    }else{
                                                        arrayList.add("${friend.trim()}")
                                                    }

                                                }
                                                docData = hashMapOf(
                                                    "nickName" to "${user["nickName"]}",
                                                    "friendArr" to arrayList
                                                )
                                            }
                                            friendCommit.document("${user.id}")
                                                .set(docData)
                                                .addOnSuccessListener {
                                                    System.out.println("nickname?????? ??????")
                                                    var isFirst =true // ????????? ????????? ???????????? ????????? ?????? ????????????
                                                    postings.get().addOnSuccessListener {
                                                        for(post in it){//???????????? ????????????
                                                            //?????? ????????? ????????? ???????????? ??????  ex> ?????? -?????????

                                                            if("${post["nickName"]}"== myNickName){//???????????? ????????? ????????? ????????? ??????
                                                                isFirst = false // ???????????? ?????? ????????? ????????? ??????
                                                                val docData = hashMapOf(
                                                                    "nickName" to newNickName,
                                                                    "title" to "${post["title"]}"
                                                                )//????????? ????????? ????????? ?????? ??? ??? ??????
                                                                postings.document("${post.id}")
                                                                    .set(docData)
                                                                    .addOnSuccessListener { System.out.println("nickname?????? ??????");
                                                                        inActivateTimer()//??????????????? ????????? ??????
                                                                        val intent = Intent(this, PostingsActivity::class.java)
                                                                        intent.putExtra("userNickName",newNickName)
                                                                        startActivity(intent)
                                                                    }
                                                                    .addOnFailureListener { e ->  System.out.println(e); }
                                                            }
                                                        }
                                                        if(isFirst){//?????? ????????? ???????????? ??????????????? ???????????? PostingsActivity??? ??????
                                                            inActivateTimer()//??????????????? ????????? ??????
                                                            val intent = Intent(this, PostingsActivity::class.java)
                                                            intent.putExtra("userNickName",newNickName)
                                                            startActivity(intent)
                                                        }
                                                    }

                                                }
                                                .addOnFailureListener { e ->  System.out.println(e) }
                                        }
                                    }

                                }
                            }
                        }
                    }

                }

            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        myNickName = intent?.getStringExtra("userNickName") ?: ""
        println("my Nick" + myNickName)
        var ref = rootRef.child("user_profile_image/${myNickName}.jpg")



        ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
            if(it.isSuccessful){
                val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                binding.imageButton.setImageBitmap(bmp)
            }else{
                var ref = rootRef.child("user_profile_image/default.jpg")
                ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
                    if(it.isSuccessful){
                        val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                        binding.imageButton.setImageBitmap(bmp)
                    }else{
                        println("undefined err")
                    }
                }
            }
        }
        userProfils.get().addOnSuccessListener {
            for (user in it){
                if("${user.data["nickName"]}" == myNickName){
                    binding.nickNameEditText.setText( "${user.data["nickName"]}")
                    binding.emailEditText.setText( "${user.data["email"]}")
                    binding.passwordEditText.setText("${user.data["password"] }")
                }
            }
        }
        binding.imageButton.setOnClickListener {
            /*
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,null,null,null)
                AlertDialog.Builder(this)
                    .setTitle("Choose Photo")
                    .setCursor(cursor,{_,i->
                        cursor?.run {
                            moveToPosition(i)
                            val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                            val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                            file_id = getLong(idIdx)
                            fileName = getString(nameIdx)

                            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                file_id!!
                            )
                            var bitmap = MediaStore.Images.Media.getBitmap(contentResolver,contentUri)
                            binding.imageButton.setImageBitmap(bitmap)
                        }
                    },MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()
            }else{
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }*/

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
            startActivityForResult(intent, 1)

        }


        binding.helpButton2.setOnClickListener {
            //help activity??? ??????
            val intent = Intent(this,ProfileEditHelpActivity::class.java)
            startActivity(intent)
        }
        //????????? ?????? ???????????? ????????? ?????? ????????? ??????.
        binding.logoutButton2.setOnClickListener {
            Firebase.auth.signOut()
            //????????? activity??? ?????? ??????
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
        binding.postButton2.setOnClickListener {
            //????????? ?????? activity??? ?????? ??????
            val intent = Intent(this, WritePostActivity::class.java)
            startActivity(intent)
        }



        binding.uploadButton.setOnClickListener{
            //friendCommit ??? ?????? id ??? userProfile??? id ??????  postings????????? nickName ??????
            newNickName =  binding.nickNameEditText.text.toString()
            newEmail = binding.emailEditText.text.toString()
            newPassWord = binding.passwordEditText.text.toString()

            if(newNickName.length > 15){
                Toast.makeText(this,"???????????? 15????????? ?????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Pattern.matches("^[a-zA-Z???-???0-9]*$",newNickName)){
                Toast.makeText(this,"???????????? ????????? ????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(newPassWord.length > 15){
                Toast.makeText(this,"??????????????? 15????????? ?????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Pattern.matches("^[a-zA-Z0-9]*$",newPassWord)){
                Toast.makeText(this,"??????????????? ????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var isDuplicate = false
            userProfils.get().addOnSuccessListener {
                for (user in it) {
                    if ("${user["nickName"]}" != myNickName) {
                        if("${user["email"]}" == newEmail){
                            Toast.makeText(this,"???????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
                            isDuplicate = true
                        }
                        /*
                        if("${user["password"]}" == newPassWord){
                            Toast.makeText(this,"????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
                            isDuplicate = true
                        }*/
                    }
                }
                if(isDuplicate==false){
                    uploadProfileEdit()
                }
            }



        }


    }


}