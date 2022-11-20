package com.example.snsproject

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Item(val icon:Bitmap,val nickName: String ,val title :String,val postId:String,val statCode :String);

class PostingsViewModel : ViewModel() {
    val itemsListData = MutableLiveData<ArrayList<Item>>()
    val items = ArrayList<Item>()

    val itemClickEvent = MutableLiveData<Int>()
    var itemLongClick = -1
    var loginUserName="star1"

    fun addItem(item: Item){
        items.add(item);
        itemsListData.value = items;
    }
    fun updateItem(pos : Int ,item : Item){
        items[pos] = item
        itemsListData.value = items
    }
    fun deleteItem(item:Item){
        //items.removeAt(pos)
        items.remove(item)
        itemsListData.value = items
    }
}