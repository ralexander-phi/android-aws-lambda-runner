package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.*

class JsonPropertyViewHolder(
    val view: RelativeLayout
): RecyclerView.ViewHolder(view) {
    val keyText: TextView = view.findViewById(R.id.key)
    val icon: ImageButton = view.findViewById(R.id.icon)
    val jsonValue: TextView = view.findViewById(R.id.json_value)
    val valueContainer: LinearLayout = view.findViewById(R.id.value_container)
    val propertyLayout: RelativeLayout = view.findViewById(R.id.property)
}

class JsonPropertyArrayAdapter(
    private val path: String,
    private val baseJson: JsonElement,
    initialMap: Map<String, JsonElement>,
    private val activity: EditJsonActivity
): RecyclerView.Adapter<JsonPropertyViewHolder>(), EditObjectDialog.ObjectCallback {

    val contents = TreeMap<String, JsonElement>(initialMap)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsonPropertyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.json_property,
                parent,
                false
            )
        return JsonPropertyViewHolder(view as RelativeLayout)
    }

    override fun onBindViewHolder(holder: JsonPropertyViewHolder, position: Int) {
        val context = holder.view.context
        val originalKey = contents.keys.toTypedArray()[position]
        val element = contents[originalKey]!!
        val jsonText = Helpers.prettySingleLineJsonString(element)

        holder.keyText.text = originalKey
        holder.jsonValue.text = jsonText

        // Tapping any of these will edit
        listOf(
            holder.keyText, holder.jsonValue, holder.icon,
            holder.valueContainer, holder.propertyLayout
        ).forEach {
            it.setOnClickListener {
                EditObjectDialog(context).edit(originalKey, element, this@JsonPropertyArrayAdapter)
            }
        }

        holder.icon.setImageResource(JsonType.of(element).imageButtonIcon)
    }

    override fun onRemoveItem(key: String) {
        contents.remove(key)
        notifyDataSetChanged()
    }

    override fun onUpdateItem(key: String, element: JsonElement) {
        contents[key] = element
        notifyDataSetChanged()
    }

    override fun onUpdateItem(oldKey: String, newKey: String, element: JsonElement) {
        contents.remove(oldKey)
        contents[newKey] = element
        notifyDataSetChanged()
    }

    override fun onEditJson(context: Context, key: String) {
        activity.updateView("${path}.${key}")
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemCount(): Int {
        return contents.size
    }

    private fun jsonForView(): JsonObject {
        return JsonObject().also {
            for (property in contents.entries) {
                it.add(property.key, property.value)
            }
        }
    }

    fun getUpdatedJsonRoot(): JsonElement {
        return JqJsonUpdater(baseJson).update(path, jsonForView())
    }
}

