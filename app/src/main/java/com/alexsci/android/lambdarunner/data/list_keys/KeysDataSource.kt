package com.alexsci.android.lambdarunner.data.list_keys

import com.alexsci.android.lambdarunner.data.list_keys.model.Key
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement

class KeysDataSource(private val keyManagement: KeyManagement) {
    fun listKeys() : List<Key> {
        val keyNames = keyManagement.listKeys()
        val keyModels = ArrayList<Key>(keyNames.size)

        for (keyName in keyNames) {
            val info = keyManagement.describeKey(keyName)
            keyModels.add(Key(info.name, info.awsARN))
        }

        return keyModels
    }

    fun remove(key: Key) {
        keyManagement.deleteKey(key.keyName)
    }
}

