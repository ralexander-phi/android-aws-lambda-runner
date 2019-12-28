package com.alexsci.android.lambdarunner.ui.add_key

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import arrow.core.Either
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.RequestCodes
import com.alexsci.android.lambdarunner.aws.iam.IamClient
import com.alexsci.android.lambdarunner.ui.common.ToolbarHelper
import com.alexsci.android.lambdarunner.ui.scan_qr.ScanQRActivity
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement
import com.alexsci.android.lambdarunner.util.preferences.PreferencesUtil
import com.amazonaws.AmazonClientException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider

data class SaveKeyTaskParams(val accessKey: String, val secretKey: String)

class AddKeyActivity: AppCompatActivity() {

    companion object {
        const val LOG_TAG = "AddKeyActivity"
    }

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var accessKeyIdEditText: EditText
    private lateinit var secretAccessKeyEditText: EditText

    private lateinit var preferencesUtil: PreferencesUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_key)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accessKeyIdEditText = findViewById(R.id.access_key_id)
        secretAccessKeyEditText = findViewById(R.id.secret_access_key)

        val loginButton = findViewById<Button>(R.id.login)
        val scanQrButton = findViewById<Button>(R.id.scan_qr)

        preferencesUtil = PreferencesUtil(this)

        loginViewModel = ViewModelProviders.of(this,
            LoginViewModelFactory()
        )
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@AddKeyActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            loginButton.isEnabled = loginState.isDataValid

            if (loginState.accessKeyIdError != null) {
                accessKeyIdEditText.error = getString(loginState.accessKeyIdError)
            }
            if (loginState.secretAccessKeyError!= null) {
                secretAccessKeyEditText.error = getString(loginState.secretAccessKeyError)
            }
        })

        loginViewModel.loginResult.observe(this@AddKeyActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                //updateUiWithUser(loginResult.success)

                val params = SaveKeyTaskParams(
                    accessKeyIdEditText.text.toString(),
                    secretAccessKeyEditText.text.toString()
                )

                SaveKeyTask().execute(params)
            }
        })

        accessKeyIdEditText.afterTextChanged {
            loginViewModel.loginDataChanged(
                accessKeyIdEditText.text.toString(),
                secretAccessKeyEditText.text.toString()
            )
        }

        secretAccessKeyEditText.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    accessKeyIdEditText.text.toString(),
                    secretAccessKeyEditText.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            accessKeyIdEditText.text.toString(),
                            secretAccessKeyEditText.text.toString()
                        )
                }
                false
            }

            loginButton.setOnClickListener {
                loginViewModel.login(
                    accessKeyIdEditText.text.toString(),
                    secretAccessKeyEditText.text.toString()
                )
            }

            scanQrButton.setOnClickListener {
                val intent = Intent(this@AddKeyActivity, ScanQRActivity::class.java)
                intent.putExtra(
                    ScanQRActivity.SCAN_REQUIREMENTS_EXTRA,
                    ScanQRActivity.ScanRequirements.IS_AWS_CREDS.name
                )
                startActivityForResult(intent, RequestCodes.REQUEST_CODE_SCAN_QR.code)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toolbarResult = ToolbarHelper().onOptionsItemSelected(this, item)
        return if (toolbarResult != null) {
            toolbarResult
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCodes.REQUEST_CODE_SCAN_QR.code &&
            resultCode == Activity.RESULT_OK &&
            data != null) {
            val ak = data.getStringExtra(ScanQRActivity.DETECTED_ACCESS_KEY_ID)
            val sec = data.getStringExtra(ScanQRActivity.DETECTED_SECRET_ACCESS_KEY)

            assert(ak != null)
            assert(sec != null)

            runOnUiThread {
                accessKeyIdEditText.setText(ak)
                secretAccessKeyEditText.setText(sec)
            }
        }
    }
    inner class SaveKeyTask: AsyncTask<SaveKeyTaskParams, Void, Either<AmazonClientException, String>>() {
        override fun doInBackground(vararg params: SaveKeyTaskParams?): Either<AmazonClientException, String> {
            assert(params.size == 1)

            val accessKey = params[0]!!.accessKey
            val secretKey = params[0]!!.secretKey

            val keyManagement = KeyManagement.getInstance(this@AddKeyActivity)
            val creds = BasicAWSCredentials(accessKey, secretKey)
            val credProvider = StaticCredentialsProvider(creds)

            val iamClient = IamClient(credProvider)

            return when (val getUserResult = iamClient.getUser()) {
                is Either.Left -> {
                    Either.left(getUserResult.a)
                }
                is Either.Right -> {
                    val username = getUserResult.b.getUserResult.user.userName
                    keyManagement.addKey(username, accessKey, secretKey)
                    Either.right(username)
                }
            }
        }

        override fun onPostExecute(result: Either<AmazonClientException, String>) {
            super.onPostExecute(result)

            when (result) {
                is Either.Left -> Toast.makeText(
                    this@AddKeyActivity,
                    result.a.toString(),
                    Toast.LENGTH_LONG
                ).show()

                is Either.Right -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
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
