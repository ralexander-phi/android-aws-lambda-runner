package com.alexsci.android.lambdarunner.data.list_functions

import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import arrow.core.Either
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClient
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.aws.lambda.ListFunctionsRequest
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import java.lang.Exception

class FunctionsDataSource(private val context: Context) {
    fun listFunctions(accessKeyId: String, region: String) : Either<AmazonClientException, MutableList<Function>> {
        val client = LambdaClientBuilder(accessKeyId, region).getClient(context)

        return when (val result = client.list(ListFunctionsRequest())) {
            is Either.Left -> Either.left(result.a)
            is Either.Right -> {
                val list = ArrayList<Function>(result.b.functions.size)
                for (item in result.b.functions) {
                    list.add(
                        Function(
                            item.functionName,
                            item.functionArn,
                            item.description ?: "<no description>"
                        )
                    )
                }
                Either.right(list)
            }
        }
    }
}
