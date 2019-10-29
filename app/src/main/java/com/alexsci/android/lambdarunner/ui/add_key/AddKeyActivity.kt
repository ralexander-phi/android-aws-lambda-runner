package com.alexsci.android.lambdarunner.ui.add_key

import android.app.Activity
import android.os.AsyncTask
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.aws.iam.IamClient
import com.alexsci.android.lambdarunner.util.crypto.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider

data class SaveKeyTaskParams(val accessKey: String, val secretKey: String)

class SaveKeyTask(val activity: AddKeyActivity) : AsyncTask<SaveKeyTaskParams, Void, String>() {
    override fun doInBackground(vararg params: SaveKeyTaskParams?): String {
        assert(params != null)
        assert(params.size == 1)

        val accessKey = params[0]?.accessKey
        val secretKey = params[0]?.secretKey

        val keyManagement = KeyManagement.getInstance(activity)
        val creds = BasicAWSCredentials(accessKey, secretKey)
        val credProvider = StaticCredentialsProvider(creds)

        val iamClient = IamClient(credProvider)
        val user = iamClient.getUser()

        keyManagement.addKey(accessKey, user.user.userName, secretKey)

        return user.user.userName
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        activity.onSuccessfulSave(result)
    }
}


class AddKeyActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_key)

        val accessKeyId = findViewById<EditText>(R.id.access_key_id)
        val secretAccessKey = findViewById<EditText>(R.id.secret_access_key)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this,
            LoginViewModelFactory()
        )
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@AddKeyActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.accessKeyIdError != null) {
                accessKeyId.error = getString(loginState.accessKeyIdError)
            }
            if (loginState.secretAccessKeyError!= null) {
                secretAccessKey.error = getString(loginState.secretAccessKeyError)
            }
        })

        loginViewModel.loginResult.observe(this@AddKeyActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                //updateUiWithUser(loginResult.success)

                val params = SaveKeyTaskParams(
                    accessKeyId.text.toString(),
                    secretAccessKey.text.toString()
                )

                SaveKeyTask(this).execute(params)
            }
        })

        accessKeyId.afterTextChanged {
            loginViewModel.loginDataChanged(
                accessKeyId.text.toString(),
                secretAccessKey.text.toString()
            )
        }

        secretAccessKey.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    accessKeyId.text.toString(),
                    secretAccessKey.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            accessKeyId.text.toString(),
                            secretAccessKey.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(accessKeyId.text.toString(), secretAccessKey.text.toString())
            }
        }
    }

    fun onSuccessfulSave(username: String) {
        Toast.makeText(applicationContext, "Successfully added $username", Toast.LENGTH_LONG).show()

        setResult(Activity.RESULT_OK)

        //Complete and destroy login activity once successful
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
