package com.refael.finalproject.all_tasks

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.refael.finalproject.R
import com.refael.finalproject.databinding.TaskLayoutBinding
import com.refael.finalproject.model.Task

class TasksAdapter(private val callBack: TaskListener) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    private val tasks = ArrayList<Task>()

    fun setTasks(tasks: Collection<Task>) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
        notifyDataSetChanged()
    }

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
            binding.missionUsername.text = task.taskerName
            binding.missionTv.text = task.title
            binding.missionDescTv.text = task.desc
            binding.missionCb.isChecked = task.finished
            binding.itemPriceTv.text = task.price
            binding.missionTypeTv.text = task.type
            if (task.finished) {
                binding.active.text = binding.root.context.getString(R.string.sold)
                //Hide seller number
                binding.missionPhoneTv.text = "**********"
                //Change color to red
                binding.active.setTextColor(Color.parseColor("#FF0000"))
            } else {
                binding.active.text = binding.root.context.getString(R.string.active)
                binding.missionPhoneTv.text = task.taskerPhone
                //Change color to green
                binding.active.setTextColor(Color.parseColor("#26D32D"))
            }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TaskViewHolder(
        TaskLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bind(tasks[position])

    override fun getItemCount() = tasks.size

    fun taskAt(position: Int) = tasks[position]


}