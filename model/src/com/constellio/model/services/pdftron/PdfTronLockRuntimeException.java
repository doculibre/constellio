package com.constellio.model.services.pdftron;

public class PdfTronLockRuntimeException extends RuntimeException {
	public static class PdfTronLockRuntimeException_LockIsAlreadyTaken extends RuntimeException {
		public PdfTronLockRuntimeException_LockIsAlreadyTaken() {
			super("Lock is already taken");
		}
	}
}
