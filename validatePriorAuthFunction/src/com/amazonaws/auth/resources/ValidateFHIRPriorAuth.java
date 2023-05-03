package com.amazonaws.auth.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import com.amazonaws.auth.LambdaHandler;
import com.amazonaws.serverless.proxy.internal.jaxrs.AwsProxySecurityContext.CognitoUserPoolPrincipal;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;


// snippet-start:[stepfunctions.java2.start_execute.import]

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.StartSyncExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartSyncExecutionResult;
@Path("/Bundle")
@io.swagger.annotations.Api(description = "the Bundle API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-07-17T16:45:16.134-07:00")
public class ValidateFHIRPriorAuth implements IResourceProvider {
	@Context
	SecurityContext securityContext;
	static final Logger log = LogManager.getLogger(ValidateFHIRPriorAuth.class);
	private static final String VALIDATE_FHIR_RESOURCE = System.getenv("VALIDATE_FHIR_RESOURCE");
	private static final String COGNITO_ENABLED = System.getenv("COGNITO_ENABLED");
	private static final String AUTH_STATE_MACHINE_ARN = System.getenv("AUTH_STATE_MACHINE_ARN");


	@POST
	@Consumes({ "application/fhir+json", "application/xml+fhir" })
	@Produces({ "application/fhir+json", "application/xml+fhir" })
	@io.swagger.annotations.ApiOperation(value = "", notes = "Create a new type ", response = Void.class, tags = {})
	@io.swagger.annotations.ApiResponses(value = {
			@io.swagger.annotations.ApiResponse(code = 201, message = "Succesfully created a new type ", response = Void.class),

			@io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request - Resource cound not be parsed or failed basic FHIR validation rules ", response = Void.class),

			@io.swagger.annotations.ApiResponse(code = 404, message = "Not Found - resource type not support, or not a FHIR validation rules ", response = Void.class) })

	public Response pOSTBundle(@Context SecurityContext securityContext, String bundleBlob) {
		log.info("Entering post bundle...");
		String userId = null;
		OperationOutcome opOutCome = null;
		
		if(COGNITO_ENABLED.equals("true")) {
			CognitoUserPoolPrincipal cognitoPrin = 
					securityContext.getUserPrincipal()!=null?(CognitoUserPoolPrincipal)securityContext.getUserPrincipal():null;
			userId = 
					cognitoPrin!=null?cognitoPrin.getClaims().getUsername():null;
		}
		
		log.info("Before Validation VALIDATE_FHIR_RESOURCE.."+VALIDATE_FHIR_RESOURCE);
		//ValidationResult result = FhirContext.forDstu3().newValidator().validateWithResult(patientBlob);
		if(VALIDATE_FHIR_RESOURCE.equals("true")) {
		     log.info("entering Validation started .."+userId);

			ValidationResult result = LambdaHandler.getFHIRValidator().validateWithResult(bundleBlob);
		   	log.info("after Validation  .."+result);
			if (result.getMessages().size() > 0) {
				log.debug("Validation failed ..");
				// The result object now contains the validation results
				for (SingleValidationMessage next : result.getMessages()) {
					log.debug("Validation message : " + next.getLocationString() + " " + next.getMessage());
				}
				return Response.status(Response.Status.BAD_REQUEST).header("Access-Control-Allow-Origin","*").header("Access-Control-Allow-Headers", "*").header("Access-Control-Allow-Methods", "*").build();
			}
		}
			//	log.info("Before Validation started ..bundle"+bundleBlob);

		Bundle bundle = LambdaHandler.getFHIRContext().newJsonParser().parseResource(Bundle.class, bundleBlob);
		log.info("Before getting client");

		AWSStepFunctions awsStepFunctions =LambdaHandler.getAWSSTeoAwsStepFunctionsClient();
				log.info("after getting client");

		// (5) Create a request to start execution with needed parameters
StartSyncExecutionRequest request = new StartSyncExecutionRequest()
                                      .withStateMachineArn(AUTH_STATE_MACHINE_ARN)
                                      .withInput(bundleBlob);
				log.info("after getting request");

// (6) Start the state machine and capture response
      StartSyncExecutionResult result = awsStepFunctions.startSyncExecution(request);
      				log.info("result "+result.getOutput()+ "Result detail is" + result.getOutputDetails()+ "status is" + result.getStatus());
      				
      String id=bundle.getId();
		//String id = this.createBundle(bundle,userId!=null?userId:"Unknown");
	//	String id =this.startWorkflow(awsStepFunctions,"arn:aws:states:us-west-2:349110885558:stateMachine:ProcessAuth",bundleBlob);
		List<BundleEntryComponent> list = bundle.getEntry();
		
		String patientId = "Auth1234";
		//Patient patient = null;
		String patientFullUrl = null;
	
		
		if (result.getStatus().equalsIgnoreCase("SUCCEEDED"))
		{
		opOutCome = new OperationOutcome();
		opOutCome.setId(new IdType("Patient", id, "1"));
		//opOutCome.fhirType();
		Narrative narrative = new Narrative();
		narrative.setStatus(NarrativeStatus.GENERATED);
		narrative.setDivAsString("Authorization completed Successfully created resource No issues detected during validation ");
		opOutCome.setText(narrative);
		ArrayList<OperationOutcomeIssueComponent> outcomelist = new ArrayList<OperationOutcomeIssueComponent>();
		
		OperationOutcomeIssueComponent issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.INFORMATION);
		issue.setCode(IssueType.INFORMATIONAL);
		issue.setDiagnostics("Successfully created resource Bundle/"+id+"/_history/1");
		outcomelist.add(issue);
		
		issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.INFORMATION);
		issue.setCode(IssueType.INFORMATIONAL);
		issue.setDiagnostics("No issues detected during validation");
		outcomelist.add(issue);
		opOutCome.addContained(bundle);
		
		opOutCome.setIssue(outcomelist);
		// return Response.status(201).entity(newOrder).build();

		log.debug("End of function...");
		log.info("End of function from system out....");

		return Response.status(Response.Status.CREATED).entity(LambdaHandler
				.getFHIRContext().newJsonParser()
				.encodeResourceToString(opOutCome)).header("Access-Control-Allow-Origin","*").header("Access-Control-Allow-Headers", "*").header("Access-Control-Allow-Methods", "*").build();
				
		}
		else
		{
			
				opOutCome = new OperationOutcome();
		opOutCome.setId(new IdType("Patient", id, "1"));
		//opOutCome.fhirType();
		Narrative narrative = new Narrative();
		narrative.setStatus(NarrativeStatus.GENERATED);
		narrative.setDivAsString("Clinical notes are not attached ");
		opOutCome.setText(narrative);
		ArrayList<OperationOutcomeIssueComponent> outcomelist = new ArrayList<OperationOutcomeIssueComponent>();
		
		OperationOutcomeIssueComponent issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.INFORMATION);
		issue.setCode(IssueType.INFORMATIONAL);
		issue.setDiagnostics("Authorization is not/"+id+"/_history/1");
		outcomelist.add(issue);
		
		issue = new OperationOutcomeIssueComponent();
		issue.setSeverity(IssueSeverity.INFORMATION);
		issue.setCode(IssueType.INFORMATIONAL);
		issue.setDiagnostics("Not Approved");
		outcomelist.add(issue);
		opOutCome.addContained(bundle);
		
		opOutCome.setIssue(outcomelist);
		
			return Response.status(Response.Status.BAD_REQUEST).entity(LambdaHandler
				.getFHIRContext().newJsonParser()
				.encodeResourceToString(opOutCome)).header("Access-Control-Allow-Origin","*").header("Access-Control-Allow-Headers", "*").header("Access-Control-Allow-Methods", "*").build();
		}
	}

 
    public Class<Bundle> getResourceType() {
        return Bundle.class;
    }

}
