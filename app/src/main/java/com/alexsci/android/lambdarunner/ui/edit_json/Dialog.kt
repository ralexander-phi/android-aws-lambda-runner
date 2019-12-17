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

open class EditDialog(val context: Context) {
    val view: View

    val keyLabel: TextView
    val keyValue: EditText
    val objectTextView: TextView
    val arrayTextView: TextView
    val jsonValueLabel: TextView
    val jsonTypeSpinner: Spinner
    val editButton: ImageButton
    val booleanToggle: ToggleButton
    val numberEditText: EditText
    val stringEditText: EditText

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.update_property_dialog, null)

        keyLabel = view.findViewById(R.id.key_label)
        keyValue = view.findViewById(R.id.key_value)
        objectTextView = view.findViewById(R.id.object_textview)
        arrayTextView = view.findViewById(R.id.array_textview)
        jsonValueLabel = view.findViewById(R.id.json_label)
        jsonTypeSpinner = view.findViewById(R.id.json_type)
        editButton = view.findViewById(R.id.edit)
        booleanToggle = view.findViewById(R.id.boolean_toggle)
        numberEditText = view.findViewById(R.id.number_edit)
        stringEditText = view.findViewById(R.id.string_edit)
    }

    protected fun getUpdatedJsonValue(): JsonElement {
        return when (jsonTypeSpinner.selectedItemPosition) {
            JsonType.OBJECT.ordinal -> JsonParser().parse(objectTextView.text.toString())
            JsonType.ARRAY.ordinal -> JsonParser().parse(arrayTextView.text.toString())
            JsonType.STRING.ordinal -> JsonPrimitive(stringEditText.text.toString())
            JsonType.NUMBER.ordinal -> JsonPrimitive(numberEditText.text.toString().toBigDecimal())
            JsonType.BOOLEAN.ordinal -> JsonPrimitive(booleanToggle.isChecked)
            JsonType.NULL.ordinal -> JsonNull.INSTANCE
            else -> throw RuntimeException("Unexpected")
        }
    }

    protected fun createViews(element: JsonElement?) {
        val originalType: JsonType

        when {
            element == null -> {
                stringEditText.setText("text")
                originalType = JsonType.STRING
            }
            element.isJsonObject -> {
                objectTextView.text = Helpers.prettySingleLineJsonString(element)
                originalType = JsonType.OBJECT
            }
            element.isJsonArray -> {
                arrayTextView.text = Helpers.prettySingleLineJsonString(element)
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
        jsonTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                        Helpers.hideViews(
                            arrayTextView,
                            booleanToggle,
                            numberEditText,
                            stringEditText
                        )
                    }

                    JsonType.ARRAY.ordinal -> {
                        if (originalType.ordinal != position) {
                            arrayTextView.text = "[]"
                        }
                        Helpers.showViews(jsonValueLabel, arrayTextView, editButton)
                        Helpers.hideViews(
                            objectTextView,
                            booleanToggle,
                            numberEditText,
                            stringEditText
                        )
                    }

                    JsonType.STRING.ordinal -> {
                        if (originalType.ordinal != position) {
                            stringEditText.setText("text")
                        }
                        Helpers.showViews(jsonValueLabel, stringEditText)
                        Helpers.hideViews(
                            booleanToggle,
                            numberEditText,
                            objectTextView,
                            arrayTextView,
                            editButton
                        )
                    }

                    JsonType.NUMBER.ordinal -> {
                        if (originalType.ordinal != position) {
                            numberEditText.setText("42")
                        }
                        Helpers.showViews(jsonValueLabel, numberEditText)
                        Helpers.hideViews(
                            booleanToggle,
                            objectTextView,
                            arrayTextView,
                            editButton,
                            stringEditText
                        )
                    }

                    JsonType.BOOLEAN.ordinal -> {
                        if (originalType.ordinal != position) {
                            booleanToggle.isChecked = true
                        }
                        Helpers.showViews(jsonValueLabel, booleanToggle)
                        Helpers.hideViews(
                            objectTextView,
                            arrayTextView,
                            editButton,
                            stringEditText,
                            numberEditText
                        )
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

    }
}

class EditObjectDialog(context: Context): EditDialog(context) {
    fun add(callback: ObjectCallback) {
        edit(null, null, callback)
    }

    fun edit(
        originalKey: String?,
        element: JsonElement?,
        callback: ObjectCallback
    ) {
        Helpers.showViews(keyLabel, keyValue)

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

        createViews(element)

        fun getUpdatedKey(): String {
            return keyValue.text.toString()
        }

        fun save() {
            val newKey = getUpdatedKey()

            if (originalKey != null && originalKey != newKey) {
                callback.onUpdateItem(originalKey, newKey, getUpdatedJsonValue())
            } else {
                callback.onUpdateItem(newKey, getUpdatedJsonValue())
            }
        }

        val alertDialog = AlertDialog.Builder(context)
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
            .create()

        editButton.setOnClickListener {
            // First save the view (we may have changed the key)
            save()
            // Then edit it
            callback.onEditJson(context, getUpdatedKey())
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    interface ObjectCallback {
        fun onRemoveItem(key: String)
        fun onUpdateItem(key: String, element: JsonElement)
        fun onUpdateItem(oldKey: String, newKey: String, element: JsonElement)
        fun onEditJson(context: Context, key: String)
    }
}


class EditArrayDialog(context: Context): EditDialog(context) {
    fun edit(
        index: Int,
        element: JsonElement?,
        callback: ArrayCallback
    ) {
        Helpers.hideViews(keyLabel, keyValue)

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            JsonType.values().map { it.str }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jsonTypeSpinner.adapter = adapter

        createViews(element)

        fun save() {
            callback.onUpdateItem(index, getUpdatedJsonValue())
        }

        val alertDialog = AlertDialog.Builder(context)
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
                callback.onRemoveItem(index)
                dialogInterface.dismiss()
            }
            .create()

        editButton.setOnClickListener {
            // First save the view (we may have changed the key)
            save()
            // Then edit it
            callback.onEditJson(context, index)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    interface ArrayCallback {
        fun onRemoveItem(index: Int)
        fun onUpdateItem(index: Int, element: JsonElement)
        fun onEditJson(context: Context, index: Int)
    }
}
