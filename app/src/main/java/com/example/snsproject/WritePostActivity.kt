package com.example.snsproject

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.snsproject.databinding.ActivityWritePostBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class WritePostActivity  : AppCompatActivity() { // 게시글 작성 화면
    val db = Firebase.firestore
    val st = FirebaseStorage.getInstance()
    private val binding by lazy {
        ActivityWritePostBinding.inflate(layoutInflater)
    }

    private val OPEN_GALLERY = 1
    lateinit var storage : FirebaseStorage
    //var bitmap2: String? = ""

    companion object {
        const val REQUEST_CODE = 1
        const val UPLOAD_FOLDER = "upload_images/"
    }

    var image : Uri? = null
    var imgName : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Firebase.auth.currentUser ?: finish()

        storage = Firebase.storage
        val storageRef = storage.reference

        binding.imgBtn.setOnClickListener(){ // 첨부파일 이미지 버튼 클릭
            println("이미지 버튼 클릭")
            //uploadDialog()
            // 갤러리로 이동하여 이미지 파일을 이미지뷰에 가져옴
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
            startActivityForResult(intent, OPEN_GALLERY)
        }

        binding.postBtn.setOnClickListener(){   // 게시글 작성 포스트 버튼 클릭
            println("포스트 버튼 클릭")
            val title:String = binding.editTextTitle.getText().toString()
            val content:String = binding.editTextContent.getText().toString()

            if (title.length > 0 && content.length > 0 ) { // 게시글 제목과 내용이 작성되면
                println("포스팅 성공")
                uploadImageToStorage() // 이미지 storage로 업로드
                var user = FirebaseAuth.getInstance().currentUser
                var user_uid = user?.uid ?: String
                var nickname : String? = ""
                val nb = db.collection("userProfils")
                nb.get().addOnSuccessListener {
                    println("Firebase userProfils 컬렉션으로 접근 성공")
                    for(d in it){
                        println("자신의 문서(document) 찾는중")
                        if(user_uid.toString()==d["Uid"]) {
                            println("자신의 문서(document) 찾음")
                            nickname = d["nickname"].toString()
                        }
                    }
                    val itemMap = hashMapOf(
                        "uid" to (user?.uid ?: String),
                        "nickname" to nickname,
                        "title" to binding.editTextTitle.getText().toString(),
                        "content" to binding.editTextContent.getText().toString(),
                        "image" to imgName
                    )
                    val postCol = db.collection("postings")
                    println("Firebase로 데이터 전송")
                    postCol.add(itemMap)


                    // 게시글 포스트 성공
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("게시글 작성 성공")
                        .setMessage("게시글 작성을 성공하였습니다.")
                        .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                            // 이미지 확인 -> 게시물 리스트 화면으로 대체해야함 (PostingsActivity)
                            val intent = Intent(this,imageCheck::class.java)
                            startActivity(intent)
                        })
                    builder.create()
                    builder.show()


                }
            }
            else if ( title.length <= 0 ){ // 게시글 제목을 작성하지 않은 경우
                println("게시글 제목을 입력하지 않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("게시글 작성 실패")
                    .setMessage("제목이 비었습니다.\n제목을 작성해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
            else if ( content.length <= 0 ){ // 게시글 내용을 작성하지 않은 경우
                println("게시글 내용을 입력하지 않음")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("게시글 작성 실패")
                    .setMessage("내용이 비었습니다.\n내용을 작성해주세요.")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    })
                builder.create()
                builder.show()
            }
        }
    }

    private fun uploadDialog() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null)
            println("check 1")

            AlertDialog.Builder(this)
                .setTitle("Choose Photo")
                .setCursor(cursor, { _, i ->
                    cursor?.run {
                        moveToPosition(i)
                        val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                        val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                        //uploadFile(getLong(idIdx), getString(nameIdx))

                    }
                }, MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()

        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    //private fun uploadFile(file_id: Long?, fileName: String?) {
    //    file_id ?: return
    //    val imageRef = storage.reference.child("${UPLOAD_FOLDER}${fileName}")
    //    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, file_id)
    //    imageRef.putFile(contentUri).addOnCompleteListener {
    //        if (it.isSuccessful) {
    //            // upload success
    //            Snackbar.make(binding.root, "Upload completed.", Snackbar.LENGTH_SHORT).show()
    //        }
    //    }
    //}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                uploadDialog()
            }
        }
    }

    // 갤러리에서 사진을 선택하면 실행되는 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == OPEN_GALLERY){
                image = data?.data
                try{

                    var bitmap = MediaStore.Images.Media.getBitmap(contentResolver,image)
                    binding.imageView.setImageBitmap(bitmap)
                } catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    // Firebase Storage로 이미지 전송
    private fun uploadImageToStorage(){
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imgName = "Image_" + timeStamp + ".png"
        var storageReference= st?.reference?.child("image")?.child(imgName!!)

        storageReference?.putFile(image!!)?.addOnSuccessListener {
            println("이미지 업로드 성공")
        }

    }



    //private fun post(postproperty: PostProperty) {
    //    var db : FirebaseFirestore = FirebaseFirestore.getInstance();
    //    db.collection("posts").add(postproperty)//document(user.getUid()).set(postproperty)
    //        .addOnSuccessListener((OnSuccessListener) (aVoid){
    //            // 게시글 포스트 성공
    //            startToast("게시글 포스트 성공")
    //            finish()
    //        })
    //        .addOnFailureListener((e) {
    //            // 게시글 포스트 실패
    //            startToast("게시글 포스트 실패")
    //            Log.w(TAG, "실패")
    //        })
    //}
    //private fun startToast(msg : String) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }



}