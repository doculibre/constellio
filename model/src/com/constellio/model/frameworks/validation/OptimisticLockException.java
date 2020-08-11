package com.constellio.model.frameworks.validation;

public class OptimisticLockException extends Exception {

	public OptimisticLockException(String id) {
		super("Optimistic locking for record : " + id);
	}
}
