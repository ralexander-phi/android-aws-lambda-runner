package com.alexsci.android.lambdarunner.ui.view_results

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alexsci.android.lambdarunner.R
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

class ViewResultsActivity: AppCompatActivity() {
    companion object {
        const val RESULT_JSON = "JSON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_view_results)

        val uglyJson = intent.getStringExtra(RESULT_JSON)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val root = JsonParser().parse(uglyJson)
        val prettyJson = gson.toJson(root)

        findViewById<TextView>(R.id.results).text = prettyJson

        findViewById<Button>(R.id.copy).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("json", prettyJson)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }
    }
}
