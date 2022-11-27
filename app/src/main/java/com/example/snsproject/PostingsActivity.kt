package com.example.snsproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.snsproject.databinding.ActivityPostingsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostingsActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPostingsBinding.inflate(layoutInflater)
    }
    //private lateinit var viewModel : PostingsViewModel
    private val viewModel : PostingsViewModel by viewModels<PostingsViewModel>()
    val db = Firebase.firestore

    var isInit =false
    var myNickName ="star1"//닉네임은 일단 알고 있다고 가정함 login Activity에서 넘겨 받아야 함


    var rootRef = Firebase.storage.reference
    val posts = db.collection("postings")

    val friendCommit = db.collection("friendCommit")
    val userProfiles = db.collection("userProfils")
    //val friendRequest = db.collection("friendRequest")
    //val friend = db.collection("friend")
    //val userProfils = db.collection("userProfils")
    //var isFriend = false
    //var isFriend = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val adapter = PostingsAdapter(viewModel)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        myNickName = intent?.getStringExtra("userNickName") ?: ""
        viewModel.loginUserName = myNickName



        var ref = rootRef.child("user_profile_image/${myNickName}.jpg")
        ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
            if(it.isSuccessful){
                val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                binding.profileButton.setImageBitmap(bmp)
            }else{
                var ref = rootRef.child("user_profile_image/default.jpg")
                ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
                    if(it.isSuccessful){
                        val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                        binding.profileButton.setImageBitmap(bmp)
                    }else{
                        println("undefined err")
                    }
                }
            }
        }


        binding.helpButton.setOnClickListener {
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
        }


        viewModel.itemsListData.observe(this){
            adapter.notifyDataSetChanged()
        }
        viewModel.itemClickEvent.observe(this){
            //ItemDialog(it).show
            val i =viewModel.itemClickEvent.value
            if(viewModel.items[i!!].statCode =="1"){//친구
                //게시물 acitivity 로 post id 실어서 이동

            }else{//친구 아님
                Toast.makeText(this,"친구 관계만 게시물을 볼 수 있습니다.",Toast.LENGTH_SHORT).show()
            }

        }
        registerForContextMenu(binding.recyclerView)

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
        }



        //여기다 Firebase.auth.currentUser 를 출력하려 하면
        // 네트워크 비동기 작업이 안끝났는데 실행한다ㅏ. 아직 안받아온거를 리턴할 수는 없다.
        /*
        Firebase.auth.signInWithEmailAndPassword("a@ab.com","123456")
            .addOnCompleteListener {
                if(it.isSuccessful){
                    println("sign up success")
                    println(Firebase.auth.currentUser?.uid)

                }else {
                    println("sign up faild ${it.exception?.message}")
                }
            }*/
        //throw RuntimeException("dfasdfasd")
        //인증 안해도 받아오게 하려면 storage의 rule을 바꿔야 한다.

        //친구 관계 변동시
        friendCommit.addSnapshotListener { value, error ->
            if (value ==null) return@addSnapshotListener
            for(d in value!!.documentChanges){
                if ("${d.type}" =="REMOVED"){
                    /*
                    for(k in viewModel.items){
                        if(k.nickName == "${d.document.id}"){//ㅕuser id == 문서 id
                            viewModel.deleteItem(k)
                        }
                    }*/
                    //해당 회원이 탈퇴 할 경우에 대해서 정의 하지 않았음
                }else if("${d.type}" =="MODIFIED"){//친구 관계 수정 되었을때
                    var i =0;
                    for(k in viewModel.items) {
                        System.out.println("k.nickName "+k.nickName+"docu id"  +"${d.document.id}" );
                        System.out.println("ok");

                        if ( k.nickName == "${d.document["nickName"]}" || "${d.document["nickName"]}" ==myNickName) {//데이터 변경자의 게시물일때와 로그인 사용자의 데이터가 변경될때만 변경하면됨
                            updatePostingData(k.postId,k.title,k.nickName,i) //글 작성자와 본인과의 관계와 데이터 변경자의 관계를 고려 해야 함
                            System.out.println("stat "+k.statCode) //윗 줄은 friendCommit의 nickName이 회원정보 수정으로 변동 될수 있어서 그렇게 처리함
                        }else{// 나머지 모든 경우는 패스
                        }
                        i++ //모든 게시물을 다 조회한다.
                    }
                }else if("${d.type}" =="ADDED"){
                    /*
                    if(isInit){//새로운 회원이 추가되어서 friendCommit 에 목록이 추가 되는 경우 게시글 을 쓴 것이 아니라 추가 할 필요 없다.
                        var title =""
                        var nickname=""
                        var id = ""
                        id ="${d.document.id}"
                        title = "${d.document.data["title"]}"
                        nickname ="${d.document.data["nickName"]}"
                        addPostingData(id, title, nickname)
                    }*/
                }
            }
        }
        posts.addSnapshotListener { value, error ->
            if(value == null){ return@addSnapshotListener }
            for(d in value!!.documentChanges){
                //println("${d.type}, ${d.document.id}, ${d.document.data["title"]}")
                if ("${d.type}" =="REMOVED"){
                    for(k in viewModel.items){
                        if(k.postId == "${d.document.id}"){
                            viewModel.deleteItem(k)
                        }
                    }
                }else if("${d.type}" =="MODIFIED"){//친구 관계 변동은 아님  친구 관계는 안바꾸고 그대로 쓰면 됨
                    var i =0;
                    for(k in viewModel.items) {
                        if (k.postId == "${d.document.id}") {
                            var title = ""
                            var nickname = ""
                            var id = ""
                            id = "${d.document.id}"
                            title = "${d.document.data["title"]}"
                            nickname = "${d.document.data["nickName"]}"

                            updatePostingData(id,title,nickname,i)
                            break;
                        }else{
                            i++
                        }
                    }

                }else if("${d.type}" =="ADDED"){
                    if(isInit){
                        var title =""
                        var nickname=""
                        var id = ""
                        id ="${d.document.id}"
                        title = "${d.document.data["title"]}"
                        nickname ="${d.document.data["nickName"]}"
                        addPostingData(id, title, nickname)

                    }
                }else{
                    System.out.println("undefined")
                }
            }
        }//게시물 변하면 화면에 반영

        //post id는 유일해야함 time 같은 거?
        //처음에 넣을때 id도 같이넣고 삭제된 아이디와 일치하는 것을 리사이클러 뷰에서 제거 REMOVED
        // 수정될때 MODIFIED
        //추가 될때 ADDED

        //게시글 문서들을 다 받아 왔을 때
        posts.get().addOnSuccessListener {
            for(post in it) {// 게시글 문서들을 다 받아와서 문서마다 추가한다.
                var title = ""
                var nickname = ""
                var id = ""
                if(post.id ==null){
                    continue
                }
                id = "${post.id}"
                title = "${post["title"]}"
                nickname = "${post["nickName"]}"
                addPostingData(id,title,nickname)
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
    fun processAddDefaultIcon(id:String,title:String,nickname: String,statCode: String){
        var ref = rootRef.child("user_profile_image/default.jpg")
        ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
            if(it.isSuccessful){
                val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                viewModel.addItem(Item(bmp, nickname,title,id,statCode))
            }else{
                println("undefined err")
            }
        }
    }
    fun processUpdateDefaultIcon(id:String,title:String,nickname: String,statCode: String,i :Int){
        var ref = rootRef.child("user_profile_image/default.jpg")
        ref.getBytes(Long.MAX_VALUE).addOnCompleteListener{
            if(it.isSuccessful){
                val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                viewModel.updateItem(i,Item(bmp, nickname,title,id,statCode))
            }else{
                println("undefined err")
            }
        }
    }
    fun updatePostingData(id:String,title:String,nickname: String,i:Int) {
        var ref = rootRef.child("user_profile_image/${nickname}.jpg")

        var statCode ="0"
        var isLoginUserContainUploader = false
        var isUploaderContainLoginUser = false

        friendCommit.get().addOnSuccessListener {
            //System.out.println("it ${it}");
            for(user in it){//문서들을 얻어온다
                //본인 이름을 필드로 넣어보고 확인  ex> 본인 -상대방
                println("${user.id}:id arrayOf(temp[friendArr ${user["friendArr"]}")
                var tempStr = "${user["friendArr"]}"
                tempStr = tempStr.substring(1,tempStr.length-1)
                var friendArr :List<String>
                friendArr = tempStr.split(',')
                //게시물 작성자는 데이터 변경자임
                if("${user["nickName"]}"== myNickName){
                    for(friend in friendArr){
                        System.out.println("friend ${friend} friend.trim()'${friend.trim()}' myNickName'${myNickName}'")
                        if(friend.trim() == nickname){//업로더를 login 한 자가 친구로 하고 있다.
                            System.out.println("friend.trim() == myNickName friend ${friend}")
                            //isFriend = true
                            isLoginUserContainUploader = true
                        }
                    }
                }else if("${user["nickName"]}" == nickname){
                    for(friend in friendArr){
                        System.out.println("friend ${friend} friend.trim()'${friend.trim()}' myNickName'${myNickName}'")
                        if(friend.trim() == myNickName){//login 한 자를 업로더가 친구로 하고 있다.
                            System.out.println("friend.trim() == myNickName friend ${friend}")
                            //isFriend = true
                            isUploaderContainLoginUser = true
                        }
                    }
                }
            }
            if(isLoginUserContainUploader ==true && isUploaderContainLoginUser==false){
                //로그인 유저가 친구 요청중
                statCode ="2"
            }else if(isLoginUserContainUploader== false && isUploaderContainLoginUser == true){
                //업로더가 친구 요청중
                statCode ="3"
            }else if(isLoginUserContainUploader ==true && isUploaderContainLoginUser ==true) {
                //친구 관계
                statCode ="1"
            }else{
                //친구 관계 아님
                statCode ="0"
            }
            ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
                if(it.isSuccessful){
                    val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                    viewModel.updateItem(i, Item(bmp,nickname, title, id,statCode))
                }
                else{
                    //신규 유저라 프사가 없는 경우 기본 이미지
                    processUpdateDefaultIcon(id,title,nickname,statCode,i)
                }
            }

        }
    }
    fun addPostingData(id:String,title:String,nickname: String) {
        var ref = rootRef.child("user_profile_image/${nickname}.jpg")
        //친구 관계인지 확인
        //val isFriend = isFriend(friendCommit,userProfils,nickname)
        //System.out.println("isFriend ${isFriend}")
        //boolean 값을 int 형 statCode로 바꾸기
        //그전에 친구 요청 상태 점검 알고리즘 부터
        //var isFriend = false
        var statCode ="0"

        var isLoginUserContainUploader = false
        var isUploaderContainLoginUser = false
        friendCommit.get().addOnSuccessListener {
            //System.out.println("it ${it}");
            for(user in it){//문서들을 얻어온다
                //본인 이름을 필드로 넣어보고 확인  ex> 본인 -상대방
                println("${user.id}:id arrayOf(temp[friendArr ${user["friendArr"]}")
                var tempStr = "${user["friendArr"]}"
                tempStr = tempStr.substring(1,tempStr.length-1)
                var friendArr :List<String>
                friendArr = tempStr.split(',')


                if("${user["nickName"]}"==myNickName){//업로더가 자기 자신인 경우도 처리 해야함 boolean 이 아니고int code로 해야할듯
                    //login 한 사용자의 친구 목록에 업로더가 있는지

                    System.out.println("temp.id ${user.id}")
                    for(friend in friendArr){
                        System.out.println("friend ${friend} friend.trim()'${friend.trim()}' myNickName'${myNickName}'")
                        if(friend.trim() == nickname){//업로더의 친구 목록에 login user가 있는지
                            System.out.println("friend.trim() == myNickName friend ${friend}")
                            //isFriend = true
                            isLoginUserContainUploader = true
                            break
                        }
                    }
                    continue
                }
                if("${user["nickName"]}"== nickname){//업로더의 id와 friendCommit의 문서 id가 일치하면
                    System.out.println("temp.id ${user.id}")
                    for(friend in friendArr){
                        System.out.println("friend ${friend} friend.trim()'${friend.trim()}' myNickName'${myNickName}'")
                        if(friend.trim() == myNickName){//업로더의 친구 목록에 login user가 있는지
                            System.out.println("friend.trim() == myNickName friend ${friend}")
                            //isFriend = true
                            isUploaderContainLoginUser = true

                            break
                        }
                    }
                    if(isLoginUserContainUploader && isUploaderContainLoginUser){
                        break
                    }
                }

            }
            if(isLoginUserContainUploader ==true && isUploaderContainLoginUser==false){
                //로그인 유저가 친구 요청중
                statCode ="2"
            }else if(isLoginUserContainUploader== false && isUploaderContainLoginUser == true){
                //업로더가 친구 요청중
                statCode ="3"
            }else if(isLoginUserContainUploader ==true && isUploaderContainLoginUser ==true) {
                //친구 관계
                statCode ="1"
            }else{
                //친구 관계 아님
                statCode ="0"
            }
            ref.getBytes(Long.MAX_VALUE).addOnCompleteListener {
                if(it.isSuccessful){
                    val bmp = BitmapFactory.decodeByteArray(it.result,0,it.result.size)
                    viewModel.addItem(Item(bmp, nickname,title,id,statCode))
                }
                else{
                    //신규 유저라 프사가 없는 경우 기본 이미지
                    processAddDefaultIcon(id,title,nickname,statCode)
                }
            }

        }
    }



}