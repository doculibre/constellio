package com.constellio.app.services.appManagement;

@SuppressWarnings({"serial"})
public class AppManagementServiceRuntimeException extends RuntimeException {

	public AppManagementServiceRuntimeException() {
	}

	public AppManagementServiceRuntimeException(String message) {
		super(message);
	}

	public AppManagementServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public AppManagementServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class WarFileNotFoundException extends AppManagementServiceRuntimeException {

		public WarFileNotFoundException() {
			super("No uploaded war files");
		}
	}

	public static class WarFileVersionMustBeHigher extends AppManagementServiceRuntimeException {

		public WarFileVersionMustBeHigher() {
			super("War file version must be higher");
		}
	}

	public static class AlertFileNotFoundException extends AppManagementServiceRuntimeException {

		public AlertFileNotFoundException() {
			super("No alerts found");
		}
	}

	public static class ErrorResponseCodeException extends AppManagementServiceRuntimeException {

		public ErrorResponseCodeException(int statusCode) {
			super("Unexpected request response '" + statusCode + "'");
		}
	}

	public static class CannotConnectToServer extends AppManagementServiceException {

		public CannotConnectToServer(String url, Exception e) {
			super("Cannot connect to server at url '" + url + "'", e);
		}

		public CannotConnectToServer(String url) {
			this("Cannot connect to server at url '" + url + "'", null);
		}
	}

	public static class AppManagementServiceRuntimeException_SameVersionsInDifferentFolders
			extends AppManagementServiceRuntimeException {

		public AppManagementServiceRuntimeException_SameVersionsInDifferentFolders(String version, String file1,
																				   String file2) {
			super("the same version: " + version + " could not be in different folders: " + file1 + ", " + file2);
		}
	}

	public static class InvalidLicenseInstalled extends AppManagementServiceRuntimeException {

		public InvalidLicenseInstalled() {
			super("Invalid license installed, couldn't be loaded");
		}

	}

}
