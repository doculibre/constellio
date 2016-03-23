package com.constellio.model.utils;

import java.text.ParseException;

public class MaskUtilsException extends Exception {

	public MaskUtilsException(String message) {
		super(message);
	}

	public MaskUtilsException(String message, Throwable cause) {
		super(message, cause);
	}

	public MaskUtilsException(Throwable cause) {
		super(cause);
	}

	public static class MaskUtilsException_InvalidValue extends MaskUtilsException {

		public MaskUtilsException_InvalidValue(String mask, String value, ParseException e) {
			super("Value '" + value + "' is not compatible with mask '" + mask + "' : " + e.getMessage());
		}

		public MaskUtilsException_InvalidValue(String mask, String value) {
			super("Value '" + value + "' is not compatible with mask '" + mask + "'");
		}

	}

	public static class MaskUtilsException_InvalidMask extends MaskUtilsException {

		public MaskUtilsException_InvalidMask(String mask) {
			super("Mask '" + mask + "' is invalid");
		}

	}
}
