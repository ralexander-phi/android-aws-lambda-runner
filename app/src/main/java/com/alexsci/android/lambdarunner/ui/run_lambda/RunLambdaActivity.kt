package com.alexsci.android.lambdarunner.ui.run_lambda

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import arrow.core.Either
import com.alexsci.android.lambdarunner.*
import com.alexsci.android.lambdarunner.aws.lambda.InvokeFunctionRequest
import com.alexsci.android.lambdarunner.aws.lambda.InvokeFunctionResult
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.ui.common.ToolbarHelper
import com.alexsci.android.lambdarunner.ui.edit_json.EditJsonActivity
import com.alexsci.android.lambdarunner.ui.edit_json.EditJsonObjectActivity
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.ui.list_keys.ListKeysActivity
import com.alexsci.android.lambdarunner.ui.view_results.ViewResultsActivity
import com.alexsci.android.lambdarunner.util.preferences.PreferencesUtil
import com.amazonaws.AmazonClientException
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

class RunLambdaActivity: AppCompatActivity() {
    companion object {
        const val SAVED_STATE_JSON = "json"
    }

    private lateinit var preferencesUtil: PreferencesUtil
    private lateinit var jsonEditText: EditText
    private lateinit var editButton: Button
    private lateinit var errorMessage: TextView

    private var lastKnownJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_run_lambda)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        preferencesUtil = PreferencesUtil(this)
        val accessKeyId = preferencesUtil.get(SHARED_PREFERENCE_ACCESS_KEY_ID)
        val region = preferencesUtil.get(SHARED_PREFERENCE_REGION)
        val functionName = preferencesUtil.get(SHARED_PREFERENCE_FUNCTION_NAME)

        errorMessage = findViewById<TextView>(R.id.error_message).also {
            // Start hidden
            it.visibility = View.GONE
        }

        jsonEditText = findViewById<EditText>(R.id.json_edittext).also {
            it.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s != null) {
                        try {
                            onJsonValidationResult(Either.right(JsonParser().parse(s.toString())))
                        } catch (e: JsonSyntaxException) {
                            onJsonValidationResult(Either.left(e))
                        }
                    }
                }
            })
        }

        if (accessKeyId == null) {
            startActivity(Intent(this, ListKeysActivity::class.java))
        } else if (region == null || functionName == null) {
            startActivity(Intent(this, ListFunctionsActivity::class.java))
        } else {
            findViewById<Toolbar>(R.id.toolbar)?.title = functionName
            editButton = findViewById<Button>(R.id.edit_json).also {
                it.setOnClickListener {
                    val intent = Intent(this, EditJsonObjectActivity::class.java)
                    intent.putExtra(EditJsonActivity.JSON_EXTRA, jsonEditText.text.toString())
                    startActivityForResult(intent, EditJsonActivity.REQUEST_CODE_EDIT)
                }
            }

            findViewById<Button>(R.id.invoke)?.setOnClickListener {
                val jsonText = jsonEditText.text.toString()
                preferencesUtil.set(SHARED_PREFERENCE_LAST_USED_JSON, jsonText)
                val request = InvokeFunctionRequest(functionName, jsonText)
                val client = LambdaClientBuilder(accessKeyId, region).getClient(this@RunLambdaActivity)
                InvokeTask(client, request).execute()
            }
        }
    }

    private fun onJsonValidationResult(e: Either<JsonSyntaxException, JsonElement>) {
        when (e) {
            is Either.Left -> {
                // JSON is not valid
                editButton.isEnabled = false
                errorMessage.text = e.a.toString()
                errorMessage.visibility = View.VISIBLE
            }

            is Either.Right -> {
                if (e.b.isJsonObject || e.b.isJsonArray) {
                    editButton.isEnabled = true
                    errorMessage.visibility = View.GONE
                } else {
                    editButton.isEnabled = false
                    errorMessage.text = "Edit disabled, only able to edit arrays and objects"
                    errorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EditJsonActivity.REQUEST_CODE_EDIT && resultCode == Activity.RESULT_OK) {
            // TODO formatting
            if (data != null && data.hasExtra(EditJsonActivity.JSON_EXTRA)) {
                val jsonText = data.getStringExtra(EditJsonActivity.JSON_EXTRA)
                setJsonText(jsonText)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SAVED_STATE_JSON, lastKnownJson)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        lastKnownJson = savedInstanceState.getString(SAVED_STATE_JSON)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (lastKnownJson == null) {
            lastKnownJson = preferencesUtil.get(SHARED_PREFERENCE_LAST_USED_JSON, "{}")
        }

        setJsonText(lastKnownJson!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toolbarResult = ToolbarHelper().onOptionsItemSelected(this, item)
        if (toolbarResult != null) {
            return toolbarResult
        } else {
            when (item.itemId) {
                android.R.id.home -> {
                    startActivity(Intent(this, ListFunctionsActivity::class.java))
                    return true
                }

                else -> return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun setJsonText(uglyJson: String) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val root = JsonParser().parse(uglyJson)
        val prettyJson = gson.toJson(root)
        jsonEditText.setText(prettyJson)
    }

    private inner class InvokeTask(
        val client: LambdaClient,
        val request: InvokeFunctionRequest
    ): AsyncTask<Void, Void, Either<AmazonClientException, InvokeFunctionResult>>() {
        override fun doInBackground(vararg params: Void?): Either<AmazonClientException, InvokeFunctionResult> {
            return client.invoke(request)
        }

        override fun onPostExecute(result: Either<AmazonClientException, InvokeFunctionResult>?) {
            super.onPostExecute(result)

            when (result) {
                is Either.Left ->
                    Toast.makeText(this@RunLambdaActivity, result.a.toString(), Toast.LENGTH_LONG).show()

                is Either.Right -> {
                    val intent = Intent(this@RunLambdaActivity, ViewResultsActivity::class.java)
                    intent.putExtra(ViewResultsActivity.RESULT_JSON, result.b.payload)
                    startActivity(intent)
                }
            }
        }
    }
}
