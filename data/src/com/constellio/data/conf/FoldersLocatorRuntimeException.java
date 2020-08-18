package com.constellio.data.conf;

@SuppressWarnings("serial")
public class FoldersLocatorRuntimeException extends RuntimeException {

	public FoldersLocatorRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FoldersLocatorRuntimeException(String message) {
		super(message);
	}

	public FoldersLocatorRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class NotAvailableInGitMode extends FoldersLocatorRuntimeException {

		public NotAvailableInGitMode(String resource) {
			super("The resource '" + resource + "'is not available in git mode.");
		}

	}

}
