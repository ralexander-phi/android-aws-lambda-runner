package com.alexsci.android.lambdarunner.ui.run_lambda

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.alexsci.android.lambdarunner.R
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader

class RunLambdaActivity: AppCompatActivity() {
    companion object {
        const val EDIT_JSON_REQUEST = 10
    }

    private lateinit var credential: String
    private lateinit var functionName: String

    private lateinit var inputEditText: EditText

    private lateinit var jsonFile: File
    private lateinit var jsonUri: Uri

    private var prettyPrintJson = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jsonFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "buffer.json")
        jsonUri = FileProvider.getUriForFile(
            RunLambdaActivity@this,
            applicationContext.packageName + ".provider",
            jsonFile)

        setContentView(R.layout.activity_run_lambda)

        val outputEditText = findViewById<EditText>(R.id.output)
        inputEditText = findViewById(R.id.input)!!

        credential = "MyCred"
        functionName = "MyFunction"

        findViewById<TextView>(R.id.credential)!!.also {
            it.text = credential
        }

        findViewById<TextView>(R.id.function_name)!!.also {
            it.text = functionName
        }

        findViewById<ImageButton>(R.id.edit_input)!!.also {
            it.setOnClickListener {
                editJson()
            }
        }

        findViewById<Button>(R.id.input_paste_button)!!.also {
            it.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val primaryClip = clipboard.primaryClip
                if (primaryClip != null && primaryClip.itemCount > 0) {
                    val text = primaryClip.getItemAt(0).text
                    if (text != null && text.isNotBlank()) {
                        inputEditText.setText(text)
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.scan_qr)!!.also {
            it.setOnClickListener {
                // TODO
            }
        }

        findViewById<ImageButton>(R.id.input_pretty_toggle)!!.also {
            it.setOnClickListener {
                prettyPrintJson = !prettyPrintJson
                setInputText(inputEditText.text.toString())
            }
        }

        findViewById<Button>(R.id.invoke_button)!!.also {
            it.setOnClickListener {
                // TODO
            }
        }

        findViewById<Button>(R.id.copy_button)!!.also {
            it.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("json", outputEditText.text.toString())
                clipboard.setPrimaryClip(clip)
            }
        }
    }

    private fun editJson() {
        val json = inputEditText.text.toString()
        writeJsonBuffer(json)

        val intent = Intent(Intent.ACTION_EDIT)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, jsonFile.absolutePath.toString())
        intent.type = "application/json"
        startActivityForResult(intent, EDIT_JSON_REQUEST)
    }

    private fun writeJsonBuffer(json: String) {
        // Ensure it's empty
        jsonFile.delete()
        jsonFile.createNewFile()
        FileOutputStream(jsonFile).bufferedWriter().use {
            it.write(json)
        }
    }

    private fun setInputText(json: String?) {
        if (json != null && json.isNotBlank()) {
            val builder = GsonBuilder()
            if (prettyPrintJson) {
                builder.setPrettyPrinting()
            }

            val root = JsonParser().parse(json)

            inputEditText.setText(builder.create().toJson(root))
        } else {
            inputEditText.setText("{}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_JSON_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                FileReader(jsonFile).buffered().use {
                    setInputText(it.readText())
                }
            }
        }
    }
}
