package com.alexsci.android.lambdarunner.data.list_functions

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import arrow.core.Either
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.aws.lambda.ListFunctionsRequest
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.amazonaws.AmazonServiceException
import java.lang.Exception

class FunctionsDataSource(private val context: Context) {
    fun listFunctions(accessKeyId: String, region: String) : Either<Exception, MutableList<Function>> {
        val client = LambdaClientBuilder(accessKeyId, region).getClient(context)
        try {
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

            return Either.right(list)
        } catch (e: Exception) {
            return Either.left(e)
        }
    }
}
