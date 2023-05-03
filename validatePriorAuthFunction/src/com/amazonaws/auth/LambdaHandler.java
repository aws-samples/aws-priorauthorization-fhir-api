package com.amazonaws.auth;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.amazonaws.auth.exceptions.UncaughtException;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;

/**
 * This is the main Lambda handler class. It implements the RequestStreamHandler interface from the lambda-core package (see
 * maven dependencies). The only method defined by the RequestStreamHandler interface is the handleRequest method implemented.
 * 
 */
public class LambdaHandler implements RequestStreamHandler {
    // initialize the jersey application. Load the resource classes from the com.amazonaws.lab.resources
    // package and register Jackson as our JSON serializer.
    private static final ResourceConfig jerseyApplication = new ResourceConfig()
            // for this sample, we are configuring Jersey to pick up all resource classes
            // in the com.amazonaws.lab.resources package. To speed up start time, you 
            // could register the individual classes.
            .packages("com.amazonaws.auth.resources")
            .register(JacksonFeature.class)
            .register(UncaughtException.class);
    
    private static AWSStepFunctions client; 
    // Initialize the serverless-java-container library as a Jersey proxy
    private static final JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler
            = JerseyLambdaContainerHandler.getAwsProxyHandler(jerseyApplication);
    
    // singletons for the DDB client and object mapper
    private static AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.defaultClient();
    
    //private static DynamoDBMapper ddbMapper = new DynamoDBMapper(ddbClient);;
    private static AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

    private static FhirValidator fhirValidator = FhirContext.forR4().newValidator();
    

     
    private static FhirContext fhirContext = FhirContext.forR4();
    
    private static DynamoDB dyanmoDB = null;

    // Main entry point of the Lambda function, uses the serverless-java-container initialized in the global scope
    // to proxy requests to our jersey application
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) 
            throws IOException {
    	
        handler.proxyStream(inputStream, outputStream, context);

        // just in case it wasn't closed by the mapper
        outputStream.close();
    }
    public static FhirContext getFHIRContext() {
    	return fhirContext;
    }
    public static AmazonDynamoDB getDDBClient() {
    	
        return ddbClient;
    }

   public static AmazonS3 getS3Client() {
    	
        return s3Client;
    }
    
   public static AWSStepFunctions getAWSSTeoAwsStepFunctionsClient() {
    	Regions region = Regions.fromName(System.getenv("AWS_REGION"));

    	AWSStepFunctionsClientBuilder builder = AWSStepFunctionsClientBuilder.standard()
                                          .withRegion(region);
        return builder.build();
    }
    
    
    public static FhirValidator getFHIRValidator() {
    	return fhirValidator;
    }
    

    
    public static DynamoDB getDynamoDB() {
    	if(dyanmoDB == null) {
    		dyanmoDB = new DynamoDB(ddbClient);
    	}
    	return dyanmoDB;
    	
    }

}