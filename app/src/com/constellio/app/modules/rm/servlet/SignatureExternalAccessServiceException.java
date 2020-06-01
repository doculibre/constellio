package com.constellio.app.modules.rm.servlet;

public class SignatureExternalAccessServiceException extends Exception {
	private int status;

	public SignatureExternalAccessServiceException(int status, String message) {
		super(message);
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
