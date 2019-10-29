package com.alexsci.android.lambdarunner.aws.base

import android.util.Log
import com.amazonaws.AmazonServiceException
import com.amazonaws.Request
import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.HttpResponse
import com.amazonaws.http.HttpResponseHandler
import java.io.BufferedReader
import java.lang.RuntimeException

open class BaseClient(
    val credentialsProvider: AWSCredentialsProvider,
    val serviceName: String,
    val region: String = "us-east-1"
) {

    protected fun <T> addBaseParameters(params: AWSCommonRequestParameters, request: Request<T>) {
        request.addParameter("Action", params.action)
        request.addParameter("Version", params.version)
    }

    protected fun <T> sign(request: Request<T>) {
        val signer = AWS4Signer()
        signer.setRegionName(region)
        signer.setServiceName(serviceName)
        signer.sign(request, credentialsProvider.credentials)
    }
}

abstract class BaseResponseHandler<T> : HttpResponseHandler<T> {
    var responseContent: String? = null

    override fun needsConnectionLeftOpen(): Boolean = true

    override fun handle(response: HttpResponse?): T {
        if (response == null) {
            throw RuntimeException("Response is null")
        }

        Log.v("RAA", "Status: " + response.statusCode + " -> " + response.statusText)
        Log.v("RAA", "Headers: " + response.headers.toString())

        responseContent = response.content.bufferedReader().use(BufferedReader::readText)
        Log.v("RAA", "Content: $responseContent")

        return doHandle(response)
    }

    abstract fun doHandle(response: HttpResponse): T
}

class SimpleErrorHandler : BaseResponseHandler<AmazonServiceException>() {
    override fun doHandle(response: HttpResponse): AmazonServiceException {
        return AmazonServiceException(response.statusText)
    }
}

