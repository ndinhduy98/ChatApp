package com.freezer.chatapp.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.freezer.chatapp.data.LoginRepository

import com.freezer.chatapp.ui.login.LoginFormState
import com.freezer.chatapp.ui.login.LoginResult

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

//        if (result is Result.Success) {
//            _loginResult.value =
//                LoginResult(success = LoggedInUserView())
//        } else {
//            _loginResult.value = LoginResult(error = R.string.login_failed)
//        }
    }

    // A placeholder phone number validation check
    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return Patterns.PHONE.matcher(phoneNumber).matches()
    }
}