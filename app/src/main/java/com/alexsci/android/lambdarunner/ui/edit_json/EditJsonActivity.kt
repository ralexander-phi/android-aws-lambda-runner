package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.alexsci.android.lambdarunner.ui.common.ViewHolder
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
                    JsonTypes.Object.name -> createObjectRoot()
                    JsonTypes.Array.name -> createArrayRoot()
                    JsonTypes.String.name -> createStringRoot()
                    JsonTypes.Number.name -> createNumberRoot()
                    JsonTypes.Boolean.name -> createBooleanRoot()
                    JsonTypes.Null.name -> createNullRoot()
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

        Log.i(LOG_TAG, savedInstanceState?.getString("Foo"))
    }

    private fun createObjectRoot() {
        val objectView = layoutInflater.inflate(R.layout.json_object_view, null)
        val recyclerView = objectView.findViewById<RecyclerView>(R.id.recycler_view).apply {
            this.setHasFixedSize(false)
            this.layoutManager = LinearLayoutManager(this@EditJsonActivity)
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

                        Log.i(LOG_TAG, selected)
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

        rootLayout.addView(objectView)
    }

    private fun createArrayRoot() {
        val arrayView = layoutInflater.inflate(R.layout.json_array_view, null)
        val recyclerView = arrayView.findViewById<RecyclerView>(R.id.recycler_view).apply {
            this.setHasFixedSize(false)
            this.layoutManager = LinearLayoutManager(this@EditJsonActivity)
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

                        Log.i(LOG_TAG, selected)
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

        rootLayout.addView(arrayView)
    }

    private fun createStringRoot() {
        val rootElement= layoutInflater.inflate(R.layout.json_string_view, null)
        rootLayout.addView(rootElement)
    }

    private fun createNumberRoot() {
        val rootElement = layoutInflater.inflate(R.layout.json_number_view, null)
        rootLayout.addView(rootElement)
    }

    private fun createBooleanRoot() {
        val rootElement = layoutInflater.inflate(R.layout.json_boolean_view, null)
        rootLayout.addView(rootElement)
    }

    private fun createNullRoot() {
        val rootElement = layoutInflater.inflate(R.layout.json_null_view, null)
        rootLayout.addView(rootElement)
    }

    private inner class ArrayObserver(
        val adapter: JsonArrayAdapter
    ): Observer<JsonArray> {
        override fun onChanged(t: JsonArray?) {
            adapter.data.clear()
            adapter.data.addAll(t!!.value)
        }
    }

    private fun selectJsonTypeDialog(context: Context, isProperty: Boolean): AlertDialog {

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
}


enum class JsonTypes {
    Object,
    Array,
    String,
    Number,
    Boolean,
    Null,
    Property
}

private class JsonArrayAdapter: RecyclerView.Adapter<JsonViewHolder>() {
    val data: MutableList<JsonType> = LinkedList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsonViewHolder {
        val layout = when (viewType) {
            JsonTypes.Object.ordinal -> R.layout.json_object_view
            JsonTypes.Array.ordinal -> R.layout.json_array_view
            JsonTypes.String.ordinal -> R.layout.json_string_view
            JsonTypes.Number.ordinal -> R.layout.json_number_view
            JsonTypes.Boolean.ordinal -> R.layout.json_boolean_view
            JsonTypes.Null.ordinal -> R.layout.json_null_view
            JsonTypes.Property.ordinal -> R.layout.json_object_property_view
            else -> throw RuntimeException("Unexpected view type")
        }

        val view = LayoutInflater
            .from(parent.context)
            .inflate(layout, parent, false)
        return JsonViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type.ordinal
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: JsonViewHolder, position: Int) {
        val currentItem = data[position]

        when (currentItem.type.ordinal) {
            JsonTypes.Object.ordinal -> {}

            JsonTypes.Array.ordinal -> {}

            JsonTypes.String.ordinal -> {
                val stringItem = currentItem as JsonString
                holder.view.findViewById<EditText>(R.id.value).setText(stringItem.value)
            }

            JsonTypes.Number.ordinal -> {
                val numberItem = currentItem as JsonNumber
                holder.view.findViewById<EditText>(R.id.value).setText(numberItem.value.toString())
            }

            JsonTypes.Boolean.ordinal -> {
                val booleanItem = currentItem as JsonBoolean
                holder.view.findViewById<ToggleButton>(R.id.value).isChecked = booleanItem.value
            }

            JsonTypes.Null.ordinal -> {}

            JsonTypes.Property.ordinal -> {
                val propertyItem = currentItem as JsonProperty
                val key = holder.view.findViewById<EditText>(R.id.key)
                val valueHolder = holder.view.findViewById<LinearLayout>(R.id.root_holder)

                valueHolder.removeAllViews()

                key.setText(propertyItem.key)

                var valueView: View? = null
                when (currentItem.value.type) {
                    JsonTypes.Object -> {
                        valueView = LayoutInflater
                            .from(holder.view.context)
                            .inflate(R.layout.json_object_view, null)
                    }
                    JsonTypes.Array -> {
                        valueView = LayoutInflater
                            .from(holder.view.context)
                            .inflate(R.layout.json_array_view, null)
                    }
                    JsonTypes.String -> {
                        val stringItem = currentItem.value as JsonString
                        valueView = LayoutInflater
                            .from(holder.view.context)
                            .inflate(R.layout.json_string_view, null)
                        val stringEdit = valueView.findViewById<EditText>(R.id.value)
                        stringEdit.setText(stringItem.value)
                    }
                    JsonTypes.Number -> {
                        val numberItem = currentItem.value as JsonNumber
                        valueView = LayoutInflater
                            .from(holder.view.context)
                            .inflate(R.layout.json_number_view, null)
                        val numberEdit = valueView.findViewById<EditText>(R.id.value)
                        numberEdit.setText(numberItem.value.toString())
                    }
                    JsonTypes.Boolean -> {
                        val booleanItem = currentItem.value as JsonBoolean
                        valueView = LayoutInflater
                            .from(holder.view.context)
                            .inflate(R.layout.json_boolean_view, null)
                        val booleanEdit = valueView.findViewById<ToggleButton>(R.id.value)
                        booleanEdit.isChecked = booleanItem.value
                    }
                    JsonTypes.Null -> {
                        valueView = LayoutInflater
                            .from(holder.view.context)
                            .inflate(R.layout.json_null_view, null)
                    }
                }

                valueHolder.addView(valueView!!)
            }

            else -> throw RuntimeException("Unexpected view type")
        }
    }
}

private class JsonViewHolder(
    val view: View
): RecyclerView.ViewHolder(view)

private class JsonObjectAdapter(
    val data: MutableList<Map.Entry<String, JsonType>>
): RecyclerView.Adapter<ViewHolder<Map.Entry<String, JsonType>>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder<Map.Entry<String, JsonType>> {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_item, parent, true)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder<Map.Entry<String, JsonType>>, position: Int) {
        val currentItem = data[position]
        holder.t = currentItem

        holder.title.text = currentItem.key
        holder.description.text = currentItem.value.toString()
    }

    override fun getItemCount() = data.size
}
