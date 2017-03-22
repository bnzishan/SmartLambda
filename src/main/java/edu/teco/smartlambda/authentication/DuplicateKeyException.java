package edu.teco.smartlambda.authentication;

/**
 * Created on 15.02.17.
 * Is thrown when a name conflicts occurs because a Key with the same name already exists
 */
public class DuplicateKeyException extends RuntimeException{
	public DuplicateKeyException() {
		super();
	}
	public DuplicateKeyException(final String message) {
		super(message);
	}
}
