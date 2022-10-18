package com.refael.finalproject.local_db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.refael.finalproject.model.Task

@Dao
interface taskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTask(task:Task)

    @Query("SELECT * FROM tasks ORDER BY title")
    fun getTasks(): LiveData<List<Task>>

    @Delete
    fun deleteTask(vararg Tasks:Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}