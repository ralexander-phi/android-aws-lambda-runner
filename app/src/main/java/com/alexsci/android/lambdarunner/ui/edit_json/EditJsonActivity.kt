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

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

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

        changeView(jsonText, jsonViewPath)
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
            if (breadCrumbs.size > 2) {
                // If the parent isn't the root, figure out it's path
                changeView(getUpdatedJsonRoot().toString(), breadCrumbs[breadCrumbs.size - 2].path)
            } else {
                // Go to the root
                changeView(getUpdatedJsonRoot().toString(), "")
            }
        }
    }

    protected fun onDoneEditing() {
        val intent = Intent()
        intent.putExtra(JSON_EXTRA, getUpdatedJsonRoot().toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    protected open fun changeView(jsonText: String, path: String) {
        jsonViewPath = path
        jsonRoot = JsonParser().parse(jsonText)
        jsonViewRoot = JqLookup(jsonRoot).lookup(jsonViewPath)
        breadCrumbs = JqBreadCrumbs().getResults(jsonViewPath)
    }

    abstract fun getUpdatedJsonRoot(): JsonElement
}


class EditJsonObjectActivity: EditJsonActivity() {
    private lateinit var pathBreadCrumbs: RecyclerView
    private lateinit var contents: RecyclerView
    private lateinit var contentsArrayAdapter: JsonPropertyArrayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        contents = findViewById(R.id.contents)
        contents.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    fun updateView(path: String) {
        changeView(getUpdatedJsonRoot().toString(), path)
    }

    override fun changeView(jsonText: String, path: String) {
        super.changeView(jsonText, path)

        assert(jsonViewRoot.isJsonObject)

        pathBreadCrumbs.adapter = BreadCrumbArrayAdapter(breadCrumbs)

        contentsArrayAdapter = JsonPropertyArrayAdapter(
            jsonViewPath,
            jsonRoot,
            TreeMap<String, JsonElement>().also {
                for (p in jsonViewRoot.asJsonObject.entrySet()) {
                    it[p.key] = p.value
                }
            },
            this
        )
        contents.adapter = contentsArrayAdapter
    }

    override fun getUpdatedJsonRoot(): JsonElement {
        return contentsArrayAdapter.getUpdatedJsonRoot()
    }
}

