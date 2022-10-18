package com.refael.finalproject.ui.register

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.refael.finalproject.R
import com.refael.finalproject.util.autoCleared
import com.refael.finalproject.databinding.FragmentRegisterBinding
import com.refael.finalproject.FirebaseImpl.AuthRepositoryFirebase
import com.refael.finalproject.util.Resource

class RegisterFragment : Fragment(){

    private var binding : FragmentRegisterBinding by autoCleared()
    private val viewModel : RegisterViewModel by viewModels() {
        RegisterViewModel.RegisterViewModelFactory(AuthRepositoryFirebase())
    }

    //Set default profile image
    private var imageDefault = "https://firebasestorage.googleapis.com/v0/b/finalproject-343c6.appspot.com/o/default%2Fprofile.png?alt=media&token=24665cc2-f5c8-4bf0-bfbe-aa9db65abb71"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(inflater,container,false)

        //Go back to login screen
        binding.clickToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.userRegisterButton.setOnClickListener {
            if (binding.edxtUserName.editText?.text.toString().isEmpty() || binding.edxtEmailAddress.editText?.text.toString().isEmpty() ||
                binding.edxtPhoneNum.editText?.text.toString().isEmpty() || binding.edxtPassword.editText?.text.toString().isEmpty()) {
                Toast.makeText(requireContext(),getString(R.string.register_empty), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.createUser(binding.edxtUserName.editText?.text.toString(),
                    binding.edxtEmailAddress.editText?.text.toString(),
                    binding.edxtPhoneNum.editText?.text.toString(),
                    imageDefault,
                    binding.edxtPassword.editText?.text.toString())
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Loading dialog box
        val dialog = ProgressDialog(context, R.style.AppCompatAlertDialogStyle).apply {
            setMessage(getString(R.string.loading))
            setCancelable(false)
        }

        viewModel.userRegistrationStatus.observe(viewLifecycleOwner) {

            when(it) {
                is Resource.Loading -> {
                    dialog.show()
                    binding.userRegisterButton.isEnabled = false
                }
                is Resource.Success -> {
                    dialog.dismiss()
                    Toast.makeText(requireContext(),getString(R.string.registration_successful),Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_profileImageFragment)
                }
                is Resource.Error -> {
                    dialog.dismiss()
                    binding.userRegisterButton.isEnabled = true
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}