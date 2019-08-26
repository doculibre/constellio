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

		boolean launchRestart;

		public ConstellioModulesManagerException_ModuleInstallationFailed(String failedModule, String failedCollection,
																		  Throwable cause, boolean launchRestart) {
			super("Failed to install/update module '" + failedModule + "' in collection '" + failedCollection + "'", cause);
			this.launchRestart = launchRestart;
			this.failedCollection = failedCollection;
			this.failedModule = failedModule;
		}

		public String getFailedModule() {
			return failedModule;
		}

		public String getFailedCollection() {
			return failedCollection;
		}
	}
}
