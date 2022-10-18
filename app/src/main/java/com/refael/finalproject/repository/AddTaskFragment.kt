package com.refael.finalproject.repository

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.refael.finalproject.databinding.FragmentAddTaskBinding
import com.refael.finalproject.FirebaseImpl.AuthRepositoryFirebase
import com.refael.finalproject.FirebaseImpl.TaskRepositoryFirebase
import com.refael.finalproject.R
import com.refael.finalproject.all_tasks.AllTasksViewModel
import com.refael.finalproject.util.autoCleared
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class AddTaskFragment : Fragment() {
    private var binding : FragmentAddTaskBinding by autoCleared()
    private val viewModel : AllTasksViewModel by viewModels {
        AllTasksViewModel.AllTaskViewModelFactory(
            AuthRepositoryFirebase(),
            TaskRepositoryFirebase(),
            LocalTasksRepository(activity!!.application)
        )
    }

    var name: String = ""
    var phone: String = ""
    var uid: String = ""
    var userImage: String = ""
    var imageRef = ""

    private suspend fun getCurrentName(): String{
        return viewModel.currentUserName()
    }

    private suspend fun getCurrentPhone(): String{
        return viewModel.currentPhone()
    }

    private suspend fun getCurrentImage(): String{
        return viewModel.currentUserImage()
    }

    private fun getCurrentUserID(): String{
        return viewModel.currentUserID()
    }

    private var imageUri: Uri? = null

    private val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            Glide.with(binding.root).load(it).fitCenter().into(binding.missionIv)
            imageUri = it
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = FragmentAddTaskBinding.inflate(inflater, container, false)

        //Select item type.
        val types = resources.getStringArray(R.array.type)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, types)
        binding.typeItems.setAdapter(arrayAdapter)

        // Get selected type text.
        var type: String
        type = binding.typeItems.text.toString()

        binding.typeItems.doOnTextChanged { typ, _, _, _ ->
            // Respond to input text change
            type = typ.toString()
        }

        //If user isn't connected to the internet, upload button will be disabled.
        if (!isOnline(requireContext())) {
            binding.addBtn.isEnabled = false
            binding.addBtn.setBackgroundColor(resources.getColor(R.color.gray))
        }

        //Get current user data
        viewLifecycleOwner.lifecycleScope.launch {
            name = getCurrentName()
            phone = getCurrentPhone()
            uid = getCurrentUserID()
            userImage = getCurrentImage()
        }

        binding.missionIv.setOnClickListener{
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.addBtn.setOnClickListener {
            //Make sure user is connected to the internet.
            if (!isOnline(requireContext())) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.not_connected),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //Validate user input
                if(binding.missionTxt.editText?.text.toString().isEmpty() || type == "" || binding.missionPrice.editText?.text.toString().isEmpty() || binding.missionDesc.editText?.text.toString().isEmpty() || imageUri == null) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.required),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //Loading dialog box
                    val dialog = ProgressDialog(context, R.style.AppCompatAlertDialogStyle).apply {
                        setMessage(getString(R.string.loading))
                        setCancelable(false)
                    }
                    dialog.show()

                    //Uploaded image file name
                    val formatter = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.getDefault())
                    val now = Date()
                    val fileName = formatter.format(now)
                    val storageReference = FirebaseStorage.getInstance().getReference("images/${fileName}")
                    storageReference.putFile(imageUri!!).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                dialog.dismiss()
                                throw it
                            }
                        }
                        return@Continuation storageReference.downloadUrl
                    }).addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                        //Add task to database
                        if (task.isSuccessful) {
                            dialog.dismiss()
                            imageRef = task.result.toString()
                            viewModel.addTask(name, phone, uid, userImage, binding.missionTxt.editText?.text.toString(), binding.missionDesc.editText?.text.toString(),
                                imageRef, binding.missionPrice.editText?.text.toString(), type
                            )
                        }
                    } )
                }
            }
        }

        return binding.root
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

    companion object {

    }
}