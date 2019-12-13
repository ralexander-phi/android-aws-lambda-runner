package com.alexsci.android.lambdarunner.ui.edit_json

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R

class BreadCrumbPart(val name: String, val path: String)

class BreadCrumbViewHolder(val view: TextView): RecyclerView.ViewHolder(view)
class BreadCrumbArrayAdapter(
    val list: MutableList<BreadCrumbPart>
): RecyclerView.Adapter<BreadCrumbViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreadCrumbViewHolder {
        val textView = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.path_breadcrumb,
                parent,
                false
            )
        return BreadCrumbViewHolder(textView as TextView)
    }

    override fun onBindViewHolder(holder: BreadCrumbViewHolder, position: Int) {
        holder.view.text = list[position].name
        holder.view.setOnClickListener {

        }
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemCount(): Int {
        return list.size
    }
}

