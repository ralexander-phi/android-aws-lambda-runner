package com.alexsci.android.lambdarunner.ui.edit_json

import android.os.Bundle
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

        const val TODO_REMOVE_INIT_JSON = "{\"a\": \"b\", \"c\": {\"f\": [42.0, null], \"g\": {}, \"h\": true}, \"d\": [1,2,3,4], \"e\": 42.9, \"i\": true, \"j\": null}"
    }

    // Which element should we show in the editor
    protected lateinit var jsonRoot: JsonElement
    protected lateinit var jsonViewRoot: JsonElement
    protected lateinit var jsonViewPath: String

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

    protected fun breadCrumbs(): MutableList<BreadCrumbPart> {
        return JqBreadCrumbs().getResults(jsonViewPath)
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

        findViewById<Button>(R.id.add_button).also {
            it.setOnClickListener {
                JsonEditDialog(this).add(
                    contentsArrayAdapter
                )
            }
        }

        pathBreadCrumbs = findViewById(R.id.breadcrumbs)
        pathBreadCrumbs.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        pathBreadCrumbs.adapter = BreadCrumbArrayAdapter(breadCrumbs())

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
}

