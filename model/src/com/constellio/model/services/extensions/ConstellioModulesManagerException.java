package com.constellio.model.services.extensions;

public class ConstellioModulesManagerException extends Exception {

	public ConstellioModulesManagerException(String message) {
		super(message);
	}

	public ConstellioModulesManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioModulesManagerException(Throwable cause) {
		super(cause);
	}

	public static class ConstellioModulesManagerException_ModuleInstallationFailed extends ConstellioModulesManagerException {


		private String failedModule;

		private String failedCollection;

		public ConstellioModulesManagerException_ModuleInstallationFailed(String failedModule, String failedCollection,
																		  Throwable cause) {
			super("Failed to install/update module '" + failedModule + "' in collection '" + failedCollection + "'", cause);
		}

		public String getFailedModule() {
			return failedModule;
		}

		public String getFailedCollection() {
			return failedCollection;
		}
	}
}
