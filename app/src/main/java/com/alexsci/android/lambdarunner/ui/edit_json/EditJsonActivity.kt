package com.alexsci.android.lambdarunner.ui.edit_json

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.util.*

abstract class EditJsonActivity: AppCompatActivity() {
    companion object {
        const val SAVED_STATE_JSON = "json"
        const val SAVED_STATE_EDIT_PATH = "edit_path"

        const val JSON_EXTRA = "json"
        const val EDIT_PATH_EXTRA = "edit_path"

        const val REQUEST_CODE_EDIT = 100

        const val TODO_REMOVE_INIT_JSON = "{\"a\": \"b\", \"c\": {\"f\": [42.0, null], \"g\": {}, \"h\": true}, \"d\": [1,2,3,4], \"e\": 42.9, \"i\": true, \"j\": null}"
    }

    // Which element should we show in the editor
    protected lateinit var jsonRoot: JsonElement
    protected lateinit var jsonViewRoot: JsonElement
    protected lateinit var jsonViewPath: String

    protected lateinit var breadCrumbs: MutableList<BreadCrumbPart>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jsonText: String
        if (savedInstanceState != null) {
            jsonText = savedInstanceState.getString(SAVED_STATE_JSON)!!
            jsonViewPath = savedInstanceState.getString(SAVED_STATE_EDIT_PATH)!!
        } else {
            if (intent.hasExtra(JSON_EXTRA)) {
                jsonText = intent.getStringExtra(JSON_EXTRA)!!
                // default to the root element
                jsonViewPath = intent.getStringExtra(EDIT_PATH_EXTRA) ?: "."
            } else {
                jsonText = TODO_REMOVE_INIT_JSON
                jsonViewPath = "."
            }
        }

        jsonRoot = JsonParser().parse(jsonText)
        jsonViewRoot = JqLookup(jsonRoot).lookup(jsonViewPath)
        breadCrumbs = JqBreadCrumbs().getResults(jsonViewPath)
    }

    override fun onBackPressed() {
        navigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                navigateUp()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun navigateUp() {
        if (breadCrumbs.size == 1) {
            // We are at the top most JSON element, so we have finished editing
            onDoneEditing()
        } else {
            // There is a parent container, get it's path and load it
            val intent = Intent(this, EditJsonObjectActivity::class.java)
            intent.putExtra(JSON_EXTRA, getUpdatedJsonRoot().toString())
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

            if (breadCrumbs.size > 2) {
                // If the parent isn't the root, figure out it's path
                intent.putExtra(EDIT_PATH_EXTRA, breadCrumbs[breadCrumbs.size - 2].path)
            }

            startActivity(intent)
        }
    }

    protected fun onDoneEditing() {
        val intent = Intent()
        intent.putExtra(JSON_EXTRA, getUpdatedJsonRoot().toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    abstract fun getUpdatedJsonRoot(): JsonElement

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
}


class EditJsonObjectActivity: EditJsonActivity() {
    private lateinit var pathBreadCrumbs: RecyclerView
    private lateinit var contents: RecyclerView
    private lateinit var contentsArrayAdapter: JsonPropertyArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        assert(jsonViewRoot.isJsonObject)

        setContentView(R.layout.edit_json_object_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<Button>(R.id.add_button).setOnClickListener {
            JsonEditDialog(this).add(
                contentsArrayAdapter
            )
        }

        findViewById<Button>(R.id.done).setOnClickListener {
            onDoneEditing()
        }

        pathBreadCrumbs = findViewById(R.id.breadcrumbs)
        pathBreadCrumbs.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        pathBreadCrumbs.adapter = BreadCrumbArrayAdapter(breadCrumbs)

        contents = findViewById(R.id.contents)
        contents.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        contentsArrayAdapter = JsonPropertyArrayAdapter(
            jsonViewPath,
            jsonRoot,
            TreeMap<String, JsonElement>().also {
                for (p in jsonViewRoot.asJsonObject.entrySet()) {
                    it[p.key] = p.value
                }
            }
        )
        contents.adapter = contentsArrayAdapter
    }

    override fun getUpdatedJsonRoot(): JsonElement {
        return contentsArrayAdapter.getUpdatedJsonRoot()
    }
}

