package com.refael.finalproject.repository

import android.app.Application
import com.refael.finalproject.local_db.TaskDatabase
import com.refael.finalproject.local_db.taskDao
import com.refael.finalproject.model.Task

class LocalTasksRepository(application: Application) {

    private var tasksDao: taskDao?

    init {
        val db = TaskDatabase.getDatabase(application.applicationContext)
        tasksDao = db?.tasksDao()
    }

    fun getTasks() = tasksDao?.getTasks()

    fun addTask(task: Task){
        tasksDao?.addTask(task)
    }

    fun deleteTask(task:Task){
        tasksDao?.deleteTask(task)
    }

}