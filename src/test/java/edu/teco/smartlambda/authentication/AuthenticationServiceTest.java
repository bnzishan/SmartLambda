package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created on 10.02.17.
 */
public class AuthenticationServiceTest {
	
	private ExecutorService executorService;
	
	@Before
	public void setUp() throws Exception {
		executorService = Executors.newSingleThreadExecutor();
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void getInstanceNotNull() throws Exception {
		Assert.assertNotNull(AuthenticationService.getInstance());
	}
	
	@Test
	public void getInstanceReturnsSingelton() throws Exception {
		AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		AuthenticationService asSecond = AuthenticationService.getInstance();
		Assert.assertNotNull(asSecond);
		Assert.assertSame(asFirst, asSecond);
	}
	
	@Test
	public void getInstanceSingeltonIsThreadlocal() throws Exception {
		AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		
		final Future<AuthenticationService> future = executorService.submit(AuthenticationService::getInstance);
		
		Assert.assertNotNull(future.get());
		Assert.assertNotSame(asFirst, future.get());
	}
	
	@Test
	public void authenticateViaKey() throws Exception {
		executorService.submit(() -> {
			final AuthenticationService authenticationService = AuthenticationService.getInstance();
			final User                  user                  = new User("AuthenticationServiceTest.authenticateViaKey.User");//TODO also
			// use an existing User an Key
			final Key                   key                   = user.createKey("AuthenticationServiceTest.authenticateViaKey").getLeft();
			authenticationService.authenticate(key);
			//checking for the Result after setting Key twice
			authenticationService.authenticate(key);
			return null;
		}).get();
	}
	
	@Test
	public void authenticateViaString() throws Exception {
		executorService.submit(() -> {
			final AuthenticationService authenticationService = AuthenticationService.getInstance();
			authenticationService.authenticate(""/*TODO also use an existing Key*/);
			//checking for the Result after setting Key twice
			authenticationService.authenticate("");
			return null;
		}).get();
	}
	
	@Test
	public void getAuthenticatedKeyViaKey() throws Exception {
		executorService.submit(() -> {
			AuthenticationService authenticationService = AuthenticationService.getInstance();
			User                  user                  = new User("AuthenticationServiceTest.getAuthenticatedKeyViaKey.User");//TODO also
			// use an existing User an Key
			Key                   key                   = user.createKey("AuthenticationServiceTest.getAuthenticatedKeyViaKey").getLeft();
			authenticationService.authenticate(key);
			Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
			assert keyOpt.isPresent();
			Assert.assertSame(keyOpt.get(), key);
			return null;
		}).get();
	}
	
	@Test
	public void getAuthenticatedKeyViaString() throws Exception {
		executorService.submit(() -> {
			AuthenticationService authenticationService = AuthenticationService.getInstance();
			authenticationService.authenticate("");//TODO use an existing Key
			Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
			assert keyOpt.isPresent();
			Key authenticatedKey = keyOpt.get();
			
			Method m = authenticatedKey.getClass().getDeclaredMethod("getId");
			
			m.setAccessible(true);
			String authenticatedKeyID = (String) m.invoke(authenticatedKey);
			m.setAccessible(false);
			
			Assert.assertSame(authenticatedKeyID, ""/*TODO use an existing Key*/);
			
			return null;
		}).get();
	}
	
	@Test
	public void getAuthenticatedUserViaKey() throws Exception {
		executorService.submit(() -> {
			final AuthenticationService authenticationService = AuthenticationService.getInstance();
			
			final User user = new User("AuthenticationServiceTest.getAuthenticatedUserViaKey.User");//TODO also use an existing User an Key
			
			Key key = user.createKey("AuthenticationServiceTest.getAuthenticatedUserViaKey").getLeft();
			authenticationService.authenticate(key);
			Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
			Assert.assertTrue(keyOpt.isPresent());
			Assert.assertSame(keyOpt.get().getUser(), key.getUser());
			
			return null;
		}).get();

	}
	
	@Test
	public void getAuthenticatedUserViaString() throws Exception {
		executorService.submit(() -> {
				final AuthenticationService authenticationService = AuthenticationService.getInstance();
				authenticationService.authenticate("");//TODO use an existing Key
				Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
				Assert.assertTrue(keyOpt.isPresent());
				Assert.assertSame(keyOpt.get().getUser(), ""/*TODO use the User of the Key*/);
			
		}).get();
	}
	
	@Test
	public void getForeignAuthenticatedKeyViaKey() throws Exception {
		AuthenticationService as0 = AuthenticationService.getInstance();
		Assume.assumeNotNull(as0);
		
		final User user = new User("AuthenticationServiceTest.getForeignAuthenticatedKeyViaKey.User");//TODO also use an existing User an
		// Key
		final Key  key0 = user.createKey("AuthenticationServiceTest.getForeignAuthenticatedKeyViaKey1").getLeft();
		as0.authenticate(key0);
		Optional<Key> key0Opt = as0.getAuthenticatedKey();
		Assert.assertTrue(key0Opt.isPresent());
		final Future<Key> future = executorService.submit(() -> {
			final Key key = user.createKey("AuthenticationServiceTest.getForeignAuthenticatedKeyViaKey0").getLeft();
			AuthenticationService.getInstance().authenticate(key);
			Optional<Key> keyOpt = AuthenticationService.getInstance().getAuthenticatedKey();
			Assert.assertTrue(keyOpt.isPresent());
			Assert.assertSame(keyOpt.get(), key);
			
			return key;
		});
		assert key0Opt.isPresent();
		Assert.assertNotSame(key0Opt.get(), future.get());
	}
}