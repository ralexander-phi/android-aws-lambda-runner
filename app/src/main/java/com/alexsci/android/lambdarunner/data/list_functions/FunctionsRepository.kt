package com.alexsci.android.lambdarunner.data.list_functions

import com.alexsci.android.lambdarunner.data.list_functions.model.Function

class FunctionsRepository(val dataSource: FunctionsDataSource) {
    var functions: List<Function>? = null
        private set

    fun list(): List<Function>? {
        functions = dataSource.listFunctions()
        return functions
    }
}