package com.example.androidteamproject

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidteamproject.databinding.FriendCommentBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PostingsAdapter2 (private val viewModel: PostingsViewModel2):RecyclerView.Adapter<PostingsAdapter2.ViewHolder>(){
    inner class ViewHolder(private val binding: FriendCommentBinding):RecyclerView.ViewHolder(binding.root){
        fun setContents(pos : Int){
            with(viewModel.items[pos]){
                binding.imageView.setImageBitmap(this.icon)
                binding.textView.text = nickName
                binding.textView2.text = comment

                val db = Firebase.firestore
                val friendCommit = db.collection("friendCommit")


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
        val binding = FriendCommentBinding.inflate(layoutInflater,viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.setContents(position)
    }

    override fun getItemCount()=viewModel.items.size


}