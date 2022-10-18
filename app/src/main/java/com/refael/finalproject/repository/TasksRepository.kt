package com.refael.finalproject.repository

import androidx.lifecycle.MutableLiveData
import com.refael.finalproject.model.Task
import com.refael.finalproject.util.Resource
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    suspend fun addTask(taskerName:String, taskerPhone:String, taskerID:String, taskerImage:String, title:String, desc:String, image: String, price:String, type: String) : Resource<Void>
    suspend fun deleteTask(taskId: String): Resource<Void>
    suspend fun setCompleted(taskId:String, boolean: Boolean): Resource<Void>
    suspend fun getTask(id:String) : Resource<Task>
    suspend fun getTasks() : Resource<List<Task>>

    fun getTasksFlow() : Flow<Resource<List<Task>>>
    fun getTasksLiveData(data : MutableLiveData<Resource<List<Task>>>)
}