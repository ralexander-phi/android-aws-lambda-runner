package com.alexsci.android.lambdarunner.ui.edit_json

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.alexsci.android.lambdarunner.R

class EditJsonActivity: AppCompatActivity() {
    private lateinit var webView: WebView

    private var editUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.json_editor)

        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebAppInterface(), "Android")
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val json = "[1,2,3,4,null]"
        webView.webViewClient = MyWebViewClient(json)
        webView.loadUrl("file:///android_asset/html/edit_json.html")

        if (false) {
            if (intent.data != null) {
                editUri = intent.data

                contentResolver.openInputStream(editUri!!)?.reader()?.buffered().use {
                    val json = it?.readText()
                    if (json != null) {
                    }
                }
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onResult(json: String) {
            Log.i("RAA", json)
            if (editUri != null) {
                contentResolver.openOutputStream(editUri!!)?.bufferedWriter().use {
                    it?.write(json)
                }
            }
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}

class MyWebViewClient(
    private val initialValue: String?
) : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String) {
        val initialValueEscaped = initialValue?.replace("\"", "\\\"")
        val js = "init(\"$initialValueEscaped\");"
        view.evaluateJavascript(js) {
            Log.i("RAA", "init returned: $it")
        }
    }
}

