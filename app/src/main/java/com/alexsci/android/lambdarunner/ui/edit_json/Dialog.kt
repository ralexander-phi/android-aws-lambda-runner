package com.alexsci.android.lambdarunner.ui.edit_json

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

enum class JsonType(val str: String) {
    OBJECT("Object"),
    ARRAY("Array"),
    STRING("String"),
    NUMBER("Number"),
    BOOLEAN("Boolean"),
    NULL("null")
}

class JsonEditDialog(val context: Context) {
    fun edit(
        originalKey: String,
        element: JsonElement,
        callback: Callback
    ) {
        create(originalKey, element, callback)
    }

    fun add(callback: Callback) {
        create(null, null, callback)
    }

    private fun create(
        originalKey: String?,
        element: JsonElement?,
        callback: Callback
    ) {
        val jsonText = if (element != null) Helpers.prettySingleLineJsonString(element) else "text"

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.update_property_dialog, null)

        val keyValue = view.findViewById<EditText>(R.id.key_value)
        val objectTextView = view.findViewById<TextView>(R.id.object_textview)
        val arrayTextView = view.findViewById<TextView>(R.id.array_textview)
        val jsonValueLabel = view.findViewById<TextView>(R.id.json_label)
        val jsonTypeSpinner = view.findViewById<Spinner>(R.id.json_type)
        val editButton = view.findViewById<ImageButton>(R.id.edit)
        val booleanToggle = view.findViewById<ToggleButton>(R.id.boolean_toggle)
        val numberEditText = view.findViewById<EditText>(R.id.number_edit)
        val stringEditText = view.findViewById<EditText>(R.id.string_edit)

        if (originalKey == null) {
            keyValue.setText("new")
        } else {
            keyValue.setText(originalKey)
        }

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            JsonType.values().map { it.str }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jsonTypeSpinner.adapter = adapter

        val originalType: JsonType

        when {
            element == null -> {
                stringEditText.setText("text")
                originalType = JsonType.STRING
            }
            element.isJsonObject -> {
                objectTextView.text = jsonText
                originalType = JsonType.OBJECT
            }
            element.isJsonArray -> {
                arrayTextView.text = jsonText
                originalType = JsonType.ARRAY
            }
            element.isJsonNull -> {
                originalType = JsonType.NULL
            }
            element.isJsonPrimitive -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isString -> {
                        stringEditText.setText(primitive.asString.toString())
                        originalType = JsonType.STRING
                    }
                    primitive.isNumber -> {
                        numberEditText.setText(primitive.toString())
                        originalType = JsonType.NUMBER
                    }
                    primitive.isBoolean -> {
                        booleanToggle.isChecked = primitive.asBoolean
                        originalType = JsonType.BOOLEAN
                    }
                    else -> throw java.lang.RuntimeException("Unexpected")
                }
            }
            else -> throw java.lang.RuntimeException("Unexpected")
        }

        jsonTypeSpinner.setSelection(originalType.ordinal)
        jsonTypeSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    JsonType.OBJECT.ordinal -> {
                        if (originalType.ordinal != position) {
                            objectTextView.text = "{}"
                        }
                        Helpers.showViews(jsonValueLabel, objectTextView, editButton)
                        Helpers.hideViews(arrayTextView, booleanToggle, numberEditText, stringEditText)
                    }

                    JsonType.ARRAY.ordinal -> {
                        if (originalType.ordinal != position) {
                            arrayTextView.text = "[]"
                        }
                        Helpers.showViews(jsonValueLabel, arrayTextView, editButton)
                        Helpers.hideViews(objectTextView, booleanToggle, numberEditText, stringEditText)
                    }

                    JsonType.STRING.ordinal -> {
                        if (originalType.ordinal != position) {
                            stringEditText.setText("text")
                        }
                        Helpers.showViews(jsonValueLabel, stringEditText)
                        Helpers.hideViews(booleanToggle, numberEditText, objectTextView, arrayTextView, editButton)
                    }

                    JsonType.NUMBER.ordinal -> {
                        if (originalType.ordinal != position) {
                            numberEditText.setText("42")
                        }
                        Helpers.showViews(jsonValueLabel, numberEditText)
                        Helpers.hideViews(booleanToggle, objectTextView, arrayTextView, editButton, stringEditText)
                    }

                    JsonType.BOOLEAN.ordinal -> {
                        if (originalType.ordinal != position) {
                            booleanToggle.isChecked = true
                        }
                        Helpers.showViews(jsonValueLabel, booleanToggle)
                        Helpers.hideViews(objectTextView, arrayTextView, editButton, stringEditText, numberEditText)
                    }

                    JsonType.NULL.ordinal -> {
                        Helpers.hideViews(
                            objectTextView, arrayTextView,
                            editButton, stringEditText,
                            numberEditText, jsonValueLabel, booleanToggle
                        )
                    }
                }
            }
        }

        fun getUpdateKey(): String {
            return keyValue.text.toString()
        }

        fun save() {
            val newKey = getUpdateKey()

            val newJsonElement: JsonElement = when (jsonTypeSpinner.selectedItemPosition) {
                JsonType.OBJECT.ordinal -> JsonParser().parse(objectTextView.text.toString())
                JsonType.ARRAY.ordinal -> JsonParser().parse(arrayTextView.text.toString())
                JsonType.STRING.ordinal -> JsonPrimitive(stringEditText.text.toString())
                JsonType.NUMBER.ordinal -> JsonPrimitive(numberEditText.text.toString().toDouble())
                JsonType.BOOLEAN.ordinal -> JsonPrimitive(booleanToggle.isChecked)
                JsonType.NULL.ordinal -> JsonNull.INSTANCE
                else -> throw RuntimeException("Unexpected")
            }

            if (originalKey != null && originalKey != newKey) {
                callback.onUpdateItem(originalKey, newKey, newJsonElement)
            } else {
                callback.onUpdateItem(newKey, newJsonElement)
            }
        }

        editButton.setOnClickListener {
            // First have the view (we may have changed the key)
            save()
            // Then edit it
            callback.onEditJson(context, getUpdateKey())
        }

        AlertDialog.Builder(context)
            .setMessage("Update Property")
            .setView(view)
            .setPositiveButton("Save") { dialogInterface, _ ->
                save()
                dialogInterface.dismiss()
            }
            .setNeutralButton("Cancel") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .setNegativeButton("Remove") { dialogInterface, _ ->
                if (originalKey != null) {
                    callback.onRemoveItem(originalKey)
                }
                dialogInterface.dismiss()
            }
            .create().show()
    }

    interface Callback {
        fun onRemoveItem(key: String)
        fun onUpdateItem(key: String, element: JsonElement)
        fun onUpdateItem(oldKey: String, newKey: String, element: JsonElement)
        fun onEditJson(context: Context, key: String)
    }
}