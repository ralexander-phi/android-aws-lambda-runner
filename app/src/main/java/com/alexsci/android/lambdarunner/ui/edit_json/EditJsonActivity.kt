package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.aws.lambda.InvokeFunctionRequest
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder

class EditJsonActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_JSON_SCHEMA = "com.alexsci.android.lambdarunner.ui.edit_json.json_schema"
        const val EXTRA_INITIAL_VALUE =
            "com.alexsci.android.lambdarunner.ui.edit_json.initial_value"
        const val EXTRA_LAMBDA_CLIENT_BUILDER =
            "com.alexsci.android.lambdarunner.ui.edit_json.lambda_client_builder"
        const val EXTRA_LAMBDA_FUNCTION_NAME=
            "com.alexsci.android.lambdarunner.ui.edit_json.lambda_function_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val schema = intent.getStringExtra(EXTRA_JSON_SCHEMA)?: "{}"
        val initialValue = intent.getStringExtra(EXTRA_INITIAL_VALUE)
        val lambdaClientBuilder = intent.getSerializableExtra(EXTRA_LAMBDA_CLIENT_BUILDER) as LambdaClientBuilder
        val lambdaFunctionName = intent.getStringExtra(EXTRA_LAMBDA_FUNCTION_NAME)

        setContentView(R.layout.activity_web_view)
        val webView = findViewById<WebView>(R.id.webview)

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(
            WebAppInterface(
                this,
                lambdaClientBuilder,
                lambdaFunctionName!!
            ),
            "Android"
        )
        webView.webViewClient = MyWebViewClient(schema, initialValue)
        webView.loadUrl("file:///android_asset/html/edit_json.html")
    }
}

class MyWebViewClient(
    private val schema: String,
    private val initialValue: String?
) : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String) {
        Log.i("RAA", "onPageFinished()")
        val schemaEscaped = schema.replace("\"", "\\\"")

        val js = if (initialValue != null) {
            val initialValueEscaped = initialValue.replace("\"", "\\\"")
            "init(\"$schemaEscaped\", \"$initialValueEscaped\");"
        } else {
            "init(\"$schemaEscaped\");"
        }

        Log.i("RAA", "Will eval: $js")
        view.evaluateJavascript(js) {
            Log.i("RAA", "init returned: $it")
        }
    }
}


class WebAppInterface(
    private val activity: EditJsonActivity,
    private val clientBuilder: LambdaClientBuilder,
    private val functionName: String
) {
    @JavascriptInterface
    fun onResult(json: String) {
        InvokeFunctionTask(activity, clientBuilder, functionName, json).execute()
    }
}

class InvokeFunctionTask(
    private val context: Context,
    private val clientBuilder: LambdaClientBuilder,
    private val functionName: String,
    private val payload: String
):
    AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        val request = InvokeFunctionRequest(functionName, payload, logType="Tail")
        val result = clientBuilder.getClient(context).invoke(request)
        return result.payload
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result != null) {
            val intent = Intent(context, EditJsonActivity::class.java)
            intent.putExtra(EditJsonActivity.EXTRA_INITIAL_VALUE, result)
            intent.putExtra(EditJsonActivity.EXTRA_LAMBDA_CLIENT_BUILDER, clientBuilder)
            intent.putExtra(EditJsonActivity.EXTRA_LAMBDA_FUNCTION_NAME, functionName)
            context.startActivity(intent)
        }
    }
}
