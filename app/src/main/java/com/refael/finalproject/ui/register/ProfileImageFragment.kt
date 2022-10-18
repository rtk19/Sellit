package com.refael.finalproject.ui.register

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.refael.finalproject.R
import com.refael.finalproject.util.autoCleared
import com.refael.finalproject.databinding.FragmentProfileImageBinding
import java.text.SimpleDateFormat
import java.util.*

class ProfileImageFragment : Fragment() {
    private var binding : FragmentProfileImageBinding by autoCleared()
    private var imageUri: Uri? = null
    var imageRef = ""

    private val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.registerProfileImage.setImageURI(it)
            if (it != null) {
                requireActivity().contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            imageUri = it
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileImageBinding.inflate(layoutInflater, container,false)

        Toast.makeText(requireContext(),getString(R.string.add_profile_photo), Toast.LENGTH_SHORT).show()

        binding.registerProfileImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.uploadImageBtn.setOnClickListener {

            if(imageUri != null){
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
                }).addOnCompleteListener ( OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        dialog.dismiss()
                        //File reference in the database
                        imageRef = task.result.toString()
                        val currentUser = Firebase.auth.currentUser!!.uid
                        val db = FirebaseFirestore.getInstance().collection("users").document(currentUser)
                        db.update("image", imageRef)
                        findNavController().navigate(R.id.action_profileImageFragment_to_homeActivity)
                    }
                } )

            } else {
                Toast.makeText(context, getString(R.string.choose_a_photo), Toast.LENGTH_SHORT)
            }

        }

        //Skip profile image choosing
        binding.uploadSkipBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileImageFragment_to_homeActivity)
        }

        return binding.root

    }
    fun callParentMethod() {
        activity!!.onBackPressed()
    }
}