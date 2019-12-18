package com.alexsci.android.lambdarunner.util.crypto

import android.util.JsonReader
import com.google.common.io.BaseEncoding
import com.google.gson.stream.JsonWriter
import java.io.StringReader
import java.io.StringWriter
import java.lang.RuntimeException

interface IKeyInformation {
    fun toJson(): String
}

class EncryptedCredentialInformation(
    var humanReadableName: String,
    var keyId: String,
    var iv: ByteArray,
    var encrypted: ByteArray
) : IKeyInformation {
    private enum class Fields(val key: String) {
        HumanReadableName("Name"),
        KeyIdentifier("Id"),
        InitializationVector("IV"),
        EncryptedContent("Encrypted")
    }

    companion object {
        private val BASE16_ENCODING: BaseEncoding = BaseEncoding.base16().lowerCase()

        fun fromJson(jsonText: String): EncryptedCredentialInformation {
            var humanReadableName: String? = null
            var keyId: String? = null
            var iv: ByteArray? = null
            var encrypted: ByteArray? = null

            val reader = JsonReader(StringReader(jsonText))
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    Fields.HumanReadableName.key ->
                        humanReadableName = reader.nextString()
                    Fields.KeyIdentifier.key ->
                        keyId = reader.nextString()
                    Fields.InitializationVector.key ->
                        iv = BaseEncoding.base16().lowerCase().decode(reader.nextString())
                    Fields.EncryptedContent.key ->
                        encrypted = BaseEncoding.base16().lowerCase().decode(reader.nextString())
                    else ->
                        reader.skipValue()
                }
            }
            reader.endObject()

            if (humanReadableName == null || keyId == null || iv == null || encrypted == null) {
                throw RuntimeException("JSON missing content")
            }

            return EncryptedCredentialInformation(humanReadableName, keyId, iv, encrypted)
        }
    }


    override fun toJson(): String {
        val writer = StringWriter()
        val jsonWriter = JsonWriter(writer)

        jsonWriter.beginObject()

        jsonWriter.name(Fields.HumanReadableName.key).value(humanReadableName)
        jsonWriter.name(Fields.KeyIdentifier.key).value(keyId)
        jsonWriter.name(Fields.InitializationVector.key).value(BASE16_ENCODING.encode(iv))
        jsonWriter.name(Fields.EncryptedContent.key).value(BASE16_ENCODING.encode(encrypted))

        jsonWriter.endObject()
        return writer.toString()
    }
}

