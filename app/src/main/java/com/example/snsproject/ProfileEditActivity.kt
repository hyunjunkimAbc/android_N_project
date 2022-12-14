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
        //타이머 동작 시간 지정 및 작업 내용 지정
        timer.schedule(object : TimerTask(){
            override fun run(){
                //카운트 값 증가
                timeCnt ++
                if(timeCnt > 30){
                    runOnUiThread {
                        Toast.makeText(
                            this@ProfileEditActivity,
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
            this@ProfileEditActivity,
            "프로필 수정을 성공했습니다.",
            Toast.LENGTH_SHORT
        ).show()
        println("--프로필 업로드 성공")
        timer.cancel()
    }

    fun uploadProfileEdit(){
        newNickName =  binding.nickNameEditText.text.toString()
        newEmail = binding.emailEditText.text.toString()
        newPassWord = binding.passwordEditText.text.toString()
        activateTimer() // 타이머 실행
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

        //userProfil에도 넣고 auth에도 넣야 함
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
                                        for(user in it){//문서들을 얻어온다
                                            //본인 이름을 필드로 넣어보고 확인  ex> 본인 -상대방
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
                                            if("${user["nickName"]}"== myNickName){//나의 닉네임 수정
                                                for(friend in friendArr){
                                                    arrayList.add("${friend.trim()}")

                                                }

                                                docData = hashMapOf(
                                                    "nickName" to newNickName,
                                                    "friendArr" to arrayList
                                                )

                                            }else{//다른 사람 배열의 정보도 수정
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
                                                    System.out.println("nickname업뎃 성공")
                                                    var isFirst =true // 최초에 가입한 포스팅이 한개도 없는 유저인지
                                                    postings.get().addOnSuccessListener {
                                                        for(post in it){//문서들을 얻어온다
                                                            //본인 이름을 필드로 넣어보고 확인  ex> 본인 -상대방

                                                            if("${post["nickName"]}"== myNickName){//업로더를 로그인 유저의 배열에 추가
                                                                isFirst = false // 게시글이 있기 때문에 최초가 아님
                                                                val docData = hashMapOf(
                                                                    "nickName" to newNickName,
                                                                    "title" to "${post["title"]}"
                                                                )//게시물 구조가 바뀌면 수정 될 수 있음
                                                                postings.document("${post.id}")
                                                                    .set(docData)
                                                                    .addOnSuccessListener { System.out.println("nickname업뎃 성공");
                                                                        inActivateTimer()//성공했으니 타이머 종료
                                                                        val intent = Intent(this, PostingsActivity::class.java)
                                                                        intent.putExtra("userNickName",newNickName)
                                                                        startActivity(intent)
                                                                    }
                                                                    .addOnFailureListener { e ->  System.out.println(e); }
                                                            }
                                                        }
                                                        if(isFirst){//최초 더라도 프로필은 수정한거는 맞으니까 PostingsActivity로 이동
                                                            inActivateTimer()//성공했으니 타이머 종료
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
            //help activity로 이동
            val intent = Intent(this,ProfileEditHelpActivity::class.java)
            startActivity(intent)
        }
        //프로필 수정 화면이라 프로필 관련 버튼은 없다.
        binding.logoutButton2.setOnClickListener {
            Firebase.auth.signOut()
            //로그인 activity로 강제 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
        binding.postButton2.setOnClickListener {
            //게시글 작성 activity로 강제 이동
            val intent = Intent(this, WritePostActivity::class.java)
            startActivity(intent)
        }



        binding.uploadButton.setOnClickListener{
            //friendCommit 의 문서 id 와 userProfile의 id 수정  postings에서도 nickName 수정
            newNickName =  binding.nickNameEditText.text.toString()
            newEmail = binding.emailEditText.text.toString()
            newPassWord = binding.passwordEditText.text.toString()

            if(newNickName.length > 15){
                Toast.makeText(this,"닉네임은 15글자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Pattern.matches("^[a-zA-Z가-힣0-9]*$",newNickName)){
                Toast.makeText(this,"닉네임은 영어와 한글과 숫자만 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(newPassWord.length > 15){
                Toast.makeText(this,"비밀번호는 15글자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Pattern.matches("^[a-zA-Z0-9]*$",newPassWord)){
                Toast.makeText(this,"비밀번호는 영어와 숫자만 가능합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var isDuplicate = false
            userProfils.get().addOnSuccessListener {
                for (user in it) {
                    if ("${user["nickName"]}" != myNickName) {
                        if("${user["email"]}" == newEmail){
                            Toast.makeText(this,"이메일이 타인과 중복됩니다.", Toast.LENGTH_SHORT).show()
                            isDuplicate = true
                        }
                        /*
                        if("${user["password"]}" == newPassWord){
                            Toast.makeText(this,"비번이 타인과 중복됩니다.", Toast.LENGTH_SHORT).show()
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