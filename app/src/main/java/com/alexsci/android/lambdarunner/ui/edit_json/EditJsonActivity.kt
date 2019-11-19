package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
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

    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.json_editor)
        rootLayout = findViewById(R.id.root_holder)
    }

    override fun onStart() {
        super.onStart()

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
                    JsonTypes.Object.name ->
                        rootLayout.addView(createObjectView(baseContext))
                    JsonTypes.Array.name ->
                        rootLayout.addView(createArrayView(baseContext))
                    JsonTypes.String.name ->
                        rootLayout.addView(createStringView(baseContext))
                    JsonTypes.Number.name ->
                        rootLayout.addView(createNumberView(baseContext))
                    JsonTypes.Boolean.name ->
                        rootLayout.addView(createBooleanView(baseContext))
                    JsonTypes.Null.name ->
                        rootLayout.addView(createNullView(baseContext))
                }

                dialog.dismiss()
            }
        }.show()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.putString("Foo", "Bar")

        // TODO
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)

        Log.i(LOG_TAG, savedInstanceState?.getString("Foo")!!)
    }
}

internal class JsonArrayAdapter: RecyclerView.Adapter<JsonViewHolder>() {
    val data: MutableList<JsonType> = LinkedList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsonViewHolder {
        val context = parent.context

        val view = when (viewType) {
            JsonTypes.Object.ordinal -> createObjectView(context)
            JsonTypes.Array.ordinal -> createArrayView(context)
            JsonTypes.String.ordinal -> createStringView(context, JsonString("Default"))
            JsonTypes.Number.ordinal -> createNumberView(context, JsonNumber(42.0))
            JsonTypes.Boolean.ordinal -> createBooleanView(context, JsonBoolean(false))
            JsonTypes.Null.ordinal -> createNullView(context)
            JsonTypes.Property.ordinal -> createPropertyView(context)
            else -> throw RuntimeException("Unexpected view type")
        }

        return JsonViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type.ordinal
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: JsonViewHolder, position: Int) {
        val currentItem = data[position]

        when (currentItem.type) {
            JsonTypes.Object -> {}

            JsonTypes.Array -> {}

            JsonTypes.String -> {
                val stringItem = currentItem as JsonString
                holder.view.findViewById<EditText>(R.id.value).setText(stringItem.value)
            }

            JsonTypes.Number -> {
                val numberItem = currentItem as JsonNumber
                holder.view.findViewById<EditText>(R.id.value).setText(numberItem.value.toString())
            }

            JsonTypes.Boolean -> {
                val booleanItem = currentItem as JsonBoolean
                holder.view.findViewById<ToggleButton>(R.id.value).isChecked = booleanItem.value
            }

            JsonTypes.Null -> {}

            JsonTypes.Property -> {
                val propertyItem = currentItem as JsonProperty
                val key = holder.view.findViewById<EditText>(R.id.key)
                val valueHolder = holder.view.findViewById<LinearLayout>(R.id.root_holder)

                valueHolder.removeAllViews()

                key.setText(propertyItem.key)

                var valueView: View = when (currentItem.value.type) {
                    JsonTypes.Object -> createObjectView(holder.view.context)
                    JsonTypes.Array -> createArrayView(holder.view.context)
                    JsonTypes.String -> createStringView(
                        holder.view.context,
                        currentItem.value as JsonString
                    )
                    JsonTypes.Number -> createNumberView(
                        holder.view.context,
                        currentItem.value as JsonNumber
                    )
                    JsonTypes.Boolean -> createBooleanView(
                        holder.view.context,
                        currentItem.value as JsonBoolean
                    )
                    JsonTypes.Null -> createNullView(holder.view.context)
                    else -> throw java.lang.RuntimeException("Unexpected type")
                }

                valueHolder.addView(valueView)
            }

            else -> throw RuntimeException("Unexpected view type")
        }
    }
}

internal class JsonViewHolder(
    val view: View
): RecyclerView.ViewHolder(view)
