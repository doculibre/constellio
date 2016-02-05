package com.constellio.model.packaging.custom;

import java.io.File;

@SuppressWarnings("serial")
public class CustomPluginsPackagingServiceException extends RuntimeException {

	public CustomPluginsPackagingServiceException(String message, Exception e) {
		super(message, e);
	}

	public CustomPluginsPackagingServiceException(String message) {
		super(message);
	}

	public static class MethodCannotBeParsed extends CustomPluginsPackagingServiceException {

		public MethodCannotBeParsed(File license, String method) {
			super("Method '" + method + "' of license '" + license.getAbsolutePath() + "' cannot be parsed");
		}

	}

	public static class InvalidDate extends CustomPluginsPackagingServiceException {

		public InvalidDate(File license, String method, IllegalArgumentException e) {
			super("Date value of method '" + method + "' of license '" + license.getAbsolutePath() + "' cannot be parsed", e);
		}

	}

}
