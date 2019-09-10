package com.constellio.model.services.schemas.builders;

@SuppressWarnings("serial")
public class ClassListBuilderRuntimeException extends RuntimeException {

	private ClassListBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	private ClassListBuilderRuntimeException(String message) {
		super(message);
	}

	private ClassListBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ClassNotFound extends ClassListBuilderRuntimeException {
		public ClassNotFound(String className, Throwable e) {
			super("Class '" + className + "' could not be found in classpath", e);
		}
	}

	public static class CannotInstanciate extends ClassListBuilderRuntimeException {
		public CannotInstanciate(String className, Throwable e) {
			super("Cannot instanciate '" + className + "'", e);
		}
	}

	public static class ClassDoesntImplementInterface extends ClassListBuilderRuntimeException {
		public ClassDoesntImplementInterface(String className, Class<?> interfaceType) {
			super("Class '" + className + "' doesn't implement '" + interfaceType.getName() + "'");
		}
	}

}
