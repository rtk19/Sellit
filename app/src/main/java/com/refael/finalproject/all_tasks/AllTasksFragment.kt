package com.refael.finalproject.all_tasks

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.refael.finalproject.R
import com.refael.finalproject.util.autoCleared
import com.refael.finalproject.databinding.FragmentAllTasksBinding
import com.refael.finalproject.model.Task
import com.refael.finalproject.FirebaseImpl.AuthRepositoryFirebase
import com.refael.finalproject.FirebaseImpl.TaskRepositoryFirebase
import com.refael.finalproject.repository.LocalTasksRepository
import com.refael.finalproject.ui.MainActivity
import com.refael.finalproject.util.Resource


class AllTasksFragment : Fragment() {

    private var binding : FragmentAllTasksBinding by autoCleared()
    private val viewModel : AllTasksViewModel by viewModels {
        AllTasksViewModel.AllTaskViewModelFactory(
            AuthRepositoryFirebase(),
            TaskRepositoryFirebase(),
            LocalTasksRepository(activity!!.application)
        )
    }
    lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        binding = FragmentAllTasksBinding.inflate(inflater,container,false)


        //Current user id
        uid = viewModel.currentUserID()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Add an annotation to the user if there is no connection
        if (!isOnline(requireContext())) {
            binding.recycler.isVisible = false
            binding.offlineTv.isVisible = true
        }

        //Set the layout according to the orientation of the phone
        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.recycler.layoutManager = GridLayoutManager(requireContext(),2)
        } else {
            binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        }


        binding.recycler.adapter = TasksAdapter(object : TasksAdapter.TaskListener {

            override fun onTaskClicked(task: Task) {
                //Clicking on a task will open a menu of options
                val popupMenu = PopupMenu(requireContext(), binding.offlineTv, Gravity.CENTER)
                popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener{ item ->
                    if(item.itemId == R.id.action_call) {
                        if(task.taskerID == uid) {//Calling is impossible for your own posts
                            Toast.makeText(requireContext(), getString(R.string.your_post) ,Toast.LENGTH_SHORT).show()
                        }
                        else if(task.finished) {//Calling is impossible for sold items
                            Toast.makeText(requireContext(), getString(R.string.already_sold) ,Toast.LENGTH_SHORT).show()
                        }else {
                            //If the item is still available, open calling app with the seller number
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("tel:${task.taskerPhone}")
                            startActivity(intent)
                        }
                    }
                    if(item.itemId == R.id.action_save) {
                        if(task.taskerID == uid) {//Saving posts is impossible for your own posts
                            Toast.makeText(requireContext(), getString(R.string.your_post) ,Toast.LENGTH_SHORT).show()
                        }
                        else if(task.finished) {//Saving posts is impossible for sold items
                            Toast.makeText(requireContext(), getString(R.string.already_sold) ,Toast.LENGTH_SHORT).show()
                        }else {
                            //If the item is still available, saving it for later local database
                            viewModel.addLocalTask(task)
                            Toast.makeText(requireContext(),getString(R.string.post_saved) ,Toast.LENGTH_SHORT).show()
                        }
                    }
                    if(item.itemId == R.id.action_sms) {
                        if(task.taskerID == uid) {//SMS sending is impossible for your own posts
                            Toast.makeText(requireContext(), getString(R.string.your_post) ,Toast.LENGTH_SHORT).show()
                        }
                        else if(task.finished) {//SMS sending is impossible for sold items
                            Toast.makeText(requireContext(), getString(R.string.already_sold) ,Toast.LENGTH_SHORT).show()
                        }else {
                            //If the item is still available, open SMS app with the seller number and a preset message
                            val intent = Intent(Intent.ACTION_VIEW)
                            val text = getString(R.string.sms_purchase) + task.title
                            intent.data = Uri.parse("sms:${task.taskerPhone}")
                            intent.putExtra("sms_body", text)
                            startActivity(intent)
                        }
                    }
                    if(item.itemId == R.id.action_delete) {
                        if(task.taskerID == uid){//Ask the user to make sure he wants to delete the item
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle(getString(R.string.sure_delete))

                                setPositiveButton(getString(R.string.yes)) { _, _ ->
                                    // if user press yes, then delete the item.
                                    viewModel.deleteTask(task.id)
                                }

                                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                }
                                setCancelable(true)
                            }.create().show()
                        } else {//Deleting posts is possible for the user posts only
                            Toast.makeText(requireContext(),getString(R.string.cant_delete) ,Toast.LENGTH_SHORT).show()
                        }
                    }
                    if(item.itemId == R.id.action_status) {
                        if(uid == task.taskerID){//Set as sold is impossible for your own posts
                            viewModel.setCompleted(task.id,!task.finished)
                            Toast.makeText(requireContext(),getString(R.string.update) ,Toast.LENGTH_SHORT).show()
                        } else {
                        Toast.makeText(requireContext(), getString(R.string.cant_set_sold) ,Toast.LENGTH_SHORT).show()
                        }
                    }

                    true
                })
                popupMenu.show()
            }

            override fun onTaskLongClicked(task: Task) {
            }
        })

        viewModel.taskStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    (binding.recycler.adapter as TasksAdapter).setTasks(it.data!!)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }

            }
        }

        viewModel.addTaskStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Snackbar.make(binding.coordinator,getString(R.string.post_added),Snackbar.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }

            }
        }

        viewModel.deleteTaskStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Snackbar.make(binding.coordinator,getString(R.string.post_deleted),Snackbar.LENGTH_SHORT)
                        .show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //Item menu inflater
        inflater.inflate(R.menu.main_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Logout
        if(item.itemId == R.id.action_sign_out) {
            viewModel.signOut()
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    //Check internet connection
    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
        return false
    }
}