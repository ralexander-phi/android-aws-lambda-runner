package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.alexsci.android.lambdarunner.R

internal fun createObjectView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_object_view, null)!!
}

internal fun createArrayView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_array_view, null)!!
}


internal fun createStringView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_string_view, null)
}

internal fun createNumberView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_number_view, null)
}

internal fun createBooleanView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_boolean_view, null)
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

    val jsonTypesArray = JsonTypes.values().map { v -> v.name }.filter{
        it != "PropertyKey" &&
        it != "EndObject" &&
        it != "EndArray"
    }.toTypedArray()

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


