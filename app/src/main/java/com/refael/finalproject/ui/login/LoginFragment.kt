package com.refael.finalproject.ui.login

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
import com.refael.finalproject.databinding.FragmentLoginBinding
import com.refael.finalproject.FirebaseImpl.AuthRepositoryFirebase
import com.refael.finalproject.util.Resource

class LoginFragment : Fragment() {

    private var binding : FragmentLoginBinding by autoCleared()
    private val viewModel : LoginViewModel by viewModels {
        LoginViewModel.LoginViewModelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)

        //Go to Register fragment
        binding.noAccountTv.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.buttonLogin.setOnClickListener {
            //Validate user input
            if(binding.editTextLoginEmail.editText?.text.toString().isEmpty() || binding.editTextLoginPass.editText?.text.toString().isEmpty()) {
                Toast.makeText(requireContext(),getString(R.string.empty_login), Toast.LENGTH_SHORT).show()
            } else {
                //Sign in with user input
                viewModel.signInUser(binding.editTextLoginEmail.editText?.text.toString(),
                    binding.editTextLoginPass.editText?.text.toString())
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

        viewModel.userSignInStatus.observe(viewLifecycleOwner) {

            when(it) {
                is Resource.Loading -> {
                    dialog.show()
                    binding.buttonLogin.isEnabled = false
                }
                is Resource.Success -> {
                    dialog.dismiss()
                    Toast.makeText(requireContext(),getString(R.string.login_successful), Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                }
                is Resource.Error -> {
                    dialog.dismiss()
                    binding.buttonLogin.isEnabled = true
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {

            when(it) {
                is Resource.Loading -> {
                    dialog.show()
                    binding.buttonLogin.isEnabled = false
                }
                is Resource.Success -> {
                    dialog.dismiss()
                    findNavController().navigate(R.id.action_loginFragment_to_homeActivity)
                }
                is Resource.Error -> {
                    dialog.dismiss()
                    binding.buttonLogin.isEnabled = true
                }
            }
        }
    }
}