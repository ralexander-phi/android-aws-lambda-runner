package com.alexsci.android.lambdarunner.ui.login

import android.app.Activity
import android.content.Context
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.aws.BetterAWSLambdaClient
import com.alexsci.android.lambdarunner.aws.ListFunctionsRequest
import com.alexsci.android.lambdarunner.util.crypto.*
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import java.lang.RuntimeException

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel

    class HackThread(val context: Context, val keyName: String) : Thread() {
        var functions: String? = null

        override fun run() {
            val thread = GetKeysThread(context, keyName)
            thread.run()
            if (thread.keySecret == null) {
                throw RuntimeException("Ooops")
            }

            val creds = BasicAWSCredentials(keyName, thread.keySecret)

            val client = BetterAWSLambdaClient(creds)
            client.setRegion(Region.getRegion("us-east-1"))

            val res = client.listFunctions(ListFunctionsRequest(""))
            functions = res.functions.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val listKeyTh = ListKeysThread(this)
        // uhh...
        listKeyTh.start()
        listKeyTh.join()

        val existingKeys = listKeyTh.keys

        if (! existingKeys.isNullOrEmpty()) {
            val doTh = HackThread(this, existingKeys.first())
            doTh.start()
            doTh.join()
            Log.i("RAA = DONE", doTh.functions)
        }

        setContentView(R.layout.activity_login)

        val accessKeyId = findViewById<EditText>(R.id.access_key_id)
        val secretAccessKey = findViewById<EditText>(R.id.secret_access_key)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
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

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                //updateUiWithUser(loginResult.success)
                val addTh = AddKeyThread(this, accessKeyId.text.toString(), "Something", secretAccessKey.text.toString())
                addTh.start()
                addTh.join()

                val doTh = HackThread(this, accessKeyId.text.toString())
                doTh.start()
                doTh.join()
                Log.i("RAA = DONE", doTh.functions)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
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

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
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
