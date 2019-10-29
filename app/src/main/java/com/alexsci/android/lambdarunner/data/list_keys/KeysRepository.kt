package com.alexsci.android.lambdarunner.data.list_functions

import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.alexsci.android.lambdarunner.data.list_keys.model.Key


class KeysRepository(val dataSource: KeysDataSource) {
    var functions: List<Key>? = null
        private set

    fun list(): List<Key>? {
        functions = dataSource.listKeys()
        return functions
    }
}