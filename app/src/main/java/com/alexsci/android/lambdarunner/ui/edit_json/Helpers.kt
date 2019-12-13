package com.alexsci.android.lambdarunner.ui.edit_json

import android.view.View
import com.google.gson.JsonElement
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import java.io.StringWriter

class Helpers {
    companion object {
        fun prettySingleLineJsonString(element: JsonElement): String {
            val str = StringWriter()
            val writer = JsonWriter(str)
            writer.setIndent(" ")
            writer.isLenient = true // Allow non object/array root elements
            Streams.write(element, writer)
            return str.toString()
        }

        fun showViews(vararg view: View) {
            view.forEach {
                it.visibility = View.VISIBLE
            }
        }

        fun hideViews(vararg view: View) {
            view.forEach {
                it.visibility = View.GONE
            }
        }
    }
}