package com.constellio.data.frameworks.extensions;

public enum ExtensionBooleanResult {

	TRUE, FALSE, NOT_APPLICABLE, FORCE_TRUE;

	public static ExtensionBooleanResult trueIf(boolean value) {
		return value ? TRUE : FALSE;
	}

	public static ExtensionBooleanResult forceTrueIf(boolean value) {
		return value ? FORCE_TRUE : FALSE;
	}

	public static ExtensionBooleanResult falseIf(boolean value) {
		return value ? FALSE : TRUE;
	}
}
