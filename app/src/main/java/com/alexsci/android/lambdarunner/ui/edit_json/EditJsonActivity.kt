package com.alexsci.android.lambdarunner.ui.edit_json

import android.app.Activity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.alexsci.android.lambdarunner.R

class EditJsonActivity: AppCompatActivity() {
    companion object {
        const val SAVED_STATE_JSON = "json"
    }

    private lateinit var webView: WebView

    private var lastKnownJson: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.json_editor)

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
            lastKnownJson = readJsonFromIntent()
        }
        webView.webViewClient = MyWebViewClient()
        webView.loadUrl("file:///android_asset/html/edit_json.html")
    }

    private fun readJsonFromIntent(): String {
        if (intent.data != null) {
            contentResolver.openInputStream(intent.data!!)?.reader()?.buffered().use {
                val text = it?.readText()
                if (text != null) {
                    return text
                }
            }
        }

        // Default to an empty object
        return "{}"
    }

    private fun writeJsonToIntentData(json: String) {
        if (intent.data != null) {
            contentResolver.openOutputStream(intent.data!!)?.bufferedWriter().use {
                it?.write(json)
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onResult(json: String) {
            writeJsonToIntentData(json)
            setResult(Activity.RESULT_OK)
            finish()
        }

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

}

