package com.alexsci.android.lambdarunner.data.list_functions

import arrow.core.Either
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import java.lang.Exception

class FunctionsRepository(private val dataSource: FunctionsDataSource) {
    fun list(accessKeyId: String, region: String): Either<Exception, MutableList<Function>> {
        return dataSource.listFunctions(accessKeyId, region)
    }
}

