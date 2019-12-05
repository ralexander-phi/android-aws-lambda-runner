package com.alexsci.android.lambdarunner.ui.run_lambda

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import arrow.core.Either
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_ACCESS_KEY_ID
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_FUNCTION_NAME
import com.alexsci.android.lambdarunner.SHARED_PREFERENCE_REGION
import com.alexsci.android.lambdarunner.aws.lambda.InvokeFunctionRequest
import com.alexsci.android.lambdarunner.aws.lambda.InvokeFunctionResult
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.ui.common.ToolbarHelper
import com.alexsci.android.lambdarunner.ui.list_functions.ListFunctionsActivity
import com.alexsci.android.lambdarunner.ui.view_results.ViewResultsActivity
import com.alexsci.android.lambdarunner.util.preferences.PreferencesUtil
import com.amazonaws.AmazonClientException

class RunLambdaActivity: AppCompatActivity() {
    companion object {
        const val SAVED_STATE_JSON = "json"
    }

    private lateinit var webView: WebView

    private var lastKnownJson: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_run_lambda)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val preferences = PreferencesUtil(this)
        val accessKeyId = preferences.get(SHARED_PREFERENCE_ACCESS_KEY_ID)
        val region = preferences.get(SHARED_PREFERENCE_REGION)
        val functionName = preferences.get(SHARED_PREFERENCE_FUNCTION_NAME)

        findViewById<Toolbar>(R.id.toolbar)?.title = functionName

        findViewById<Button>(R.id.invoke)?.setOnClickListener {
            webView.evaluateJavascript("editor.get();") { jsonText ->
                val request = InvokeFunctionRequest(functionName, jsonText)
                val client = LambdaClientBuilder(accessKeyId, region).getClient(this@RunLambdaActivity)
                InvokeTask(client, request).execute()
            }
        }

        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(), "Android")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(SAVED_STATE_JSON, lastKnownJson)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        lastKnownJson = savedInstanceState.getString(SAVED_STATE_JSON, "{}")
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (lastKnownJson == null) {
            lastKnownJson = "{}"
        }
        webView.webViewClient = MyWebViewClient()
        webView.loadUrl("file:///android_asset/html/edit_json.html")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toolbarResult = ToolbarHelper().onOptionsItemSelected(this, item)
        return if (toolbarResult != null) {
            toolbarResult
        } else {
            when (item.itemId) {
                android.R.id.home -> {
                    startActivity(Intent(this, ListFunctionsActivity::class.java))
                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onChangeText(json: String) {
            lastKnownJson = json
        }
    }

    inner class MyWebViewClient: WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val js = "editor.set($lastKnownJson);"
            view.evaluateJavascript(js) {}
        }
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
                    Toast.makeText(this@RunLambdaActivity, result?.a.toString(), Toast.LENGTH_LONG).show()

                is Either.Right -> {
                    val intent = Intent(this@RunLambdaActivity, ViewResultsActivity::class.java)
                    intent.putExtra(ViewResultsActivity.RESULT_JSON, result?.b.payload)
                    startActivity(intent)
                }
            }
        }
    }
}
