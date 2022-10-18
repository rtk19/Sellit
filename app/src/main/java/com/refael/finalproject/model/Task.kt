package com.refael.finalproject.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = false) val id:String="",
    //Seller info
    val taskerName:String="",
    val taskerPhone:String="",
    val taskerID:String="",
    val taskerImage:String="",
    //Item info
    val title:String="",
    val desc: String = "",
    val image: String = "",
    val price: String = "",
    val type: String = "",
    //Finished = Sold
    var finished:Boolean = false) : Parcelable