package com.alexsci.android.lambdarunner.aws.lambda

import android.util.Log
import com.alexsci.android.lambdarunner.aws.base.BaseClient
import com.alexsci.android.lambdarunner.aws.base.BaseResponseHandler
import com.alexsci.android.lambdarunner.aws.base.SimpleErrorHandler
import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.ClientConfiguration
import com.amazonaws.DefaultRequest
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.AmazonHttpClient
import com.amazonaws.http.ExecutionContext
import com.amazonaws.http.HttpMethodName
import com.amazonaws.http.HttpResponse
import com.google.gson.JsonParser
import java.net.URI

class LambdaClient(credProvider: AWSCredentialsProvider, region: String = "us-east-1") :
        BaseClient(
            credProvider,
            "lambda",
            region
        ) {

    /*
      See: https://docs.aws.amazon.com/lambda/latest/dg/API_ListFunctions.html
     */
    fun list(listRequest: ListFunctionsRequest) : ListFunctionsResult {
        val request = DefaultRequest<Void>("lambda")
        request.httpMethod = HttpMethodName.GET
        // global endpoint
        request.endpoint = URI.create("https://lambda.us-east-1.amazonaws.com/2015-03-31/functions/")

        request.addParameter("FunctionVersion", listRequest.functionVersion)

        if (listRequest.marker != null) {
            request.addParameter("Marker", listRequest.marker)
        }
        if (listRequest.masterRegion != null) {
            request.addParameter("MasterRegion", listRequest.masterRegion)
        }
        if (listRequest.maxItems != null) {
            request.addParameter("MaxItems", listRequest.maxItems.toString())
        }

        sign(request)

        val response = AmazonHttpClient(ClientConfiguration())
            .execute(request,
                ListFunctionsResponseHandler(),
                SimpleErrorHandler(), ExecutionContext()
            )

        return response.awsResponse
    }
}

class ListFunctionsRequest(
    val functionVersion: String = "ALL",
    val marker: String? = null,
    val masterRegion: String? = null,
    val maxItems: Int? = null
)

class ListFunctionsResult(
    val functions: List<Function>,
    val nextMarker: String?
)

class Function(
    val functionName: String,
    val functionArn: String,
    val description: String? = null
)

class ListFunctionsResponseHandler : BaseResponseHandler<AmazonWebServiceResponse<ListFunctionsResult>>() {
    override fun doHandle(response: HttpResponse): AmazonWebServiceResponse<ListFunctionsResult> {
        return parseJson(responseContent)
    }

    private fun parseJson(json: String?) : AmazonWebServiceResponse<ListFunctionsResult> {
        if (json == null) { throw RuntimeException("Null response content, no json") }

        val rootObject = JsonParser().parse(json).asJsonObject
        val functionsArray = rootObject.getAsJsonArray("Functions")

        val nextMarkerObject = rootObject.get("NextMarker")
        val nextMarkerString : String?
        if (nextMarkerObject.isJsonNull) {
            nextMarkerString = null
        } else {
            nextMarkerString = nextMarkerObject.asJsonPrimitive.asString
        }

        val functionsList = ArrayList<Function>(functionsArray.size())
        for (function in functionsArray) {
            val functionObject = function.asJsonObject
            functionsList.add(Function(
                functionObject.get("FunctionName").asString,
                functionObject.get("FunctionArn").asString,
                functionObject.get("FunctionDescription")?.asString
            ))
        }

        return AmazonWebServiceResponse<ListFunctionsResult>().apply {
            result = ListFunctionsResult(
                functionsList,
                nextMarkerString
            )
        }
    }
}

