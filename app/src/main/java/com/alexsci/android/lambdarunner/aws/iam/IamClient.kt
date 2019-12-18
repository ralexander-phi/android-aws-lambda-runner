package com.alexsci.android.lambdarunner.aws.iam

import android.util.Log
import arrow.core.Either
import com.alexsci.android.lambdarunner.aws.base.*
import com.amazonaws.*
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.AmazonHttpClient
import com.amazonaws.http.ExecutionContext
import com.amazonaws.http.HttpMethodName
import com.amazonaws.http.HttpResponse
import com.amazonaws.regions.Region
import com.amazonaws.util.XmlUtils
import com.google.api.client.util.DateTime
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import java.net.URI

class IamClient(credProvider: AWSCredentialsProvider):
        BaseClient(
            credProvider,
            "iam",
            Region.getRegion("us-east-1")
        ) {

    /*
      See https://docs.aws.amazon.com/IAM/latest/APIReference/API_GetUser.html
     */
    fun getUser(getUserRequest: GetUserRequest? = null) : Either<AmazonClientException, GetUserResponse> {
        try {
            val getUserRequest = getUserRequest ?: GetUserRequest()

            val request = DefaultRequest<Void>("iam")
            request.httpMethod = HttpMethodName.GET
            // global endpoint
            request.endpoint = URI.create("https://iam.amazonaws.com")

            request.addParameter("Action", getUserRequest.action)
            request.addParameter("Version", getUserRequest.version)
            request.addParameterIfNonNull("UserName", getUserRequest.username)

            sign(request)

            val response = AmazonHttpClient(ClientConfiguration())
                .execute(
                    request,
                    GetUserResponseHandler(),
                    SimpleErrorHandler(), ExecutionContext()
                )

            return Either.right(response.awsResponse)
        } catch (e: AmazonClientException) {
            return Either.left(e)
        }
    }
}

class GetUserResponseHandler : BaseResponseHandler<AmazonWebServiceResponse<GetUserResponse>>() {
    override fun doHandle(response: HttpResponse): AmazonWebServiceResponse<GetUserResponse> {
        val parsedResult = parseResponse()
        Log.i("RAA", parsedResult.responseMetadata.requestId)
        return parsedResult
    }

    private fun parseResponse() : AmazonWebServiceResponse<GetUserResponse> {
        val handler = GetUserContentHandler()
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver")
        XmlUtils.parse(response!!.content, handler)

        return AmazonWebServiceResponse<GetUserResponse>().apply {
            responseMetadata = handler.response.responseMetadata.asAwsType()
            result = handler.response
        }
    }

    private class GetUserContentHandler: ContentHandler {

        lateinit var response: GetUserResponse
        private lateinit var responseBuilder: GetUserResponse.Builder
        private lateinit var getUserResultBuilder: GetUserResult.Builder
        private lateinit var userBuilder: User.Builder
        private lateinit var responseMetadataBuilder: ResponseMetadata.Builder
        private lateinit var elementChars: StringBuilder

        override fun startElement(
            uri: String?,
            localName: String?,
            qName: String?,
            atts: Attributes?
        ) {
            // reset to store the chars of this element
            elementChars = StringBuilder()

            when(localName) {
                "GetUserResponse" -> responseBuilder = GetUserResponse.Builder()
                "GetUserResult" -> getUserResultBuilder = GetUserResult.Builder()
                "User" -> userBuilder = User.Builder()
                "ResponseMetadata" -> responseMetadataBuilder = ResponseMetadata.Builder()
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {
            val foundChars = elementChars.toString()

            Log.i("RAA", "Ending: $localName $foundChars")

            when (localName) {
                "GetUserResponse" -> response = responseBuilder.build()

                "GetUserResult" -> responseBuilder.getUserResult(getUserResultBuilder.build())

                "User" -> getUserResultBuilder.user(userBuilder.build())
                "UserId" -> userBuilder.userId(foundChars)
                "Path" -> userBuilder.path(foundChars)
                "UserName" -> userBuilder.userName(foundChars)
                "Arn" -> userBuilder.arn(foundChars)
                "CreateDate" -> userBuilder.createDate(DateTime(foundChars))
                "PasswordLastUsed" -> userBuilder.passwordLastUsed(DateTime(foundChars))

                "ResponseMetadata" -> responseBuilder.responseMetadata(responseMetadataBuilder.build())
                "RequestId" -> responseMetadataBuilder.requestId(foundChars)
            }
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (ch == null) return

            elementChars.append(ch.slice(IntRange(start, start+length-1)).joinToString(""))
        }

        // Unused
        override fun startDocument() {}
        override fun endDocument() {}
        override fun skippedEntity(name: String?) {}
        override fun setDocumentLocator(locator: Locator?) {}
        override fun endPrefixMapping(prefix: String?) {}
        override fun processingInstruction(target: String?, data: String?) {}
        override fun startPrefixMapping(prefix: String?, uri: String?) {}
        override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {}
    }
}

class GetUserRequest(val username: String? = null) :
    AWSCommonRequestParameters(
        "GetUser",
        "2010-05-08"
    )

class GetUserResponse private constructor(
    val getUserResult: GetUserResult,
    val responseMetadata: ResponseMetadata
) {
    data class Builder(
        var getUserResult: GetUserResult? = null,
        var responseMetadata: ResponseMetadata? = null
    ) {
        fun getUserResult(getUserResult: GetUserResult) { this.getUserResult = getUserResult }
        fun responseMetadata(responseMetadata: ResponseMetadata) { this.responseMetadata = responseMetadata }

        fun build(): GetUserResponse {
            return GetUserResponse(getUserResult!!, responseMetadata!!)
        }
    }
}

class GetUserResult private constructor(
    val user: User
) {
    data class Builder(
        var user: User? = null
    ) {
        fun user(user: User) { this.user = user }

        fun build(): GetUserResult {
            return GetUserResult(user!!)
        }
    }
}

class ResponseMetadata private constructor(
    private val requestId: String
) {
    data class Builder(
        var requestId: String? = null
    ) {
        fun requestId(requestId: String) { this.requestId = requestId }
        fun build(): ResponseMetadata {
            return ResponseMetadata(requestId!!)
        }
    }

    fun asAwsType() : com.amazonaws.ResponseMetadata {
        val map = HashMap<String, String>()
        map[com.amazonaws.ResponseMetadata.AWS_REQUEST_ID] = requestId
        return ResponseMetadata(map)
    }
}

class User private constructor(
    val userId: String,
    val path: String,
    val userName: String,
    val arn: String,
    val createDate: DateTime,
    val passwordLastUsed: DateTime?
) {
    data class Builder(
        var userId: String? = null,
        var path: String? = null,
        var userName: String? = null,
        var arn: String? = null,
        var createDate: DateTime? = null,
        var passwordLastUsed: DateTime? = null
    ) {
        fun userId(userId: String) { this.userId = userId }
        fun path(path: String) { this.path = path }
        fun userName(userName: String) { this.userName = userName }
        fun arn(arn: String) { this.arn = arn }
        fun createDate(createDate: DateTime) { this.createDate = createDate }
        fun passwordLastUsed(passwordLastUsed: DateTime) { this.passwordLastUsed = passwordLastUsed }

        fun build() : User {
            return User(userId!!, path!!, userName!!, arn!!, createDate!!, passwordLastUsed)
        }
    }
}
