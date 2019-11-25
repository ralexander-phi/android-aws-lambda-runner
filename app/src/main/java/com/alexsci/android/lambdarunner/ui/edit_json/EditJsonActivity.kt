package com.alexsci.android.lambdarunner.ui.edit_json

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonElement
import java.util.*


class EditJsonActivity: AppCompatActivity() {
    companion object {
        const val EXTRA_JSON_SCHEMA = "com.alexsci.android.lambdarunner.ui.edit_json.json_schema"
        const val EXTRA_LAMBDA_CLIENT_BUILDER =
            "com.alexsci.android.lambdarunner.ui.edit_json.lambda_client_builder"
        const val EXTRA_LAMBDA_FUNCTION_NAME=
            "com.alexsci.android.lambdarunner.ui.edit_json.lambda_function_name"
        const val LOG_TAG = "EditJsonActivity"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var viewAdapter: JsonAdapter

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

        findViewById<Button>(R.id.done_button).also {button ->
            button.setOnClickListener {
                val json = getJson()
                setResult(Activity.RESULT_OK, Intent(ACTION_SEND).also {intent ->
                    intent.putExtra(EXTRA_TEXT, json)
                })
                Toast.makeText(this, json, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (viewAdapter.data.isEmpty()) {
            selectRootJsonType()
        }
    }

    fun selectRootJsonType() {
        selectJsonTypeDialog(this, false).also {
            it.setButton(
                DialogInterface.BUTTON_POSITIVE,
                "OK"
            ) { dialog, _ ->
                val alertDialog = dialog as AlertDialog
                val listView = alertDialog.listView
                val selected = listView.adapter.getItem(listView.checkedItemPosition) as String

                Log.i(LOG_TAG, selected)
                when (selected) {
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

    private fun getJson(): String {
        val stack = Stack<JsonElement>()
        val propertyStack = Stack<String>()
        val iter = viewAdapter.data.iterator()

        while (iter.hasNext()) {
            val item = iter.next()

            when (item.type) {
                // Push containers onto the stacks
                JsonTypes.Object -> stack.push(item.asJsonElement())
                JsonTypes.Array -> stack.push(item.asJsonElement())
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

        when (currentItem.type) {
            JsonTypes.Object -> {
                bindObjectView(holder, this)
            }

            JsonTypes.Array -> {
                bindArrayView(holder, this)
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

            JsonTypes.Null -> {}
            JsonTypes.EndObject -> {}
            JsonTypes.EndArray -> {}
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
