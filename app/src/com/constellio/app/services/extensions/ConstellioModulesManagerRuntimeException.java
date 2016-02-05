package com.constellio.app.services.extensions;

import com.constellio.app.entities.modules.InstallableModule;

@SuppressWarnings("serial")
public class ConstellioModulesManagerRuntimeException extends RuntimeException {

	public ConstellioModulesManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioModulesManagerRuntimeException(String message) {
		super(message);
	}

	public ConstellioModulesManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AlreadyStarted extends ConstellioModulesManagerRuntimeException {

		public AlreadyStarted(InstallableModule module) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' is already started");
		}

	}

	public static class FailedToStart extends ConstellioModulesManagerRuntimeException {

		public FailedToStart(InstallableModule module, Throwable e) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' failed to start", e);
		}

	}

	public static class FailedToStop extends ConstellioModulesManagerRuntimeException {

		public FailedToStop(InstallableModule module, Throwable e) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' failed to stop", e);
		}

	}

	public static class FailedToInstall extends ConstellioModulesManagerRuntimeException {

		public FailedToInstall(InstallableModule module, Exception e) {
			super("Module '" + module.getName() + "' of publisher '" + module.getPublisher() + "' failed to install", e);
		}

	}

}
