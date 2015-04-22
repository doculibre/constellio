/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
