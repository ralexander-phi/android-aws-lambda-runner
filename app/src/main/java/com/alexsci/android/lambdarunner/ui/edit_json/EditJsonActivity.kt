package com.alexsci.android.lambdarunner.ui.edit_json

import android.app.Activity
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.*
import java.util.*

class EditJsonActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_JSON_SCHEMA = "json_schema"
        const val EXTRA_LAMBDA_CLIENT_BUILDER = "lambda_client_builder"
        const val EXTRA_LAMBDA_FUNCTION_NAME = "lambda_function_name"

        const val BUNDLE_SAVED_JSON = "json"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var viewAdapter: JsonAdapter

    private var editUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.json_editor)

        viewManager = LinearLayoutManager(this)
        viewAdapter = JsonAdapter()

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        findViewById<Button>(R.id.done_button).also { button ->
            button.setOnClickListener {
                val json = getJson()
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

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState != null) {
            val json = savedInstanceState.getString(BUNDLE_SAVED_JSON)
            if (json != null) {
                inflateJsonViewFromText(json)
            }
        }
    }

    private fun inflateJsonViewFromText(json: String) {
        val parser = JsonParser()
        inflateJsonViewFromTree(parser.parse(json))
        viewAdapter.notifyDataSetChanged()
    }

    private fun inflateJsonViewFromTree(root: JsonElement, depth: Int = 0) {
        if (root.isJsonObject) {
            val obj = root.asJsonObject
            viewAdapter.data.add(JsonObject(depth))
            for (entry in obj.entrySet()) {
                viewAdapter.data.add(JsonPropertyKey(entry.key, depth+1))
                inflateJsonViewFromTree(entry.value, depth+1)
            }
            viewAdapter.data.add(JsonEndObject(depth))
        } else if (root.isJsonArray) {
            val arr = root.asJsonArray
            viewAdapter.data.add(JsonArray(depth))
            for (item in arr.iterator()) {
                inflateJsonViewFromTree(item, depth+1)
            }
            viewAdapter.data.add(JsonEndArray(depth))
        } else if (root.isJsonPrimitive) {
            val primitive = root.asJsonPrimitive
            if (primitive.isBoolean) {
                viewAdapter.data.add(JsonBoolean(primitive.asBoolean, depth))
            } else if (primitive.isNumber) {
                viewAdapter.data.add(JsonNumber(primitive.asNumber.toDouble(), depth))
            } else if (primitive.isString) {
                viewAdapter.data.add(JsonString(primitive.asString, depth))
            } else {
                throw RuntimeException("Unexpected")
            }
        } else if (root.isJsonNull) {
            viewAdapter.data.add(JsonNull(depth))
        } else {
            throw RuntimeException("Unexpected")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (viewAdapter.data.isEmpty()) {
            if (intent.data != null) {
                editUri = intent.data

                contentResolver.openInputStream(editUri!!)?.reader()?.buffered().use {
                    val json = it?.readText()
                    if (json != null) {
                        inflateJsonViewFromText(json)
                    }
                }
            } else {
                selectRootJsonType()
            }
        }
    }

    private fun selectRootJsonType() {
        selectJsonTypeDialog(this, false).also {
            it.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "OK"
            ) { dialog, _ ->
                val alertDialog = dialog as AlertDialog
                val listView = alertDialog.listView

                when (listView.adapter.getItem(listView.checkedItemPosition) as String) {
                    JsonTypes.Object.name -> {
                        viewAdapter.data.add(JsonObject())
                        viewAdapter.data.add(JsonEndObject())
                    }
                    JsonTypes.Array.name -> {
                        viewAdapter.data.add(JsonArray())
                        viewAdapter.data.add(JsonEndArray())
                    }
                    JsonTypes.String.name -> {
                        val stringEdit = it.findViewById<EditText>(R.id.string_value)
                        viewAdapter.data.add(JsonString(stringEdit!!.text.toString()))
                    }
                    JsonTypes.Number.name -> {
                        val numberEdit = it.findViewById<EditText>(R.id.number_value)
                        viewAdapter.data.add(JsonNumber(numberEdit!!.text.toString().toDouble()))
                    }
                    JsonTypes.Boolean.name -> {
                        val booleanEdit = it.findViewById<ToggleButton>(R.id.boolean_value)
                        viewAdapter.data.add(JsonBoolean(booleanEdit!!.isChecked))
                    }
                    JsonTypes.Null.name ->
                        viewAdapter.data.add(JsonNull())
                }
                viewAdapter.notifyDataSetChanged()

                dialog.dismiss()
            }
        }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(BUNDLE_SAVED_JSON, getJson())
    }

    private fun getJson(): String {
        val stack = Stack<JsonElement>()
        val propertyStack = Stack<String>()
        val iter = viewAdapter.data.iterator()

        while (iter.hasNext()) {
            val item = iter.next()

            when (item.type) {
                // Push containers onto the stacks
                JsonTypes.Object, JsonTypes.Array -> stack.push(item.asJsonElement())

                // Properties are added when the value is fully constructed
                JsonTypes.PropertyKey -> propertyStack.push((item as JsonPropertyKey).key)

                // When ending a container, pop it, and add it to the parent container
                JsonTypes.EndObject, JsonTypes.EndArray -> {
                    val container = stack.pop()

                    if (stack.empty()) {
                        return container.toString()
                    }

                    val parent = stack.peek()

                    if (parent.isJsonArray) {
                        parent.asJsonArray.add(container)
                    } else if (parent.isJsonObject) {
                        parent.asJsonObject.add(propertyStack.pop(), container)
                    }
                }

                // primitives
                else -> {
                    val element = item.asJsonElement()

                    if (stack.empty()) {
                        return element.toString()
                    }

                    val parent = stack.peek()

                    if (parent.isJsonArray) {
                        parent.asJsonArray.add(element)
                    } else if (parent.isJsonObject) {
                        parent.asJsonObject.add(propertyStack.pop(), element)
                    }
                }
            }
        }

        throw RuntimeException("Expected a bare element, or the closure of a top level object/array to return")
    }
}

internal class JsonAdapter: RecyclerView.Adapter<JsonViewHolder>() {
    val data: MutableList<JsonType> = LinkedList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsonViewHolder {
        val context = parent.context

        val view = when (viewType) {
            JsonTypes.Object.ordinal -> createObjectView(context)
            JsonTypes.Array.ordinal -> createArrayView(context)
            JsonTypes.String.ordinal -> createStringView(context)
            JsonTypes.Number.ordinal -> createNumberView(context)
            JsonTypes.Boolean.ordinal -> createBooleanView(context)
            JsonTypes.Null.ordinal -> createNullView(context)
            JsonTypes.PropertyKey.ordinal -> createPropertyView(context)
            JsonTypes.EndObject.ordinal -> TextView(context)
            JsonTypes.EndArray.ordinal -> TextView(context)
            else -> throw RuntimeException("Unexpected view type")
        }

        return JsonViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type.ordinal
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: JsonViewHolder, position: Int) {
        holder.pos = position

        val currentItem = data[position]

        val spacer = holder.view.findViewById<View>(R.id.spacer)
        if (spacer != null) {
            spacer.layoutParams = RelativeLayout.LayoutParams(30*currentItem.depth, 0)
        }

        when (currentItem.type) {
            JsonTypes.Object -> {
                holder.view.findViewById<ImageButton>(R.id.add_button).also { button ->
                    button.setOnClickListener { view ->
                        selectJsonTypeDialog(view.context, true).also {
                            it.setButton(
                                DialogInterface.BUTTON_POSITIVE,
                                "Add"
                            ) { dialog, _ ->
                                val alertDialog = dialog as AlertDialog
                                val listView = alertDialog.listView
                                val selected = listView.adapter.getItem(listView.checkedItemPosition) as String
                                val keyValue = it.findViewById<EditText>(R.id.key_value)!!.text.toString()

                                // Add the property key
                                val propertyKeyPos = holder.pos+1
                                data.add(propertyKeyPos, JsonPropertyKey(keyValue, currentItem.depth+1))
                                notifyItemInserted(propertyKeyPos)

                                // Add the property value
                                insertType(it, this, propertyKeyPos+1, selected, currentItem.depth+1)

                                dialog.dismiss()
                            }
                        }.show()
                    }
                }
            }

            JsonTypes.Array -> {
                holder.view.findViewById<ImageButton>(R.id.add_button).also { button ->
                    button.setOnClickListener { view ->
                        selectJsonTypeDialog(view.context, false).also {
                            it.setButton(
                                DialogInterface.BUTTON_POSITIVE,
                                "Add"
                            ) { dialog, _ ->
                                val alertDialog = dialog as AlertDialog
                                val listView = alertDialog.listView
                                val selected = listView.adapter.getItem(listView.checkedItemPosition) as String

                                insertType(it, this, holder.pos+1, selected, currentItem.depth+1)

                                dialog.dismiss()
                            }
                        }.show()
                    }
                }
            }

            JsonTypes.String -> {
                val stringItem = currentItem as JsonString
                holder.view.findViewById<EditText>(R.id.value).apply {
                    setText(stringItem.value)
                    addTextChangedListener(object : SimpleTextWatcher() {
                        override fun onTextChanged(s: String) {
                            stringItem.value = s
                        }
                    })
                }
            }

            JsonTypes.Number -> {
                val numberItem = currentItem as JsonNumber
                holder.view.findViewById<EditText>(R.id.value).apply {
                    setText(numberItem.value.toString())
                    addTextChangedListener(object : SimpleTextWatcher() {
                        override fun onTextChanged(s: String) {
                            numberItem.value = s.toDouble()
                        }
                    })
                }
            }

            JsonTypes.Boolean -> {
                val booleanItem = currentItem as JsonBoolean
                holder.view.findViewById<ToggleButton>(R.id.value).apply {
                    isChecked = booleanItem.value
                    addTextChangedListener(object : SimpleTextWatcher() {
                        override fun onTextChanged(s: String) {
                            booleanItem.value = s.toBoolean()
                        }
                    })
                }
            }

            JsonTypes.PropertyKey -> {
                val propertyItem = currentItem as JsonPropertyKey
                holder.view.findViewById<EditText>(R.id.key).apply {
                    setText(propertyItem.key)
                    addTextChangedListener(object : SimpleTextWatcher() {
                        override fun onTextChanged(s: String) {
                            propertyItem.key = s
                        }
                    })
                }
            }

            JsonTypes.Null,
            JsonTypes.EndObject,
            JsonTypes.EndArray
            -> {}
        }
    }

    private fun insertType(
        alertDialog: AlertDialog, adapter: JsonAdapter, insertPos: Int, name: String, depth: Int
    ) {
        when (name) {
            JsonTypes.Object.name -> {
                adapter.data.add(insertPos, JsonEndObject(depth))
                adapter.notifyItemInserted(insertPos)

                adapter.data.add(insertPos, JsonObject(depth))
                adapter.notifyItemInserted(insertPos)
            }

            JsonTypes.Array.name -> {
                adapter.data.add(insertPos, JsonEndArray(depth))
                adapter.notifyItemInserted(insertPos)

                adapter.data.add(insertPos, JsonArray(depth))
                adapter.notifyItemInserted(insertPos)
            }

            JsonTypes.String.name -> {
                val stringVal = alertDialog.findViewById<EditText>(R.id.string_value)!!.text.toString()
                adapter.data.add(insertPos, JsonString(stringVal, depth))
                adapter.notifyItemInserted(insertPos)
            }

            JsonTypes.Number.name -> {
                val numberVal = alertDialog.findViewById<EditText>(R.id.number_value)!!.text.toString().toDouble()
                adapter.data.add(insertPos, JsonNumber(numberVal, depth))
                adapter.notifyItemInserted(insertPos)
            }

            JsonTypes.Boolean.name -> {
                val booleanVal = alertDialog.findViewById<ToggleButton>(R.id.boolean_value)!!.isChecked
                adapter.data.add(insertPos, JsonBoolean(booleanVal, depth))
                adapter.notifyItemInserted(insertPos)
            }

            JsonTypes.Null.name -> {
                adapter.data.add(insertPos, JsonNull(depth))
                adapter.notifyItemInserted(insertPos)
            }
        }
    }
}

private abstract class SimpleTextWatcher: TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        onTextChanged(s!!.toString())
    }

    abstract fun onTextChanged(s: String)
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}

internal class JsonViewHolder(
    val view: View,
    var pos: Int = -1
): RecyclerView.ViewHolder(view)
