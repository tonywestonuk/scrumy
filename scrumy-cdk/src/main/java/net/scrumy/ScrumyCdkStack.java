package net.scrumy;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
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

		Function.Builder.create(this, "scrumy-site").code(Code.fromAsset("../site/target/function.zip"))
				.functionName("scrumy-lambda").memorySize(512)
				.handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
				.runtime(Runtime.JAVA_17).build();
		
	}
}
