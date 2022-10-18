package com.refael.finalproject.FirebaseImpl

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.refael.finalproject.model.Task
import com.refael.finalproject.repository.TasksRepository
import com.refael.finalproject.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.refael.finalproject.util.safeCall

class TaskRepositoryFirebase : TasksRepository {

    private val taskRef = FirebaseFirestore.getInstance().collection("tasks")

    override suspend fun addTask(
        taskerName: String,
        taskerPhone: String,
        taskerID:String,
        taskerImage:String,
        title: String,
        desc: String,
        image: String,
        price: String,
        type: String
    ): Resource<Void> = withContext(Dispatchers.IO) {

        safeCall {
            val taskId = taskRef.document().id
            val task = Task(taskId, taskerName, taskerPhone, taskerID, taskerImage, title, desc, image, price, type)
            val addition = taskRef.document(taskId).set(task).await()
            Resource.Success(addition)
        }
    }

    override suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result  = taskRef.document(taskId).delete().await()
            Resource.Success(result)
        }
    }

    override suspend fun setCompleted(taskId: String, boolean: Boolean)= withContext(Dispatchers.IO) {
        safeCall {
            val result  = taskRef.document(taskId).update("finished",boolean).await()
            Resource.Success(result)
        }
    }

    override suspend fun getTask(id: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = taskRef.document(id).get().await()
            val task  = result.toObject(Task::class.java)
            Resource.Success(task!!)
        }
    }

    override suspend fun getTasks()= withContext(Dispatchers.IO) {
        safeCall {
            val result  = taskRef.get().await()
            val tasks = result.toObjects(Task::class.java)
            Resource.Success(tasks)
        }
    }

    override fun getTasksFlow(): Flow<Resource<List<Task>>>  = callbackFlow {

        val snapshotListener = taskRef.orderBy("title").addSnapshotListener {
                value, error ->

            val response = if(value!=null) {
                val tasks  = value.toObjects(Task::class.java)
                Resource.Success(tasks)
            }else {
                Resource.Error(error?.message ?: error.toString())
            }
            trySend(response)
        }
        awaitClose {
            snapshotListener.remove()
        }

    }

    override fun getTasksLiveData(data: MutableLiveData<Resource<List<Task>>>) {

        data.postValue(Resource.Loading())

        taskRef.orderBy("title").addSnapshotListener {snapshot, e ->
            if(e != null) {
                data.postValue(Resource.Error(e.localizedMessage))
            }
            if(snapshot != null && !snapshot.isEmpty) {
                data.postValue(Resource.Success(snapshot.toObjects(Task::class.java)))
            }
            else {
                data.postValue(Resource.Error("No Data"))
            }
        }
    }
}