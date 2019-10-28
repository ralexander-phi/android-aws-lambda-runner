package com.alexsci.android.lambdarunner.aws

import com.amazonaws.*
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.http.*
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.util.AWSRequestMetrics

class BetterAWSLambdaClient : AWSLambdaClient {

    private val awsCredentialsProvider : AWSCredentialsProvider

    constructor(awsCredentials: AWSCredentials) : this(StaticCredentialsProvider(awsCredentials))

    constructor(awsCredentials: AWSCredentials, clientConfiguration: ClientConfiguration)
            : this(StaticCredentialsProvider(awsCredentials), clientConfiguration)

    constructor(awsCredentialsProvider: AWSCredentialsProvider) : super(awsCredentialsProvider) {
        this.awsCredentialsProvider = awsCredentialsProvider
    }

    constructor(
        awsCredentialsProvider: AWSCredentialsProvider,
        clientConfiguration: ClientConfiguration
    )
            : super(awsCredentialsProvider, clientConfiguration) {
        this.awsCredentialsProvider = awsCredentialsProvider
    }

    constructor(
        awsCredentialsProvider: AWSCredentialsProvider,
        clientConfiguration: ClientConfiguration,
        httpClient: HttpClient
    ) :
            super(awsCredentialsProvider, clientConfiguration, httpClient) {
        this.awsCredentialsProvider = awsCredentialsProvider
    }

    fun listFunctions(listRequest : ListFunctionsRequest) : ListFunctionsResponse {
        val executionContext = createExecutionContext(listRequest)
        val awsRequestMetrics = executionContext.getAwsRequestMetrics()
        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.ClientExecuteTime)
        var request: Request<ListFunctionsRequest>? = null
        var response: Response<ListFunctionsResponse>? = null
        try {
            awsRequestMetrics.startEvent(AWSRequestMetrics.Field.RequestMarshallTime)
            try {
                request = ListFunctionsRequestMarshaller().marshall(listRequest)
                // Binds the request metrics to the current request.
                request!!.awsRequestMetrics = awsRequestMetrics
            } finally {
                awsRequestMetrics.endEvent(AWSRequestMetrics.Field.RequestMarshallTime)
            }
            val unmarshaller = ListFuctionsResponseUnmarshaller()
            val responseHandler = JsonResponseHandler(
                unmarshaller
            )

            response = invoke(request!!, responseHandler, executionContext)

            return response.getAwsResponse()
        } finally {
            awsRequestMetrics.endEvent(AWSRequestMetrics.Field.ClientExecuteTime)
            endClientExecution(
                awsRequestMetrics,
                request,
                response,
                AmazonWebServiceClient.LOGGING_AWS_REQUEST_METRIC
            )
        }
    }

    private operator fun <X, Y : AmazonWebServiceRequest> invoke(
        request: Request<Y>,
        responseHandler: HttpResponseHandler<AmazonWebServiceResponse<X>>,
        executionContext: ExecutionContext
    ): Response<X> {
        request.endpoint = endpoint
        request.timeOffset = timeOffset

        val awsRequestMetrics = executionContext.awsRequestMetrics
        var credentials: AWSCredentials
        awsRequestMetrics.startEvent(AWSRequestMetrics.Field.CredentialsRequestTime)
        try {
            credentials = awsCredentialsProvider.credentials
        } finally {
            awsRequestMetrics.endEvent(AWSRequestMetrics.Field.CredentialsRequestTime)
        }

        val originalRequest = request.originalRequest
        if (originalRequest != null && originalRequest.requestCredentials != null) {
            credentials = originalRequest.requestCredentials
        }

        executionContext.credentials = credentials
        val errorResponseHandler = JsonErrorResponseHandler(
            jsonErrorUnmarshallers
        )
        return client.execute(
            request, responseHandler,
            errorResponseHandler, executionContext
        )
    }
}
