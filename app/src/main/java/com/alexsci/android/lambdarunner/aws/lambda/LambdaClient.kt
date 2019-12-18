package com.alexsci.android.lambdarunner.aws.lambda

import android.content.Context
import arrow.core.Either
import com.alexsci.android.lambdarunner.aws.base.*
import com.alexsci.android.lambdarunner.util.crypto.KeyManagement
import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.ClientConfiguration
import com.amazonaws.DefaultRequest
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.AmazonHttpClient
import com.amazonaws.http.ExecutionContext
import com.amazonaws.http.HttpMethodName
import com.amazonaws.http.HttpResponse
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.regions.Region
import com.google.gson.JsonParser
import java.net.URI

class LambdaClient(
    _credProvider: AWSCredentialsProvider,
    _region: Region = Region.getRegion("us-east-1")
):
        BaseClient(
            _credProvider,
            "lambda",
            _region
        ) {

    private fun selectEndpoint(region: Region, path: String): URI? {
        assert(path.startsWith("/"))
        return URI.create("https://" + region.getServiceEndpoint("lambda") + path)
    }

    /*
      See: https://docs.aws.amazon.com/lambda/latest/dg/API_ListFunctions.html
     */
    fun list(listRequest: ListFunctionsRequest): Either<AmazonClientException, ListFunctionsResult> {
        try {
            val request = DefaultRequest<Void>("lambda")
            request.httpMethod = HttpMethodName.GET
            request.endpoint = selectEndpoint(region, "/2015-03-31/functions/")

            request.addParameter("FunctionVersion", listRequest.functionVersion)
            request.addParameterIfNonNull("Marker", listRequest.marker)
            request.addParameterIfNonNull("MasterRegion", listRequest.masterRegion)
            request.addParameterIfNonNull("MaxItems", listRequest.maxItems?.toString())

            sign(request)

            val response = AmazonHttpClient(ClientConfiguration())
                .execute(
                    request,
                    ListFunctionsResponseHandler(),
                    SimpleErrorHandler(), ExecutionContext()
                )

            return Either.right(response.awsResponse)
        } catch (e: AmazonClientException) {
            return Either.left(e)
        }
    }

    fun invoke(invokeRequest: InvokeFunctionRequest) : Either<AmazonClientException, InvokeFunctionResult> {
        try {
            val request = DefaultRequest<Void>("lambda")
            request.httpMethod = HttpMethodName.POST
            request.endpoint = selectEndpoint(
                region,
                "/2015-03-31/functions/${invokeRequest.functionName}/invocations"
            )

            request.addParameterIfNonNull("Qualifier", invokeRequest.qualifier)
            request.addHeaderIfNonNull("X-Amz-Invocation-Type", invokeRequest.invocationType)
            request.addHeaderIfNonNull("X-Amz-Log-Type", invokeRequest.logType)
            request.addHeaderIfNonNull("X-Amz-Client-Context", invokeRequest.clientContext)

            val payloadStream = invokeRequest.payload.toByteArray(Charsets.UTF_8)
            request.content = payloadStream.inputStream()
            request.addHeader("Content-Length", payloadStream.size.toString())

            sign(request)

            val response = AmazonHttpClient(ClientConfiguration())
                .execute(
                    request,
                    InvokeFunctionsResponseHandler(),
                    SimpleErrorHandler(),
                    ExecutionContext()
                )

            return Either.right(response.awsResponse)
        } catch (e: AmazonClientException) {
            return Either.left(e)
        }
    }
}

class LambdaClientBuilder(
    private val accessKey: String,
    private val region: String
) {
    fun getClient(context: Context) : LambdaClient {
        val secretKey = KeyManagement.getInstance(context).getKeyById(accessKey)
        val creds = BasicAWSCredentials(accessKey, secretKey)
        val credsProvider = StaticCredentialsProvider(creds)
        return LambdaClient(credsProvider, Region.getRegion(region))
    }
}

class InvokeFunctionRequest(
    val functionName: String,
    val payload: String,
    val invocationType: String? = null,
    val logType: String? = null,
    val qualifier: String? = null,
    val clientContext: String? = null
)

class InvokeFunctionResult(
    var executedVersion: String,
    var payload: String,
    var functionError: String? = null,
    var logResult: String? = null
)

class InvokeFunctionsResponseHandler : BaseResponseHandler<AmazonWebServiceResponse<InvokeFunctionResult>>() {
    override fun doHandle(response: HttpResponse): AmazonWebServiceResponse<InvokeFunctionResult> {
        val responseContent = getResponseContent()
            ?: throw RuntimeException("Null response content, no json")

        return AmazonWebServiceResponse<InvokeFunctionResult>().apply {
            result = InvokeFunctionResult(
                executedVersion = response.headers["X-Amz-Executed-Version"]!!,
                payload = responseContent,
                functionError = response.headers["X-Amz-Fuction-Error"],
                logResult = response.headers["X-Amz-Log-Result"]
            )
        }
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
        return parseJson(getResponseContent())
    }

    private fun parseJson(json: String?) : AmazonWebServiceResponse<ListFunctionsResult> {
        if (json == null) { throw RuntimeException("Null response content, no json") }

        val rootObject = JsonParser().parse(json).asJsonObject
        val functionsArray = rootObject.getAsJsonArray("Functions")

        val nextMarkerObject = rootObject.get("NextMarker")
        val nextMarkerString : String? =
            if (nextMarkerObject.isJsonNull) {
                null
            } else {
                nextMarkerObject.asJsonPrimitive.asString
            }

        val functionsList = ArrayList<Function>(functionsArray.size())
        for (function in functionsArray) {
            val functionObject = function.asJsonObject
            functionsList.add(Function(
                functionObject.get("FunctionName").asString,
                functionObject.get("FunctionArn").asString,
                functionObject.get("Description")?.asString
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

