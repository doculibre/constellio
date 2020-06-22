package com.constellio.app.services.factories;

public class AppLayerFactoryRuntineException extends RuntimeException {

	public AppLayerFactoryRuntineException(String message) {
		super(message);
	}

	public AppLayerFactoryRuntineException(String message, Throwable cause) {
		super(message, cause);
	}

	public AppLayerFactoryRuntineException(Throwable cause) {
		super(cause);
	}

	public static class AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldRetry extends AppLayerFactoryRuntineException {

		public AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldRetry() {
			super("AppLayerFactories initialize cancelled");
		}
	}
}
