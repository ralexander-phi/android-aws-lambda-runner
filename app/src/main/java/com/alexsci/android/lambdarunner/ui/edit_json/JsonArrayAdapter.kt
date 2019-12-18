package com.alexsci.android.lambdarunner.ui.edit_json

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
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
    val upButton: ImageButton = view.findViewById(R.id.up)
    val downButton: ImageButton = view.findViewById(R.id.down)
    val valueContainer: RelativeLayout = view.findViewById(R.id.value_container)
    val arrayItemLayout: RelativeLayout = view.findViewById(R.id.array_item)
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

        // Tapping any of these will edit
        listOf(
            holder.jsonValue, holder.icon, holder.valueContainer, holder.arrayItemLayout
        ).forEach {
            it.setOnClickListener {
                EditArrayDialog(context).edit(position, element, this@JsonArrayAdapter)
            }
        }

        holder.icon.setImageResource(JsonType.of(element).imageButtonIcon)

        holder.upButton.also {
            if (position == 0) {
                it.setColorFilter(Color.GRAY)
                it.setOnClickListener(null)
            } else {
                it.setColorFilter(Color.DKGRAY)
                it.setOnClickListener {
                    // Swap this item with the one above it
                    val tmp = contents[position-1]
                    contents[position-1] = contents[position]
                    contents[position] = tmp
                    notifyItemRangeChanged(position-1, 2)
                }
            }
        }

        holder.downButton.also {
            if (position == contents.size-1) {
                it.setColorFilter(Color.GRAY)
                it.setOnClickListener(null)
            } else {
                it.setColorFilter(Color.DKGRAY)
                it.setOnClickListener {
                    // Swap this item with the one below it
                    val tmp = contents[position+1]
                    contents[position+1] = contents[position]
                    contents[position] = tmp
                    notifyItemRangeChanged(position, 2)
                }
            }
        }
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
        if (index < contents.size) {
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

