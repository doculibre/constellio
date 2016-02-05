package com.constellio.data.io.concurrent.exception;


public class AtomicIOException extends RuntimeException{
	public AtomicIOException() {
	}

	public AtomicIOException(String msg) {
		super(msg);
	}

	public AtomicIOException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 1801501666564602151L;

	
}
