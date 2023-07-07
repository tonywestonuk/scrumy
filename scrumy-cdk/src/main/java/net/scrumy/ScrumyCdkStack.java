package net.scrumy;

import java.util.Arrays;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.ContentHandling;
import software.amazon.awscdk.services.apigateway.Integration;
import software.amazon.awscdk.services.apigateway.IntegrationOptions;
import software.amazon.awscdk.services.apigateway.IntegrationResponse;
import software.amazon.awscdk.services.apigateway.LambdaIntegrationOptions;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class ScrumyCdkStack extends Stack {
	public ScrumyCdkStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public ScrumyCdkStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		var function = Function.Builder.create(this, "scrumy-site").code(Code.fromAsset("../site/target/function.zip"))
				.functionName("scrumy-lambda").memorySize(512)
				.handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
				.runtime(Runtime.JAVA_17).build();
		
		  var apiGateway = LambdaRestApi.Builder.create(this, "RestApiGateway")
				  .integrationOptions(LambdaIntegrationOptions.builder()
		       				.allowTestInvoke(false)
		       				.contentHandling(ContentHandling.CONVERT_TO_BINARY)
		       				//.integrationResponses(Arrays.asList(IntegrationResponse.builder().contentHandling(ContentHandling.CONVERT_TO_BINARY).build()))
		       				
		       				.build())
				  .handler(function)
				  .build();
	}
}
