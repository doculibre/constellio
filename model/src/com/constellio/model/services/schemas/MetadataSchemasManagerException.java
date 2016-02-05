package com.constellio.model.services.schemas;

import com.constellio.data.dao.managers.config.ConfigManagerException;

@SuppressWarnings("serial")
public class MetadataSchemasManagerException extends Exception {

	public MetadataSchemasManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataSchemasManagerException(String message) {
		super(message);
	}

	public MetadataSchemasManagerException(Throwable cause) {
		super(cause);
	}

	public static class OptimistickLocking extends MetadataSchemasManagerException {

		public OptimistickLocking(ConfigManagerException.OptimisticLockingConfiguration exception) {
			super(exception.getMessage(), exception);
		}

	}
}
