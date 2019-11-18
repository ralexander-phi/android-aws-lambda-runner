package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R

internal fun createObjectView(context: Context): View {
    val objectView = LayoutInflater
        .from(context)
        .inflate(R.layout.json_object_view, null)!!

    val recyclerView = objectView.findViewById<RecyclerView>(R.id.recycler_view).apply {
        this.setHasFixedSize(false)
        this.layoutManager = LinearLayoutManager(context)
    }

    val propertyAdapter = JsonArrayAdapter()
    recyclerView.adapter = propertyAdapter

    objectView.findViewById<ImageButton>(R.id.add_button).also { button ->
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

                    Log.i(EditJsonActivity.LOG_TAG, selected)
                    when (selected) {
                        JsonTypes.Object.name -> {
                            propertyAdapter.data.add(
                                JsonProperty(
                                    keyValue,
                                    JsonObject()
                                )
                            )
                        }
                        JsonTypes.Array.name -> {
                            propertyAdapter.data.add(
                                JsonProperty(
                                    keyValue,
                                    JsonArray()
                                )
                            )
                        }
                        JsonTypes.String.name -> {
                            val stringEdit = it.findViewById<EditText>(R.id.string_value)!!
                            propertyAdapter.data.add(
                                JsonProperty(
                                    keyValue,
                                    JsonString(stringEdit.text.toString())
                                )
                            )
                        }
                        JsonTypes.Number.name -> {
                            val numberEdit = it.findViewById<EditText>(R.id.number_value)
                            propertyAdapter.data.add(
                                JsonProperty(
                                    keyValue,
                                    JsonNumber(numberEdit!!.text.toString().toDouble())
                                )
                            )
                        }
                        JsonTypes.Boolean.name -> {
                            val booleanEdit = it.findViewById<ToggleButton>(R.id.boolean_value)
                            propertyAdapter.data.add(
                                JsonProperty(
                                    keyValue,
                                    JsonBoolean(booleanEdit!!.isChecked)
                                )
                            )
                        }
                        JsonTypes.Null.name ->
                            propertyAdapter.data.add(
                                JsonProperty(
                                    keyValue,
                                    JsonNull()
                                )
                            )
                    }
                    propertyAdapter.notifyDataSetChanged()

                    dialog.dismiss()
                }
            }.show()
        }
    }

    return objectView
}

internal fun createArrayView(context: Context): View {
    val arrayView = LayoutInflater
        .from(context)
        .inflate(R.layout.json_array_view, null)!!
    val recyclerView = arrayView.findViewById<RecyclerView>(R.id.recycler_view).apply {
        this.setHasFixedSize(false)
        this.layoutManager = LinearLayoutManager(context)
    }

    val propertyAdapter = JsonArrayAdapter()
    recyclerView.adapter = propertyAdapter

    arrayView.findViewById<ImageButton>(R.id.add_button).also { button ->
        button.setOnClickListener { view ->
            selectJsonTypeDialog(view.context, false).also {
                it.setButton(
                    DialogInterface.BUTTON_POSITIVE,
                    "Add"
                ) { dialog, _ ->
                    val alertDialog = dialog as AlertDialog
                    val listView = alertDialog.listView
                    val selected = listView.adapter.getItem(listView.checkedItemPosition) as String

                    Log.i(EditJsonActivity.LOG_TAG, selected)
                    when (selected) {
                        JsonTypes.Object.name ->
                            propertyAdapter.data.add(JsonObject())
                        JsonTypes.Array.name ->
                            propertyAdapter.data.add(JsonArray())
                        JsonTypes.String.name -> {
                            val stringEdit = it.findViewById<EditText>(R.id.string_value)
                            propertyAdapter.data.add(JsonString(stringEdit!!.text.toString()))
                        }
                        JsonTypes.Number.name -> {
                            val numberEdit = it.findViewById<EditText>(R.id.number_value)
                            propertyAdapter.data.add(JsonNumber(numberEdit!!.text.toString().toDouble()))
                        }
                        JsonTypes.Boolean.name -> {
                            val booleanEdit = it.findViewById<ToggleButton>(R.id.boolean_value)
                            propertyAdapter.data.add(JsonBoolean(booleanEdit!!.isChecked))
                        }
                        JsonTypes.Null.name ->
                            propertyAdapter.data.add(JsonNull())
                    }
                    propertyAdapter.notifyDataSetChanged()

                    dialog.dismiss()
                }
            }.show()
        }
    }

    return arrayView
}

internal fun createStringView(context: Context, stringItem: JsonString? = null): View {
    val valueView = LayoutInflater
        .from(context)
        .inflate(R.layout.json_string_view, null)
    val stringEdit = valueView.findViewById<EditText>(R.id.value)
    if (stringItem != null) {
        stringEdit.setText(stringItem.value)
    } else {
        stringEdit.setText("Default")
    }
    return valueView!!
}

internal fun createNumberView(context: Context, numberItem: JsonNumber? = null): View {
    val valueView = LayoutInflater
        .from(context)
        .inflate(R.layout.json_number_view, null)
    val numberEdit = valueView.findViewById<EditText>(R.id.value)
    if (numberItem != null) {
        numberEdit.setText(numberItem.value.toString())
    } else {
        numberEdit.setText("42.0")
    }
    return valueView!!
}

internal fun createBooleanView(context: Context, booleanItem: JsonBoolean? = null): View {
    val valueView = LayoutInflater
        .from(context)
        .inflate(R.layout.json_boolean_view, null)
    val booleanEdit = valueView.findViewById<ToggleButton>(R.id.value)
    if (booleanItem != null) {
        booleanEdit.isChecked = booleanItem.value
    } else {
        booleanEdit.isChecked = false
    }
    return valueView!!
}

internal fun createNullView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_null_view, null)!!
}

internal fun createPropertyView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_object_property_view, null)!!
}

internal fun selectJsonTypeDialog(context: Context, isProperty: Boolean): AlertDialog {

    val alertBuilder = AlertDialog.Builder(context)
    alertBuilder.setTitle("Select a type")

    val view = LayoutInflater.from(context).inflate(R.layout.create_json_type, null)

    val stringEdit = view.findViewById<EditText>(R.id.string_value)
    val numberEdit = view.findViewById<EditText>(R.id.number_value)
    val booleanEdit = view.findViewById<ToggleButton>(R.id.boolean_value)
    val keyEdit = view.findViewById<EditText>(R.id.key_value)

    if (isProperty) {
        keyEdit.visibility = View.VISIBLE
    } else {
        keyEdit.visibility = View.GONE
    }

    fun hideInputs() {
        numberEdit.isVisible = false
        stringEdit.isVisible = false
        booleanEdit.isVisible = false
    }

    fun showOnly(view: View) {
        hideInputs()
        view.isVisible = true
    }

    hideInputs()

    alertBuilder.setView(view)

    val jsonTypesArray = JsonTypes.values().map { v -> v.name }.filter{it != "Property"}.toTypedArray()
    alertBuilder.setSingleChoiceItems(
        jsonTypesArray,
        0
    ) { _, which ->
        when (jsonTypesArray[which]) {
            JsonTypes.Object.name -> { hideInputs() }
            JsonTypes.Array.name -> { hideInputs() }
            JsonTypes.String.name -> { showOnly(stringEdit) }
            JsonTypes.Number.name -> { showOnly(numberEdit) }
            JsonTypes.Boolean.name -> { showOnly(booleanEdit) }
            JsonTypes.Null.name -> { hideInputs() }
        }
    }
    return alertBuilder.create()!!
}

