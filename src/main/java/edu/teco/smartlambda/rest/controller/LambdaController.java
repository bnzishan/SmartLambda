package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.rest.exception.LambdaNotFoundException;
import edu.teco.smartlambda.rest.exception.UserNotFoundException;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Data;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class LambdaController {
	@Data
	private static class LambdaExecutionRequest {
		private Boolean    async;
		private ObjectNode parameters;
	}
	
	@Data
	private static class LambdaRequest {
		private Boolean async;
		private String  runtime;
		private byte[]  src;
	}
	
	@Data
	private static class LambdaResponse {
		private String  user;
		private String  name;
		private boolean async;
		private String  runtime;
		private byte[]  src;
	}
	
	@Data
	private static class StatisticsResponse {
		private long executions;
		private long averageExecutionTime;
		private long errors;
	}
	
	public static Object createLambda(final Request request, final Response response) throws IOException {
		final LambdaRequest  lambdaRequest = new ObjectMapper().readValue(request.body(), LambdaRequest.class);
		final AbstractLambda lambda        = LambdaFacade.getInstance().getFactory().createLambda();
		
		lambda.setAsync(lambdaRequest.getAsync());
		lambda.setOwner(User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user"))));
		lambda.setName(request.params(":name"));
		lambda.setRuntime(RuntimeRegistry.getInstance().getRuntimeByName(lambdaRequest.getRuntime()));
		lambda.deployBinary(lambdaRequest.getSrc());
		lambda.save();
		
		response.status(201);
		return new Object();
	}
	
	public static Object updateLambda(final Request request, final Response response) throws IOException {
		final LambdaRequest lambdaRequest = new ObjectMapper().readValue(request.body(), LambdaRequest.class);
		final String        name          = request.params(":name");
		final User user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		
		if (lambdaRequest.getAsync() != null) lambda.setAsync(lambdaRequest.getAsync());
		if (lambdaRequest.getRuntime() != null)
			lambda.setRuntime(RuntimeRegistry.getInstance().getRuntimeByName(lambdaRequest.getRuntime()));
		if (lambdaRequest.getSrc() != null) lambda.deployBinary(lambdaRequest.getSrc());
		lambda.update();
		
		response.status(200);
		return new Object();
	}
	
	public static Object readLambda(final Request request, final Response response) throws IOException {
		final LambdaResponse lambdaResponse = new LambdaResponse();
		final String         name           = request.params(":name");
		final User user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		
		lambdaResponse.setUser(lambda.getOwner().getName());
		lambdaResponse.setName(lambda.getName());
		lambdaResponse.setAsync(lambda.isAsync());
		lambdaResponse.setRuntime(lambda.getRuntime().getName());
		
		response.status(200);
		return lambdaResponse;
	}
	
	public static Object deleteLambda(final Request request, final Response response) {
		final String name = request.params(":name");
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		
		lambda.delete();
		
		response.status(200);
		return new Object();
	}
	
	public static Object executeLambda(final Request request, final Response response) throws IOException {
		final String                 name                   = request.params(":name");
		final LambdaExecutionRequest lambdaExecutionRequest = new ObjectMapper().readValue(request.body(), LambdaExecutionRequest.class);
		final User                   user                   =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		
		if ((lambda.isAsync() && lambdaExecutionRequest.async == null) ||
				(lambdaExecutionRequest.async != null && lambdaExecutionRequest.async)) {
			lambda.executeAsync(lambdaExecutionRequest.getParameters().toString());
			response.status(200);
			return "";
		} else {
			final ExecutionReturnValue executionReturnValue =
					lambda.executeSync(lambdaExecutionRequest.getParameters().toString()).getExecutionReturnValue();
			response.status(200);
			return executionReturnValue.getReturnValue().orElse(null);
		}
	}
	
	public static Object getLambdaList(final Request request, final Response response) {
		final List<LambdaResponse> lambdas = new LinkedList<>();
		
		for (final AbstractLambda lambda : User.getByName(request.params(":user"))
				.orElseThrow(() -> new UserNotFoundException(request.params(":name"))).getVisibleLambdas()) {
			final LambdaResponse lambdaResponse = new LambdaResponse();
			lambdaResponse.setName(lambda.getName());
			lambdaResponse.setUser(lambda.getOwner().getName());
			lambdaResponse.setRuntime(lambda.getRuntime().getName());
			lambdaResponse.setAsync(lambda.isAsync());
			
			lambdas.add(lambdaResponse);
		}
		
		response.status(200);
		return lambdas;
	}
	
	public static Object getStatistics(final Request request, final Response response) {
		final StatisticsResponse statisticsResponse = new StatisticsResponse();
		final User               user               =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		long                     executions         = 0;
		long                     totalExecutionTime = 0;
		long                     errors             = 0;
		
		for (final MonitoringEvent event : LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, request.params(":name"))
				.orElseThrow(() -> new LambdaNotFoundException(request.params(":name"))).getMonitoringEvents()) {
			executions++;
			if (event.getError() != null) errors++;
			
			totalExecutionTime += event.getDuration();
		}
		
		statisticsResponse.setExecutions(executions);
		statisticsResponse.setErrors(errors);
		statisticsResponse.setAverageExecutionTime(totalExecutionTime / executions);
		
		response.status(200);
		return statisticsResponse;
	}
}
