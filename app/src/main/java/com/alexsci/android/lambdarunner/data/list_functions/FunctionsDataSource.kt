package com.alexsci.android.lambdarunner.data.list_functions

import android.content.Context
import arrow.core.Either
import com.alexsci.android.lambdarunner.aws.lambda.LambdaClientBuilder
import com.alexsci.android.lambdarunner.aws.lambda.ListFunctionsRequest
import com.alexsci.android.lambdarunner.data.list_functions.model.Function
import com.amazonaws.AmazonClientException
import java.util.*

class FunctionsDataSource(private val context: Context) {
    fun listFunctions(accessKeyId: String, region: String) : Either<AmazonClientException, MutableList<Function>> {
        val client = LambdaClientBuilder(accessKeyId, region).getClient(context)
        val list = LinkedList<Function>()
        var nextMarker: String? = null

        do {
            when (val result = client.list(ListFunctionsRequest(marker = nextMarker))) {
                // An exception
                is Either.Left -> {
                    return Either.left(result.a)
                }

                // More functions
                is Either.Right -> {
                    for (item in result.b.functions) {
                        list.add(
                            Function(
                                item.functionName,
                                item.functionArn,
                                item.description ?: "<no description>"
                            )
                        )
                    }
                    nextMarker = result.b.nextMarker
                }
            }
        } while(nextMarker != null)

        return Either.right(list)
    }
}
