package com.constellio.sdk.tests.selenium.conditions;

public class ConditionTimeoutRuntimeException extends RuntimeException {

	public ConditionTimeoutRuntimeException(Exception e) {
		super("Impossible d'exécuter l'action dans les délais désirés, car cet exception survient : ", e);
	}
}
