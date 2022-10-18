package com.refael.finalproject.repository

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refael.finalproject.R
import com.refael.finalproject.databinding.TaskLayoutBinding
import com.refael.finalproject.model.Task

class LocalTasksAdapter(val tasks: List<Task> , val callBack: TaskListener) : RecyclerView.Adapter<LocalTasksAdapter.TaskViewHolder>() {


    interface TaskListener {
        fun onTaskClicked(task: Task)
        fun onTaskLongClicked(task: Task)
    }

    inner class TaskViewHolder(private val binding: TaskLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        fun bind(task: Task) {
            binding.missionPhoneTv.text = task.taskerPhone
            binding.missionUsername.text = task.taskerName
            binding.missionTv.text = task.title
            binding.missionDescTv.text = task.desc
            binding.missionCb.isChecked = task.finished
            binding.itemPriceTv.text = task.price
            binding.missionTypeTv.text = task.type
            binding.active.text = binding.root.context.getString(R.string.ask)
            binding.active.setTextColor(Color.parseColor("#26D32D"))
            Glide.with(binding.root).load(task.image).into(binding.missionIv)
            Glide.with(binding.root).load(task.taskerImage).into(binding.taskProfileImage)
        }

        override fun onClick(p0: View?) {
            callBack.onTaskClicked(tasks[adapterPosition])
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onTaskLongClicked(tasks[adapterPosition])
            return false
        }
    }

    fun taskAt(position: Int) = tasks[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskViewHolder(TaskLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bind(tasks[position])

    override fun getItemCount() = tasks.size
}