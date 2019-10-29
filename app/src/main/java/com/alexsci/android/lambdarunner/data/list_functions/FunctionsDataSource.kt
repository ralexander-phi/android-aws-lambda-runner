package com.alexsci.android.lambdarunner.data.list_functions

import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.aws.lambda.ListFunctionsRequest
import com.alexsci.android.lambdarunner.data.list_functions.model.Function

class FunctionsDataSource(val client: LambdaClient) {
    fun listFunctions() : List<Function> {
        val result = client.list(ListFunctionsRequest())
        val list = ArrayList<Function>(result.functions.size)

        for (item in result.functions) {
            list.add(
                Function(
                    item.functionName,
                    item.functionArn,
                    item.description ?: "<no description>"
                )
            )
        }

        return list
    }
}

