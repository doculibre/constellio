package com.constellio.app.ui.framework.components.fields.exception;

import com.vaadin.data.Validator.InvalidValueException;

public class ValidationException {
	public static class ToManyCharacterToLongException extends InvalidValueException {

		public ToManyCharacterToLongException(String message) {
			super(message);
		}
	}
}
