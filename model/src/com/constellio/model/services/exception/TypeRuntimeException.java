package com.constellio.model.services.exception;

public class TypeRuntimeException extends RuntimeException {
	public TypeRuntimeException(String message) {
		super(message);
	}

	public TypeRuntimeException(String message, Exception exception) {
		super(message, exception);
	}

	public static class UnsupportedTypeRunTimeException extends TypeRuntimeException {
		public UnsupportedTypeRunTimeException(String type) {
			super("Le " + type + " n'est pas supporté");
		}
	}

	public static class CannotGetTypeFromNullValueRunTimeException extends TypeRuntimeException {
		public CannotGetTypeFromNullValueRunTimeException(String variableName) {
			super("La variable" + variableName + " ne supporte pas null.");
		}
	}

	public static class InvalidValueForTypeRunTimeException extends TypeRuntimeException {
		public InvalidValueForTypeRunTimeException(String value, String type) {
			super("Le type : " + type + ", ne supporte pas la valeur : " + value);
		}
	}

	public static class TypeCannotBeRepresentedAsStringRunTimeException extends TypeRuntimeException {
		public TypeCannotBeRepresentedAsStringRunTimeException(String type) {
			super("Le type : " + type + ", ne peut pas être représenter dans une string.");
		}
	}
}
