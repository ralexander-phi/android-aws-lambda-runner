package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
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
    val jsonValue: TextView = view.findViewById(R.id.json_value)
    val actionButton: ImageButton = view.findViewById(R.id.edit)
}

class JsonPropertyArrayAdapter(
    private val path: String,
    private val baseJson: JsonElement,
    initialMap: Map<String, JsonElement>
): RecyclerView.Adapter<JsonPropertyViewHolder>(), JsonEditDialog.Callback {

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

        holder.actionButton.setOnClickListener {
            JsonEditDialog(context).edit(originalKey, element, this)
        }
    }

    override fun onRemoveItem(key: String) {
        val oldPos = contents.keys.toTypedArray().indexOf(key)
        if (oldPos != -1) {
            contents.remove(key)
            notifyItemRemoved(oldPos)
        }
    }

    override fun onUpdateItem(key: String, element: JsonElement) {
        val pos = contents.keys.toTypedArray().indexOf(key)
        if (pos != -1) {
            // Update in place
            contents[key] = element
            notifyItemChanged(pos)
        } else {
            // Adding item
            contents[key] = element
            val newPos = contents.keys.toTypedArray().indexOf(key)
            notifyItemInserted(newPos)
        }
    }

    override fun onUpdateItem(oldKey: String, newKey: String, element: JsonElement) {
        contents.remove(oldKey)
        contents[newKey] = element
        notifyDataSetChanged()
    }

    override fun onEditJson(context: Context, key: String) {
        val intent = when {
            contents[key]!!.isJsonObject -> Intent(context, EditJsonObjectActivity::class.java)
            // TODO
            //contents[key]!!.isJsonArray-> Intent(context, EditJsonArrayActivity::class.java)
            else -> throw RuntimeException("Can only edit container types")
        }

        intent.putExtra(EditJsonActivity.JSON_EXTRA, getUpdatedJsonRoot().toString())
        intent.putExtra(EditJsonActivity.EDIT_PATH_EXTRA, "${path}.${key}")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        context.startActivity(intent)
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

