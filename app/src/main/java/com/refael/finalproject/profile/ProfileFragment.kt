package com.refael.finalproject.profile

import android.app.*
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.refael.finalproject.*
import com.refael.finalproject.databinding.FragmentProfileBinding
import com.refael.finalproject.model.Task
import com.refael.finalproject.FirebaseImpl.AuthRepositoryFirebase
import com.refael.finalproject.FirebaseImpl.TaskRepositoryFirebase
import com.refael.finalproject.repository.LocalTasksAdapter
import com.refael.finalproject.repository.LocalTasksRepository
import com.refael.finalproject.ui.MainActivity
import com.refael.finalproject.all_tasks.AllTasksViewModel
import com.refael.finalproject.util.autoCleared
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding by autoCleared()
    private val viewModel : AllTasksViewModel by activityViewModels {
        AllTasksViewModel.AllTaskViewModelFactory(
            AuthRepositoryFirebase(),
            TaskRepositoryFirebase(),
            LocalTasksRepository(activity!!.application)
        )
    }
    var name: String = ""
    var image: String = ""
    var phone = ""
    var email = ""

    private suspend fun getCurrentName(): String{
        return viewModel.currentUserName()
    }

    private suspend fun getCurrentPhone(): String{
        return viewModel.currentPhone()
    }

    private fun getCurrentEmail(): String{
        return viewModel.currentUserEmail()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private suspend fun getCurrentImage(): String{
        return viewModel.currentUserImage()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            //Force update to the local data
            val task = Task()
            viewModel.addLocalTask(task)
            viewModel.deleteLocalTask(task)

            //Get User info
            name = getCurrentName()
            image = getCurrentImage()
            phone = getCurrentPhone()
            email = getCurrentEmail()
            binding.emailProfileFrag.text = email
            binding.phoneProfileFrag.text = phone
            binding.profileFragmentUsername.text = name
            binding.fullNameProfileFrag.text = name
            Glide.with(binding.root).load(image).into(binding.profileImage)
        }

        //Set the layout according to the orientation of the phone
        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.recycler.layoutManager = GridLayoutManager(requireContext(),2)
        } else {
            binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        }

        //Logout
        binding.logoutBtn.setOnClickListener {
            viewModel.signOut()
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
        }

        viewModel.localTasks?.observe(viewLifecycleOwner) {
            binding.recycler.adapter = LocalTasksAdapter(it, object : LocalTasksAdapter.TaskListener{

                override fun onTaskClicked(task: Task) {
                    //Clicking on a task will open a menu of options
                    val popupMenu = PopupMenu(requireContext(), binding.imagesSaveBtn, Gravity.CENTER)
                    popupMenu.menuInflater.inflate(R.menu.local_popup_menu, popupMenu.menu)
                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener{ item ->
                        if(item.itemId == R.id.action_call) {
                            //Open a dialog box to warn the user the item might be sold, since the item is saved offline
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle(getString(R.string.notice))
                                setMessage(getString(R.string.might_be_sold))

                                setPositiveButton(getString(R.string.ok)) { _, _ ->
                                    // if user press ok, open calling app with the seller number
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse("tel:${task.taskerPhone}")
                                    startActivity(intent)
                                }

                                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                }
                                setCancelable(true)
                            }.create().show()
                        }

                        if(item.itemId == R.id.action_remove) {//Ask the user to make sure he wants to remove the item
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle(getString(R.string.sure_delete))

                                setPositiveButton(getString(R.string.yes)) { _, _ ->
                                    // if user press yes, then deletes local saved post.
                                    viewModel.deleteLocalTask(task)
                                }

                                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                }
                                setCancelable(true)
                            }.create().show()
                        }
                        if(item.itemId == R.id.action_sms) {
                            //Open a dialog box to warn the user the item might be sold, since the item is saved offline
                            AlertDialog.Builder(requireContext()).apply {
                                setTitle(getString(R.string.notice))
                                setMessage(getString(R.string.might_be_sold))

                                setPositiveButton(getString(R.string.ok)) { _, _ ->
                                    //If the item is still available, open SMS app with the seller number and a preset message
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    val text = getString(R.string.sms_purchase) + task.title
                                    intent.data = Uri.parse("sms:${task.taskerPhone}")
                                    intent.putExtra("sms_body", text)
                                    startActivity(intent)
                                }

                                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                                }
                                setCancelable(true)
                            }.create().show()

                        }
                        true
                    })
                    popupMenu.show()
                }

                override fun onTaskLongClicked(task: Task) {
                }
            })
        }

    }

    companion object {

    }
}