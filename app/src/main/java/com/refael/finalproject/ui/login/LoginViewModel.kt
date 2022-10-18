package com.refael.finalproject.ui.login

import androidx.lifecycle.*
import com.refael.finalproject.model.User
import com.refael.finalproject.repository.AuthRepository
import com.refael.finalproject.util.Resource
import kotlinx.coroutines.launch

class LoginViewModel(private val authRep: AuthRepository) : ViewModel() {

    private val _userSignInStatus = MutableLiveData<Resource<User>>()
    val userSignInStatus: LiveData<Resource<User>> = _userSignInStatus

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser:LiveData<Resource<User>> = _currentUser

    init {
        viewModelScope.launch {
            _currentUser.postValue(Resource.Loading())
            _currentUser.postValue(authRep.currentUser())
        }
    }

    fun signInUser(userEmail:String, userPass:String) {
        if(userEmail.isEmpty() || userPass.isEmpty())
            _userSignInStatus.postValue(Resource.Error("Empty email or password"))
        else{
            _userSignInStatus.postValue(Resource.Loading())
            viewModelScope.launch {
                val loginResult = authRep.login(userEmail,userPass)
                _userSignInStatus.postValue(loginResult)
            }
        }
    }

    class LoginViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return LoginViewModel(repo) as T
        }
    }
}