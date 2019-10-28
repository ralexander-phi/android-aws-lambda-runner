package com.alexsci.android.lambdarunner.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.alexsci.android.lambdarunner.data.LoginRepository
import com.alexsci.android.lambdarunner.data.Result

import com.alexsci.android.lambdarunner.R

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isAccessKeyValid(username)) {
            _loginForm.value = LoginFormState(accessKeyIdError = R.string.invalid_access_key_id)
        } else if (!isSecretAccessKeyValid(password)) {
            _loginForm.value = LoginFormState(secretAccessKeyError = R.string.invalid_secret_access_key)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isAccessKeyValid(key : String): Boolean {
        // TODO - this is just a warning
        return key.startsWith("AKIA") || key.startsWith("ASIA")
    }

    private fun isSecretAccessKeyValid(key : String): Boolean {
        return key.length > 5
    }
}
