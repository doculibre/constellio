package com.constellio.app.services.appManagement;

import java.io.File;

@SuppressWarnings("serial")
public class AppManagementServiceException extends Exception {

	protected AppManagementServiceException(String message) {
		super(message);
	}

	protected AppManagementServiceException(String message, Exception e) {
		super(message, e);
	}

	public static class CannotWriteInCommandFile extends AppManagementServiceException {

		public CannotWriteInCommandFile(File file, Exception e) {
			super("Cannot write in command file '" + file.getAbsolutePath() + "'", e);
		}

	}

	public static class CannotDeploy extends AppManagementServiceException {

		public CannotDeploy(Exception e) {
			super("Cannot deploy war ", e);
		}

	}

	public static class CannotSaveOldPlugins extends AppManagementServiceException {

		public CannotSaveOldPlugins(Exception e) {
			super("Cannot save old plugins", e);
		}

	}

}
