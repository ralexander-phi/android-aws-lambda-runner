package com.alexsci.android.lambdarunner.util.crypto

import android.util.JsonReader
import com.google.common.io.BaseEncoding
import com.google.gson.stream.JsonWriter
import java.io.StringReader
import java.io.StringWriter

interface IKeyInformation {
    fun toJson() : String
}

class BasicCredentialInformation(var name:String, var description:String, var iv:ByteArray, var encrypted:ByteArray) : IKeyInformation {
    constructor() : this("", "", ByteArray(0), ByteArray(0))

    constructor(input:String) : this() {
        val reader = JsonReader(StringReader(input))
        reader.beginObject()
        while (reader.hasNext()) {
            val fieldName = reader.nextName()
            if (fieldName == "Name") {
                this.name = reader.nextString()
            } else if (fieldName == "Description") {
                this.description = reader.nextString()
            } else if (fieldName == "IV") {
                this.iv = BaseEncoding.base16().lowerCase().decode(reader.nextString())
            } else if (fieldName == "Encrypted") {
                this.encrypted = BaseEncoding.base16().lowerCase().decode(reader.nextString())
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
    }

    override fun toJson() : String {
        val writer = StringWriter()
        val jsonWriter = JsonWriter(writer)

        jsonWriter.beginObject()

        jsonWriter.name("Name").value(name)
        jsonWriter.name("Description").value(description)
        jsonWriter.name("IV").value(BaseEncoding.base16().lowerCase().encode(iv))
        jsonWriter.name("Encrypted").value(BaseEncoding.base16().lowerCase().encode(encrypted))

        jsonWriter.endObject()
        return writer.toString()
    }
}

