package edu.teco.smartlambda.rest.controller;

import com.google.gson.Gson;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.lambda.LambdaFactory;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.Request;
import spark.Response;

import java.nio.charset.Charset;
import java.util.Optional;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LambdaFacade.class, User.class, RuntimeRegistry.class})
public class LambdaControllerTest {
	private static final Gson   gson             = new Gson();
	private static final String TEST_USER_NAME   = "TestUser";
	private static final String TEST_RUNTIME     = "TestRuntime";
	private static final byte[] TEST_SRC         = "TestSource".getBytes(Charset.forName("US-ASCII"));
	private static final String TEST_LAMBDA_NAME = "TestLambda";
	
	private User          testUser;
	private Runtime       testRuntime;
	private LambdaFactory lambdaFactory;
	
	@RequiredArgsConstructor
	private static class LambdaRequest {
		private final boolean async;
		private final String  runtime;
		private final byte[]  src;
	}
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(User.class);
		PowerMockito.mockStatic(RuntimeRegistry.class);
		PowerMockito.mockStatic(LambdaFacade.class);
		
		this.testUser = mock(User.class);
		when(User.getByName(TEST_USER_NAME)).thenReturn(Optional.ofNullable(this.testUser));
		
		this.testRuntime = mock(Runtime.class);
		
		final RuntimeRegistry runtimeRegistry = mock(RuntimeRegistry.class);
		when(RuntimeRegistry.getInstance()).thenReturn(runtimeRegistry);
		
		when(runtimeRegistry.getRuntimeByName(TEST_RUNTIME)).thenReturn(this.testRuntime);
		
		final LambdaFacade lambdaFacade = mock(LambdaFacade.class);
		when(LambdaFacade.getInstance()).thenReturn(lambdaFacade);
		this.lambdaFactory = mock(LambdaFactory.class);
		when(lambdaFacade.getFactory()).thenReturn(this.lambdaFactory);
	}
	
	private Pair<Response, AbstractLambda> doCreateLambda(final LambdaRequest lambdaRequest) throws Exception {
		final AbstractLambda lambda = mock(AbstractLambda.class);
		when(this.lambdaFactory.createLambda()).thenReturn(lambda);
		
		final Request request = mock(Request.class);
		
		when(request.body()).thenReturn(gson.toJson(lambdaRequest));
		when(request.params(":user")).thenReturn(TEST_USER_NAME);
		when(request.params(":name")).thenReturn(TEST_LAMBDA_NAME);
		
		final Response response = mock(Response.class);
		
		assertSame(Object.class, LambdaController.createLambda(request, response).getClass());
		
		return new ImmutablePair<>(response, lambda);
	}
	
	@Test
	public void createLambda() throws Exception {
		final Pair<Response, AbstractLambda> result   = this.doCreateLambda(new LambdaRequest(true, TEST_RUNTIME, TEST_SRC));
		final Response                       response = result.getLeft();
		final AbstractLambda                 lambda   = result.getRight();
		
		verify(response).status(201);
		verify(lambda).setName(TEST_LAMBDA_NAME);
		verify(lambda).setAsync(true);
		verify(lambda).setOwner(this.testUser);
		verify(lambda).setRuntime(this.testRuntime);
		verify(lambda).deployBinary(TEST_SRC);
		verify(lambda).save();
	}
	
	@Test
	public void createLambdaInvalidRuntime() throws Exception {
		final Response response = this.doCreateLambda(new LambdaRequest(true, "does_not_exist", TEST_SRC)).getLeft();
		verify(response).status(400);
	}
	
	@Test
	public void createLambdaMissingSource() throws Exception {
		final Response response = this.doCreateLambda(new LambdaRequest(true, TEST_RUNTIME, new byte[] {})).getLeft();
		verify(response).status(400);
	}
	
	@Test
	public void updateLambda() throws Exception {
		
	}
	
	@Test
	public void readLambda() throws Exception {
		
	}
	
	@Test
	public void deleteLambda() throws Exception {
		
	}
	
	@Test
	public void executeLambda() throws Exception {
		
	}
	
	@Test
	public void getLambdaList() throws Exception {
		
	}
	
	@Test
	public void getStatistics() throws Exception {
		
	}
}