package com.alexsci.android.lambdarunner.data.list_keys

import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement

class KeysDataSource(private val keyManagement: KeyManagement) {
    fun listKeys() : List<Key> {
        return keyManagement.describeKeys().map { Key(it.humanReadableName, it.keyId) }
    }

    fun remove(key: Key) {
        keyManagement.deleteKeyById(key.keyId)
    }
}

