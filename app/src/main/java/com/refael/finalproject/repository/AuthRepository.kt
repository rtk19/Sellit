package com.refael.finalproject.repository

import com.refael.finalproject.model.User
import com.refael.finalproject.util.Resource

interface AuthRepository {
    suspend fun currentUser() : Resource<User>
    suspend fun login(email:String, password:String) : Resource<User>
    suspend fun createUser(userName:String,
                           userEmail:String,
                           userPhone:String,
                           userImage:String,
                           userLoginPass:String) : Resource<User>
    fun logout()
}