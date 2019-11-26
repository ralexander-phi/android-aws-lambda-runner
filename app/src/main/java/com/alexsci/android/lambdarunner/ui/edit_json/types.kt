package com.alexsci.android.lambdarunner.ui.edit_json

import com.google.gson.*
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject

enum class JsonTypes {
    Object,
    Array,
    String,
    Number,
    Boolean,
    Null,
    // Not JSON types, but something we will use
    PropertyKey,
    EndObject,
    EndArray
}

abstract class JsonType(val type: JsonTypes, var depth: Int) {
    abstract fun asJsonElement(): JsonElement
}

internal class JsonObject(depth: Int = 0): JsonType(JsonTypes.Object, depth) {
    override fun asJsonElement(): JsonObject {
        return JsonObject()
    }
}

internal class JsonArray(depth: Int = 0): JsonType(JsonTypes.Array, depth) {
    override fun asJsonElement(): JsonArray {
        return JsonArray()
    }
}

internal class JsonString(var value: String, depth: Int = 0): JsonType(JsonTypes.String, depth) {
    override fun asJsonElement(): JsonPrimitive {
        return JsonPrimitive(value)
    }
}

internal class JsonNumber(var value: Double, depth: Int = 0): JsonType(JsonTypes.Number, depth) {
    override fun asJsonElement(): JsonPrimitive {
        return JsonPrimitive(value)
    }
}

internal class JsonBoolean(var value: Boolean, depth: Int = 0): JsonType(JsonTypes.Boolean, depth) {
    override fun asJsonElement(): JsonPrimitive {
        return JsonPrimitive(value)
    }
}

internal class JsonNull(depth: Int = 0): JsonType(JsonTypes.Null, depth) {
    override fun asJsonElement(): JsonNull {
        return JsonNull.INSTANCE
    }
}

internal class JsonPropertyKey(
    var key: String, depth: Int = 0
): JsonType(JsonTypes.PropertyKey, depth) {
    override fun asJsonElement(): JsonElement {
        throw RuntimeException("Has no JsonElement")
    }
}

internal class JsonEndObject(depth: Int = 0): JsonType(JsonTypes.EndObject, depth) {
    override fun asJsonElement(): JsonElement {
        throw RuntimeException("Has no JsonElement")
    }
}

internal class JsonEndArray(depth: Int = 0): JsonType(JsonTypes.EndArray, depth) {
    override fun asJsonElement(): JsonElement {
        throw RuntimeException("Has no JsonElement")
    }
}
