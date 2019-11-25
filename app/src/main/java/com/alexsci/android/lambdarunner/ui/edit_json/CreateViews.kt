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
import com.alexsci.android.lambdarunner.R

internal fun createObjectView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_object_view, null)!!
}

internal fun bindObjectView(viewHolder: JsonViewHolder, adapter: JsonAdapter) {
    viewHolder.view.findViewById<ImageButton>(R.id.add_button).also { button ->
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

                    // Add the property key
                    val propertyKeyPos = viewHolder.pos+1
                    adapter.data.add(propertyKeyPos, JsonPropertyKey(keyValue))
                    adapter.notifyItemInserted(propertyKeyPos)

                    // Add the property value
                    insertType(it, adapter, propertyKeyPos+1, selected)

                    dialog.dismiss()
                }
            }.show()
        }
    }
}

internal fun createArrayView(context: Context): View {
    return LayoutInflater
        .from(context)
        .inflate(R.layout.json_array_view, null)!!
}

internal fun bindArrayView(viewHolder: JsonViewHolder, adapter: JsonAdapter) {
    viewHolder.view.findViewById<ImageButton>(R.id.add_button).also { button ->
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

                    insertType(it, adapter, viewHolder.pos+1, selected)

                    dialog.dismiss()
                }
            }.show()
        }
    }
}

internal fun insertType(alertDialog: AlertDialog, adapter: JsonAdapter, insertPos: Int, name: String) {
    when (name) {
        JsonTypes.Object.name -> {
            adapter.data.add(insertPos, JsonEndObject())
            adapter.notifyItemInserted(insertPos)

            adapter.data.add(insertPos, JsonObject())
            adapter.notifyItemInserted(insertPos)
        }

        JsonTypes.Array.name -> {
            adapter.data.add(insertPos, JsonEndArray())
            adapter.notifyItemInserted(insertPos)

            adapter.data.add(insertPos, JsonArray())
            adapter.notifyItemInserted(insertPos)
        }

        JsonTypes.String.name -> {
            val stringVal = alertDialog.findViewById<EditText>(R.id.string_value)!!.text.toString()
            adapter.data.add(insertPos, JsonString(stringVal))
            adapter.notifyItemInserted(insertPos)
        }

        JsonTypes.Number.name -> {
            val numberVal = alertDialog.findViewById<EditText>(R.id.number_value)!!.text.toString().toDouble()
            adapter.data.add(insertPos, JsonNumber(numberVal))
            adapter.notifyItemInserted(insertPos)
        }

        JsonTypes.Boolean.name -> {
            val booleanVal = alertDialog.findViewById<ToggleButton>(R.id.boolean_value)!!.isChecked
            adapter.data.add(insertPos, JsonBoolean(booleanVal))
            adapter.notifyItemInserted(insertPos)
        }

        JsonTypes.Null.name -> {
            adapter.data.add(insertPos, JsonNull())
            adapter.notifyItemInserted(insertPos)
        }
    }

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


