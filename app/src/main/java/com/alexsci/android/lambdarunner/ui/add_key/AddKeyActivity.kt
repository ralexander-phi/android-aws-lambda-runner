package com.alexsci.android.lambdarunner.ui.add_key

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.FileProvider
import arrow.core.Either
import com.alexsci.android.lambdarunner.BuildConfig

import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_SHOW_QR_CODE_HELP
import com.alexsci.android.lambdarunner.aws.iam.IamClient
import com.alexsci.android.lambdarunner.util.crypto.*
import com.alexsci.android.lambdarunner.util.preferences.PreferencesUtil
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.File
import java.io.FileNotFoundException

data class SaveKeyTaskParams(val accessKey: String, val secretKey: String)


class AddKeyActivity : AppCompatActivity() {

    companion object {
        const val PHOTO_REQUEST = 10
        const val LOG_TAG = "AddKeyActivity"
    }

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var imageUri: Uri
    private lateinit var detector: BarcodeDetector
    private lateinit var accessKeyIdEditText: EditText
    private lateinit var secretAccessKeyEditText: EditText

    private lateinit var preferencesUtil: PreferencesUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detector = BarcodeDetector.Builder(applicationContext)
            .setBarcodeFormats(Barcode.DATA_MATRIX or Barcode.QR_CODE)
            .build()

        setContentView(R.layout.activity_add_key)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        accessKeyIdEditText = findViewById(R.id.access_key_id)
        secretAccessKeyEditText = findViewById(R.id.secret_access_key)

        val loginButton = findViewById<Button>(R.id.login)
        val scanQrButton = findViewById<Button>(R.id.scan_qr)
        val loading = findViewById<ProgressBar>(R.id.loading)

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

            loading.visibility = View.GONE
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
                loading.visibility = View.VISIBLE
                loginViewModel.login(
                    accessKeyIdEditText.text.toString(),
                    secretAccessKeyEditText.text.toString()
                )
            }

            if (canTakePicture()) {
                scanQrButton.setOnClickListener {
                    val showHint = preferencesUtil.getBoolean(SHARED_PREFERENCE_SHOW_QR_CODE_HELP, true)

                    if (showHint) {
                        showQRCodeHint()
                    } else {
                        takePicture()
                    }
                }
            } else {
                scanQrButton.visibility = View.GONE
            }
        }
    }

    private fun showQRCodeHint() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.qr_code_help_view, null)
        builder.setView(view)
        builder.setMessage("Load credentials via QR code")
        builder.setPositiveButton("Continue") { dialog, _ ->
            val showHelp = view.findViewById<Switch>(R.id.show_help).isChecked
            if (!showHelp) {
                preferencesUtil.setBoolean(SHARED_PREFERENCE_SHOW_QR_CODE_HELP, false)
            }
            dialog.dismiss()
            takePicture()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            val showHelp = view.findViewById<Switch>(R.id.show_help).isChecked
            if (!showHelp) {
                preferencesUtil.setBoolean(SHARED_PREFERENCE_SHOW_QR_CODE_HELP, false)
            }
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun launchMediaScanIntent() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = imageUri
        this.sendBroadcast(mediaScanIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PHOTO_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                launchMediaScanIntent()
                try {
                    val bitmap = decodeBitmapUri(this, imageUri)
                    if (detector.isOperational && bitmap != null) {
                        val frame = Frame.Builder().setBitmap(bitmap!!).build()
                        val barcodes = detector.detect(frame)
                        for (index in 0 until barcodes.size()) {
                            val code = barcodes.valueAt(index)
                            Log.i(LOG_TAG, code.rawValue)
                            val parts = code.rawValue.split("\n")
                            accessKeyIdEditText.setText(parts[0])
                            secretAccessKeyEditText.setText(parts[1])
                        }
                        if (barcodes.size() == 0) {
                            Toast.makeText(
                                this,
                                "Scan Failed: Found nothing to scan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(this, "Could not set up the detector!", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT).show()
                    Log.e(LOG_TAG, e.toString())
                }
            } finally {
                // Cleanup the old photo (remove the creds)
                picturePath().delete()
            }
        }
    }

    @Throws(FileNotFoundException::class)
    private fun decodeBitmapUri(ctx: Context, uri: Uri): Bitmap? {
        val targetW = 600
        val targetH = 600
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeStream(ctx.contentResolver.openInputStream(uri), null, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        return BitmapFactory.decodeStream(
            ctx.contentResolver
                .openInputStream(uri), null, bmOptions
        )
    }

    private fun canTakePicture(): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
                intent.resolveActivity(packageManager) != null
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val picPath = picturePath()

        imageUri = FileProvider.getUriForFile(
            AddKeyActivity@this,
            applicationContext.packageName + ".provider",
            picPath
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, PHOTO_REQUEST)
    }

    private fun picturePath(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("qr-code", ".jpg", storageDir).absoluteFile
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
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
                    keyManagement.addKey(accessKey, username, secretKey)
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
