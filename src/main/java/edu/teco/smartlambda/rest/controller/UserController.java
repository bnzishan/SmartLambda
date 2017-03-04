package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.IdentityProvider;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import edu.teco.smartlambda.rest.exception.IdentityProviderNotFoundException;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Map;

public class UserController {
	@Data
	private static class RegistrationRequest {
		private String              identityProvider;
		private Map<String, String> parameters;
	}
	
	@Data
	private static class RegistrationResponse {
		private String name;
		private String primaryKey;
	}
	
	public static Object getUserList(final Request request, final Response response) {
		return null;
	}
	
	public static Object register(final Request request, final Response response) throws IOException {
		final RegistrationRequest registrationRequest = new ObjectMapper().readValue(request.body(), RegistrationRequest.class);
		final IdentityProvider identityProvider =
				IdentityProviderRegistry.getInstance().getIdentityProviderByName(registrationRequest.getIdentityProvider())
						.orElseThrow(() -> new IdentityProviderNotFoundException(registrationRequest.getIdentityProvider()));
		final Pair<User, String>   userKey              = identityProvider.register(registrationRequest.getParameters());
		final RegistrationResponse registrationResponse = new RegistrationResponse();
		registrationResponse.setName(userKey.getLeft().getName());
		registrationResponse.setPrimaryKey(userKey.getRight());
		
		return registrationResponse;
	}
}
