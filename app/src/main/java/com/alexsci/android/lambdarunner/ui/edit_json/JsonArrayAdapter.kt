package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonArray
import com.google.gson.JsonElement

class JsonArrayItemViewHolder(
    val view: RelativeLayout
): RecyclerView.ViewHolder(view) {
    val icon: ImageButton = view.findViewById(R.id.icon)
    val jsonValue: TextView = view.findViewById(R.id.json_value)
}

class JsonArrayAdapter(
    private val path: String,
    private val baseJson: JsonElement,
    initial: Iterable<JsonElement>,
    private val activity: EditJsonActivity
): RecyclerView.Adapter<JsonArrayItemViewHolder>(), EditArrayDialog.ArrayCallback {

    val contents = ArrayList<JsonElement>().also { it.addAll(initial) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsonArrayItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.json_array_item,
                parent,
                false
            )
        return JsonArrayItemViewHolder(view as RelativeLayout)
    }

    override fun onBindViewHolder(holder: JsonArrayItemViewHolder, position: Int) {
        val context = holder.view.context
        val element = contents[position]
        val jsonText = Helpers.prettySingleLineJsonString(element)

        holder.jsonValue.text = jsonText

        val onClickListener = View.OnClickListener {
            EditArrayDialog(context).edit(position, element, this@JsonArrayAdapter)
        }

        // Tapping any of these will edit
        holder.jsonValue.setOnClickListener(onClickListener)
        holder.icon.setOnClickListener(onClickListener)

        holder.icon.setImageResource(JsonType.of(element).imageButtonIcon)
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemCount(): Int {
        return contents.size
    }

    private fun jsonForView(): JsonArray {
        return JsonArray().also {
            for (item in contents) {
                it.add(item)
            }
        }
    }

    fun getUpdatedJsonRoot(): JsonElement {
        return JqJsonUpdater(baseJson).update(path, jsonForView())
    }

    override fun onRemoveItem(index: Int) {
        if (index <= contents.size) {
            // Existing element, remove it
            contents.removeAt(index)
            notifyDataSetChanged()
        }
    }

    override fun onUpdateItem(index: Int, element: JsonElement) {
        if (index < contents.size) {
            // Existing element, update it
            contents[index] = element
        } else {
            // New item, add to end
            contents.add(element)
        }
        notifyDataSetChanged()
    }

    override fun onEditJson(context: Context, index: Int) {
        activity.updateView("${path}[${index}]")
    }
}

