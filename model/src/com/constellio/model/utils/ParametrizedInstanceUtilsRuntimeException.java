package com.constellio.model.utils;

import java.util.List;

public class ParametrizedInstanceUtilsRuntimeException extends RuntimeException {

	public ParametrizedInstanceUtilsRuntimeException(String message) {
		super(message);
	}

	public ParametrizedInstanceUtilsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParametrizedInstanceUtilsRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class InstanceIsNotSublassOfRequiredType extends ParametrizedInstanceUtilsRuntimeException {

		public InstanceIsNotSublassOfRequiredType(Object object, Class<?> clazz, Exception e) {
			super("Object of class '" + object.getClass().getName() + "' must be subclass of type '" + clazz.getName() + "'", e);
		}

	}

	public static class NoSuchConstructor extends ParametrizedInstanceUtilsRuntimeException {

		public NoSuchConstructor(Class<?> typeClass, List<Class<?>> argumentsClasses, Exception e) {
			super("No such constructor in class '" + typeClass.getName() + "' with arguments '" + argumentsClasses + "'", e);
		}

	}

	public static class UnsupportedArgument extends ParametrizedInstanceUtilsRuntimeException {

		public UnsupportedArgument(Class<?> typeClass) {
			super("Parameter of type " + typeClass.getName() + " is not supported");
		}

	}

	public static class CannotInstanciate extends ParametrizedInstanceUtilsRuntimeException {

		public <T extends Parametrized> CannotInstanciate(String name, Exception e) {
			super("Cannot instanciate class '" + name
					+ "', make sure the constructor and class are public and that the class is not abstract", e);
		}

	}
}
