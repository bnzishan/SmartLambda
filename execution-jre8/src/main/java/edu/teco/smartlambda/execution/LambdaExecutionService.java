package edu.teco.smartlambda.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.processor.LambdaFunctionProcessor;
import edu.teco.smartlambda.processor.LambdaMetaData;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import edu.teco.smartlambda.shared.GlobalOptions;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The application that runs inside a virtual container and shall receive the lambda parameter, execute the lambda and return the return
 * value
 */
public class LambdaExecutionService {
	
	/**
	 * Main function of the lambda executor service that executes the lambda archive inside a container
	 *
	 * @param args ignored command line parameters
	 */
	public static void main(final String... args) throws InterruptedException {
		final Gson gson = new GsonBuilder().create();
		
		// the reported return value
		ExecutionReturnValue executionReturnValue = new ExecutionReturnValue("", "");
		
		final DataInputStream  systemInputStream  = new DataInputStream(System.in);
		final DataOutputStream systemOutputStream = new DataOutputStream(System.out);
		
		try {
			// initialize class loader
			final URLClassLoader classLoader;
			try {
				classLoader = new URLClassLoader(new URL[] {new File(GlobalOptions.JRE_8_BINARY_NAME).toURI().toURL()},
						LambdaExecutionService.class.getClassLoader());
			} catch (final MalformedURLException e) {
				assert false;
				return;
			}
			
			// receive serialized parameter
			final int    length = systemInputStream.readInt();
			final byte[] buffer = new byte[length];
			
			//noinspection ResultOfMethodCallIgnored
			systemInputStream.read(buffer);
			systemInputStream.close();
			
			final String         jsonParameter = new String(buffer);
			final LambdaMetaData metaData;
			
			// acquire meta data object
			metaData = readMetaData(gson, classLoader);
			
			final Class<?> lambdaMainClass;
			final Class<?> lambdaParameterClass;
			final Method   lambdaFunction;
			try {
				lambdaMainClass = classLoader.loadClass(metaData.getLambdaClassName());
				lambdaParameterClass = metaData.isHasParameter() ? classLoader.loadClass(metaData.getLambdaParameterClassName()) : null;
				
				lambdaFunction = lambdaMainClass.getDeclaredMethod(metaData.getLambdaMethodName(), lambdaParameterClass);
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				e.printStackTrace();
				executionReturnValue =
						new ExecutionReturnValue(null, new InvalidLambdaDefinitionException("Invalid lambda meta file: " + e.getMessage
								()));
				return;
			}
			
			assert (lambdaParameterClass != null) == metaData.isHasParameter();
			final Object lambdaParameter = lambdaParameterClass != null ? gson.fromJson(jsonParameter, lambdaParameterClass) : null;
			
			try {
				final InputStream prevSysIn  = System.in;
				final PrintStream prevSysOut = System.out;
				
				System.setIn(new NullInputStream(0));
				System.setOut(new PrintStream(new NullOutputStream()));
				
				final Object returnValue = lambdaFunction.invoke(lambdaMainClass.getConstructor().newInstance(), lambdaParameter);
				
				System.setIn(prevSysIn);
				System.setOut(prevSysOut);
				
				executionReturnValue = new ExecutionReturnValue(gson.toJson(returnValue), "");
			} catch (final NoSuchMethodException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null,
						new InvalidLambdaDefinitionException("No accessible default " + "constructor in lambda class"));
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null,
						new InvalidLambdaDefinitionException("Could not invoke lambda " + "function: " + e.getMessage()));
			} catch (final InvocationTargetException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null, e.getCause());
			}
			
			classLoader.close();
		} catch (final IOException e) {
			e.printStackTrace();
			executionReturnValue = new ExecutionReturnValue(null, new Exception("Internal Server Error"));
		} finally {
			final String returnValue = new GsonBuilder().create().toJson(executionReturnValue);
			try {
				systemOutputStream.write(returnValue.getBytes());
				systemOutputStream.flush();
				systemOutputStream.close();
			} catch (final IOException e) {
				// fatal unfixable and unreportable
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Read the lambda meta data file and deserialize it
	 *
	 * @param classLoader the classloader containing the meta file
	 *
	 * @return the deserialized meta data object
	 *
	 * @throws IOException on stream fail
	 */
	private static LambdaMetaData readMetaData(final Gson gson, final URLClassLoader classLoader) throws IOException {
		return gson.fromJson(new InputStreamReader(classLoader.findResource(LambdaFunctionProcessor.LAMBDA_META_DATA_FILE).openStream()),
				LambdaMetaData.class);
	}
}
