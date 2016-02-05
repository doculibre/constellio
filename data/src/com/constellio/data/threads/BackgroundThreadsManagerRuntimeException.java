package com.constellio.data.threads;

public class BackgroundThreadsManagerRuntimeException extends RuntimeException {

	public BackgroundThreadsManagerRuntimeException(String message) {
		super(message);
	}

	public BackgroundThreadsManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BackgroundThreadsManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured
			extends BackgroundThreadsManagerRuntimeException {

		public BackgroundThreadsManagerRuntimeException_RepeatInfosNotConfigured() {
			super("Must configure thread repeating");
		}
	}

	public static class BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads
			extends BackgroundThreadsManagerRuntimeException {

		public BackgroundThreadsManagerRuntimeException_ManagerMustBeStartedBeforeConfiguringThreads() {
			super("Threads manager must be started before configuring threads");
		}
	}
}
