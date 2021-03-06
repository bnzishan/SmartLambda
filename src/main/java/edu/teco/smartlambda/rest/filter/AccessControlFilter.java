package edu.teco.smartlambda.rest.filter;

import spark.Filter;
import spark.Request;
import spark.Response;

public class AccessControlFilter implements Filter {
	@Override
	public void handle(final Request request, final Response response) throws Exception {
		response.header("Access-Control-Allow-Origin", "*");
	}
}
