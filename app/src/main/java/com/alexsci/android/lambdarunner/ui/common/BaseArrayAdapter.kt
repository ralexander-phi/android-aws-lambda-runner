package com.alexsci.android.lambdarunner.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alexsci.android.lambdarunner.R

open class BaseArrayAdapter<T>(
    protected val data: MutableList<T>
): RecyclerView.Adapter<ViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.t = data[position]

        // Start hidden
        holder.description.visibility = View.GONE
        holder.buttonPanel.visibility = View.GONE

        holder.title.setOnClickListener {
            if (holder.description.visibility == View.GONE) {
                holder.description.visibility = View.VISIBLE
                holder.buttonPanel.visibility = View.VISIBLE
            } else {
                holder.description.visibility = View.GONE
                holder.buttonPanel.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = data.size
}

