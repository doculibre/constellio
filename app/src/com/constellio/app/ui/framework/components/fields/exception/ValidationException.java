package com.constellio.app.ui.framework.components.fields.exception;

import com.vaadin.data.Validator.InvalidValueException;

public class ValidationException {
	public static class ToManyCharacterException extends InvalidValueException {

		public ToManyCharacterException(String message) {
			super(message);
		}
	}
}
