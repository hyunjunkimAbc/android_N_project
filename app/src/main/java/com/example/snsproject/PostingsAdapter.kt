package com.example.snsproject

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.snsproject.databinding.ItemLayoutBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostingsAdapter (private val viewModel: PostingsViewModel):RecyclerView.Adapter<PostingsAdapter.ViewHolder>(){
    inner class ViewHolder(private val binding: ItemLayoutBinding):RecyclerView.ViewHolder(binding.root){
        fun setContents(pos : Int){
            with(viewModel.items[pos]){
                binding.imageView.setImageBitmap(this.icon)
                binding.textView.text = title
                binding.textView2.text = nickName
                //친구 관계인지 아닌지에 따라서 button의 색상 처리
                if(this.statCode=="0"){//친구 아님
                    binding.button.setBackgroundColor(Color.CYAN)
                    binding.button.text ="친구추가"
                    if(this.nickName == viewModel.loginUserName){
                        binding.button.setBackgroundColor(Color.MAGENTA)
                        binding.button.text ="me"
                    }
                }else if(this.statCode == "1"){//친구인 상태
                    binding.button.setBackgroundColor(Color.RED)
                    binding.button.text = "친구삭제"
                }else if(this.statCode=="2"){//login user가 업로더에게 친구 요청을 하고 있는 관계
                    //업로더에 버튼에 표시되게 하는 것은 나중에
                    binding.button.setBackgroundColor(Color.GRAY)
                    binding.button.text = "친구요청중"
                }else if(this.statCode =="3"){
                    binding.button.setBackgroundColor(Color.GRAY)
                    binding.button.text = "상대가 친구요청중"
                }

                binding.button.setOnClickListener {
                    //친구 요청 or 친구 삭제 요청
                    //문제 생기면 싱글톤으로 만들어 버리기
                    val db = Firebase.firestore
                    val friendCommit = db.collection("friendCommit")

                    if(this.statCode=="0"){//친구 아닌 상태에서 친구 추가 버튼을 눌렀음
                        if(this.nickName == viewModel.loginUserName){
                        }else{
                            addFriend(friendCommit,this.nickName) // 업로더의 닉네임
                            binding.button.setBackgroundColor(Color.RED)
                            binding.button.text = "친구삭제"
                        }
                    }else if(this.statCode=="1"){//친구인 상태에서 친구 삭제 버튼을 눌럿음
                        deleteFriendRelation(friendCommit, this.nickName)
                        binding.button.setBackgroundColor(Color.CYAN)
                        binding.button.text ="친구추가"
                    }else if(this.statCode =="2") {//로그인 한 사용자가 이미 친구를 요청했음
                        //아무 행동도 안되게 설정함
                    }else if(this.statCode =="3"){// 상대방이 나한테 친구 요청을 한 상태에서 이 버튼을 눌렀음
                        //누르면 친구 관계 설정됨
                        addFriend(friendCommit,this.nickName)
                        binding.button.setBackgroundColor(Color.RED)
                        binding.button.text = "친구삭제"
                    }
                }
            }


            binding.root.setOnClickListener {
                viewModel.itemClickEvent.value = adapterPosition
            }
            binding.root.setOnLongClickListener {
                viewModel.itemLongClick = adapterPosition
                false
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val binding = ItemLayoutBinding.inflate(layoutInflater,viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setContents(position)
    }

    override fun getItemCount()=viewModel.items.size

    fun deleteFriendRelation(friendCommit :CollectionReference,nickname:String){
        // user1과 user2가 친구를 맺고 있었다면 둘다 서로를 배열에서 없애 버린다.
        //업로더에서 삭제
        val loginName = viewModel.loginUserName
        var i =0;
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

                if("${user["nickName"]}"== nickname){//업로더에서 삭제
                    for(friend in friendArr){
                        if("${friend.trim()}" == loginName){

                        }else{
                            arrayList.add("${friend.trim()}")
                        }
                    }
                    val docData = hashMapOf(
                        "nickName" to nickname,
                        "friendArr" to arrayList
                    )

                    friendCommit.document("${user.id}")
                        .set(docData)
                        .addOnSuccessListener { System.out.println("nickname업뎃 성공"); }
                        .addOnFailureListener { e ->  System.out.println(e); }
                }else if("${user["nickName"]}" == loginName){//login user에서 업로더 삭제
                    for(friend in friendArr){
                        if("${friend.trim()}" == nickname){

                        }else{
                            arrayList.add("${friend.trim()}")
                        }
                    }
                    val docData = hashMapOf(
                        "nickName" to loginName,
                        "friendArr" to arrayList
                    )

                    friendCommit.document("${user.id}")
                        .set(docData)
                        .addOnSuccessListener { System.out.println("loginName업뎃 성공"); }
                        .addOnFailureListener { e ->  System.out.println(e); }
                }
            }
        }
    }
    fun addFriend(friendCommit:CollectionReference,nickname: String){
        //friendCommit 컬랙션에서 로그인 한 user와 게시물을 올린 업로더가 서로 배열에 들어가 있는지 체크한다.

        //만약 둘다 서로가 배열에 없다면 로그인 한 user가 게시물 업로더에게 친구요청을 한 것으로 간주하고 user의 배열에만 추가한다.
        //(PostingsActivity에서는 이러한 관계를 일방적으로 친구요청을 한 관계라고 간주한다.

        //만약 로그인한 user는 배열에 게시물 업로더가 없는데 게시물 업로더는 로그인한 user를 배열에 가지고 있다면 의사의 합치가
        //발생한 것으로 간주하고 둘은 친구관계로 처리한다.(PostingsActivity에서)

        //어쨌든 로그인 한 user가 업로더를 본인의 배열로 추가하는 것임

        val loginName = viewModel.loginUserName
        var i =0;
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

                if("${user["nickName"]}"== loginName){//업로더를 로그인 유저의 배열에 추가
                    for(friend in friendArr){
                        arrayList.add("${friend.trim()}")

                    }
                    arrayList.add(nickname)
                    val docData = hashMapOf(
                        "nickName" to loginName,
                        "friendArr" to arrayList
                    )
                    friendCommit.document("${user.id}")
                        .set(docData)
                        .addOnSuccessListener { System.out.println("nickname업뎃 성공"); }
                        .addOnFailureListener { e ->  System.out.println(e); }
                }
            }
        }

        //이 함수는 친구 관계가 아닐때만 작동하기 때문에 둘다 배열에 있을때는 처리 하지 않아도 된다.
        //user.getDocumentReference("").set
    }

}