package com.refael.finalproject.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.refael.finalproject.model.Task

@Database(entities = [Task::class], version = 8, exportSchema = false)
abstract class TaskDatabase: RoomDatabase() {
    abstract fun tasksDao(): taskDao

    companion object {
        @Volatile
        private var instance: TaskDatabase? = null

        fun getDatabase(context: Context) = instance ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, TaskDatabase::class.java, "tasks_db").fallbackToDestructiveMigration()
                .allowMainThreadQueries().build()
        }
    }
}