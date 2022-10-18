package com.refael.finalproject.ui.register

import android.util.Patterns
import androidx.lifecycle.*
import com.refael.finalproject.model.User
import com.refael.finalproject.repository.AuthRepository
import com.refael.finalproject.util.Resource
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _userRegistrationStatus = MutableLiveData<Resource<User>>()
    val userRegistrationStatus: LiveData<Resource<User>> = _userRegistrationStatus

    fun createUser(userName:String, userEmail:String, userPhone:String, userImage:String, userPass:String) {

        val error = if(userEmail.isEmpty() || userName.isEmpty() || userPass.isEmpty() || userPhone.isEmpty())
            "Empty Strings"
        else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            "Not a valid email"
        }else null
        error?.let {
            _userRegistrationStatus.postValue(Resource.Error(it))
        }
        _userRegistrationStatus.value = Resource.Loading()
        viewModelScope.launch {
            val registrationResult = repository.createUser(
                userName,
                userEmail,
                userPhone,
                userImage,
                userPass
            )
            _userRegistrationStatus.postValue(registrationResult)
        }

    }

    class RegisterViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RegisterViewModel(repo) as T
        }
    }
}