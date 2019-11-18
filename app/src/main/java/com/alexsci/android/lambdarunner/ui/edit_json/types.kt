package com.alexsci.android.lambdarunner.ui.edit_json

import java.util.*

abstract class JsonType(val type: JsonTypes)

internal class JsonObject(var value: MutableMap<String, JsonType>): JsonType(JsonTypes.Object) {
    constructor(): this(TreeMap<String, JsonType>())
}

internal class JsonArray(var value: MutableList<JsonType>): JsonType(JsonTypes.Array) {
    constructor(): this(ArrayList<JsonType>())
}

internal class JsonString(var value: String): JsonType(JsonTypes.String)
internal class JsonNumber(var value: Double): JsonType(JsonTypes.Number)
internal class JsonBoolean(var value: Boolean): JsonType(JsonTypes.Boolean)
internal class JsonNull: JsonType(JsonTypes.Null)
internal class JsonProperty(var key: String, var value: JsonType): JsonType(JsonTypes.Property)
