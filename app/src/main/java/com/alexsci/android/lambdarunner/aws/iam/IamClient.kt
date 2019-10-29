package com.alexsci.android.lambdarunner.aws.iam

import android.util.Log
import com.alexsci.android.lambdarunner.aws.base.AWSCommonRequestParameters
import com.alexsci.android.lambdarunner.aws.base.BaseClient
import com.alexsci.android.lambdarunner.aws.base.BaseResponseHandler
import com.alexsci.android.lambdarunner.aws.base.SimpleErrorHandler
import com.amazonaws.AmazonWebServiceResponse
import com.amazonaws.ClientConfiguration
import com.amazonaws.DefaultRequest
import com.amazonaws.ResponseMetadata
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.*
import com.google.api.client.util.DateTime
import com.google.gson.JsonParser
import java.lang.RuntimeException
import java.net.URI

class IamClient(credProvider: AWSCredentialsProvider) : BaseClient(credProvider, "iam") {

    /*
      See https://docs.aws.amazon.com/IAM/latest/APIReference/API_GetUser.html
     */
    fun getUser(getUserRequest: GetUserRequest? = null) : GetUserResult {
        val getUserRequestActual = getUserRequest ?: GetUserRequest()

        val request = DefaultRequest<Void>("iam")
        request.httpMethod = HttpMethodName.GET
        // global endpoint
        request.endpoint = URI.create("https://iam.amazonaws.com")

        if (getUserRequestActual.username != null) {
            request.addParameter("UserName", getUserRequestActual.username)
        }

        addBaseParameters(getUserRequestActual, request)
        sign(request)

        val response = AmazonHttpClient(ClientConfiguration())
            .execute(request,
                GetUserResponseHandler(),
                SimpleErrorHandler(), ExecutionContext())

        return response.awsResponse
    }
}

class GetUserResponseHandler : BaseResponseHandler<AmazonWebServiceResponse<GetUserResult>>() {
    override fun doHandle(response: HttpResponse): AmazonWebServiceResponse<GetUserResult> {
        val parsedResult = parseJson(responseContent)

        Log.i("RAA", parsedResult.responseMetadata.requestId)

        return parsedResult
    }

    private fun parseJson(json: String?) : AmazonWebServiceResponse<GetUserResult> {
        if (json == null) { throw RuntimeException("Null response content, no json") }

        val rootObject = JsonParser().parse(json).asJsonObject
        val userObject = rootObject.getAsJsonObject("User")
        val metadataObject = rootObject.getAsJsonObject("ResponseMetadata")

        val user = User(
            userObject.get("UserId").asString,
            userObject.get("Path").asString,
            userObject.get("UserName").asString,
            userObject.get("Arn").asString,
            DateTime(userObject.get("CreateData").asString),
            DateTime(userObject.get("PasswordLastUsed").asString)
        )

        val metadataMap = HashMap<String, String>()
        for(entry in metadataObject.entrySet()) {
            if (entry.value.isJsonPrimitive) {
                if (entry.value.asJsonPrimitive.isString) {
                    metadataMap[entry.key] = entry.value.asJsonPrimitive.asString
                }
            }
        }

        return AmazonWebServiceResponse<GetUserResult>().apply {
            responseMetadata = ResponseMetadata(metadataMap)
            result = GetUserResult(user)
        }
    }
}

class GetUserRequest(val username: String? = null) :
    AWSCommonRequestParameters(
        "GetUser",
        "2010-05-08"
    )

class GetUserResult(
    val user: User
)

class User(
    val userId: String,
    val path: String,
    val userName: String,
    val arn: String,
    val createDate: DateTime,
    val passwordLastUsed: DateTime
)

