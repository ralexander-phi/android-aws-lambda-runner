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

abstract class JsonType(val type: JsonTypes) {
    abstract fun asJsonElement(): JsonElement
}

internal class JsonObject: JsonType(JsonTypes.Object) {
    override fun asJsonElement(): JsonObject {
        return JsonObject()
    }
}

internal class JsonArray: JsonType(JsonTypes.Array) {
    override fun asJsonElement(): JsonArray {
        return JsonArray()
    }
}

internal class JsonString(var value: String): JsonType(JsonTypes.String) {
    override fun asJsonElement(): JsonPrimitive {
        return JsonPrimitive(value)
    }
}

internal class JsonNumber(var value: Double): JsonType(JsonTypes.Number) {
    override fun asJsonElement(): JsonPrimitive {
        return JsonPrimitive(value)
    }
}

internal class JsonBoolean(var value: Boolean): JsonType(JsonTypes.Boolean) {
    override fun asJsonElement(): JsonPrimitive {
        return JsonPrimitive(value)
    }
}

internal class JsonNull: JsonType(JsonTypes.Null) {
    override fun asJsonElement(): JsonNull {
        return JsonNull.INSTANCE
    }
}

internal class JsonPropertyKey(var key: String): JsonType(JsonTypes.PropertyKey) {
    override fun asJsonElement(): JsonElement {
        throw RuntimeException("Has no JsonElement")
    }
}

internal class JsonEndObject: JsonType(JsonTypes.EndObject) {
    override fun asJsonElement(): JsonElement {
        throw RuntimeException("Has no JsonElement")
    }
}

internal class JsonEndArray: JsonType(JsonTypes.EndArray) {
    override fun asJsonElement(): JsonElement {
        throw RuntimeException("Has no JsonElement")
    }
}
