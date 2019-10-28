package com.alexsci.android.lambdarunner.aws

import com.amazonaws.AmazonClientException
import com.amazonaws.DefaultRequest
import com.amazonaws.Request
import com.amazonaws.http.HttpMethodName
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.transform.Marshaller
import com.amazonaws.util.BinaryUtils
import com.amazonaws.util.StringUtils

import java.util.LinkedList

class ListFunctionsRequestMarshaller :
    Marshaller<Request<ListFunctionsRequest>, ListFunctionsRequest> {

    override fun marshall(listRequest: ListFunctionsRequest?): Request<ListFunctionsRequest> {
        if (listRequest == null) {
            throw AmazonClientException("Invalid argument passed to marshall(InvokeRequest)")
        }

        val request = DefaultRequest<ListFunctionsRequest>(
            listRequest,
            "AWSLambda"
        )

        request.httpMethod = HttpMethodName.GET
        request.resourcePath = "/2015-03-31/functions/"
        if (!request.headers.containsKey("Content-Type")) {
            request.addHeader("Content-Type", "application/x-amz-json-1.0")
        }

        return request
    }
}
