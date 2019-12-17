package com.alexsci.android.lambdarunner.ui.edit_json

import android.view.View
import com.alexsci.android.lambdarunner.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonWriter
import java.io.StringWriter
import java.lang.RuntimeException

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

enum class JsonType(val str: String, val imageButtonIcon: Int) {
    OBJECT("Object", R.drawable.ic__code_braces),
    ARRAY("Array", R.drawable.ic__code_brackets),
    STRING("String", R.drawable.ic__quotes),
    NUMBER("Number", R.drawable.ic__number),
    BOOLEAN("Boolean", R.drawable.ic__boolean),
    NULL("null", R.drawable.null_slash);

    companion object {
        fun of(element: JsonElement): JsonType {
            when {
                element.isJsonObject -> return OBJECT
                element.isJsonArray -> return ARRAY
                element.isJsonNull -> return NULL
                element.isJsonPrimitive -> {
                    val p = element.asJsonPrimitive
                    when {
                        p.isString -> return STRING
                        p.isNumber-> return NUMBER
                        p.isBoolean-> return BOOLEAN
                    }
                }
            }
            throw RuntimeException("Unexpected")
        }

        fun of(jsonText: String): JsonType {
            return of(JsonParser().parse(jsonText))
        }
    }
}
