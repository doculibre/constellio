package com.constellio.data.io.concurrent.exception;


public class OptimisticLockingException extends AtomicIOException{
	public OptimisticLockingException() {
	}
	
	public OptimisticLockingException(Exception e) {
		super(e);
	}
	
	private static final long serialVersionUID = 1374102489991040718L;

}
