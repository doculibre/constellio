package com.constellio.data.io.concurrent.exception;


public class FileNotFoundException extends AtomicIOException{

	public FileNotFoundException(String msg) {
		super(msg);
	}
	
	public FileNotFoundException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 1L;
	

}
