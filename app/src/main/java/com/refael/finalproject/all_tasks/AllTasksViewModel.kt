package com.refael.finalproject.all_tasks

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.refael.finalproject.util.Resource
import com.refael.finalproject.model.Task
import com.refael.finalproject.repository.AuthRepository
import com.refael.finalproject.repository.LocalTasksRepository
import com.refael.finalproject.repository.TasksRepository
import kotlinx.coroutines.launch

class AllTasksViewModel(private val authRep: AuthRepository, private val taskRep: TasksRepository, private val localTaskRep: LocalTasksRepository) : ViewModel() {

    //With LiveData
    private val _tasksStatus : MutableLiveData<Resource<List<Task>>> = MutableLiveData()
    val taskStatus: LiveData<Resource<List<Task>>> = _tasksStatus

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userRef = FirebaseFirestore.getInstance().collection("users")

    private val _addTaskStatus = MutableLiveData<Resource<Void>>()
    val addTaskStatus:LiveData<Resource<Void>> = _addTaskStatus

    private val _deleteTaskStatus = MutableLiveData<Resource<Void>>()
    val deleteTaskStatus:LiveData<Resource<Void>> = _deleteTaskStatus

   init {
        taskRep.getTasksLiveData(_tasksStatus)
    }

    suspend fun currentUserName():String{
        return authRep.currentUser().data!!.name
    }

    suspend fun currentPhone():String{
        return authRep.currentUser().data!!.phone
    }

    fun currentUserID():String{
        return firebaseAuth.currentUser!!.uid
    }

    fun currentUserEmail():String{
        return firebaseAuth.currentUser!!.email!!
    }

    suspend fun currentUserImage():String{
        return authRep.currentUser().data!!.image
    }

    fun signOut() {
        authRep.logout()
    }

    fun addTask(taskerName:String, taskerPhone:String, taskerID:String, taskerImage:String, title:String, desc:String, image:String, price:String, type:String) {
        viewModelScope.launch {
            if(title.isEmpty())
                _addTaskStatus.postValue(Resource.Error("Empty post title"))
            else {
                _addTaskStatus.postValue(Resource.Loading())
                _addTaskStatus.postValue(taskRep.addTask(taskerName, taskerPhone, taskerID, taskerImage ,title, desc, image, price, type))
            }
        }
    }

    fun deleteTask(id:String) {
        viewModelScope.launch {
            if(id.isEmpty())
                _deleteTaskStatus.postValue(Resource.Error("Empty task ID"))
            else {
                _deleteTaskStatus.postValue(Resource.Loading())
                _deleteTaskStatus.postValue(taskRep.deleteTask(id))
            }
        }
    }

    fun setCompleted(id:String, boolean: Boolean) {
        viewModelScope.launch {
            taskRep.setCompleted(id,boolean)
        }
    }


    val localTasks: LiveData<List<Task>>? = localTaskRep.getTasks()

    fun addLocalTask(task:Task){
        viewModelScope.launch {
            localTaskRep.addTask(task)
        }
    }

    fun deleteLocalTask(task:Task){
        viewModelScope.launch {
            localTaskRep.deleteTask(task)
        }
    }

    class AllTaskViewModelFactory(private val authRepo:AuthRepository, private val taskRep:TasksRepository, private val localTaskRep: LocalTasksRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AllTasksViewModel(authRepo,taskRep, localTaskRep) as T
        }
    }
}