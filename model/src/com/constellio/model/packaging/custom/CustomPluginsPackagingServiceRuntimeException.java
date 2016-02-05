package com.constellio.model.packaging.custom;

@SuppressWarnings("serial")
public class CustomPluginsPackagingServiceRuntimeException extends RuntimeException {

	public CustomPluginsPackagingServiceRuntimeException() {
	}

	public CustomPluginsPackagingServiceRuntimeException(String message) {
		super(message);
	}

	public CustomPluginsPackagingServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public CustomPluginsPackagingServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotWriteCustumerLicense extends CustomPluginsPackagingServiceRuntimeException {

		public CannotWriteCustumerLicense(String custumerName, String tempFilePath, Exception e) {
			super("Cannot write custumer license. Customer : " + custumerName + ", temporay file : " + tempFilePath, e);
		}
	}

	public static class CannotBuildCustumerJar extends CustomPluginsPackagingServiceRuntimeException {

		public CannotBuildCustumerJar(String custumerName, String binFolder, String jarDestinationFolder, Exception e) {
			super("Cannot build a custumer jar : " + custumerName + ", bin folder : " + binFolder + ", jar destination folder : "
					+ jarDestinationFolder, e);
		}

	}

}
