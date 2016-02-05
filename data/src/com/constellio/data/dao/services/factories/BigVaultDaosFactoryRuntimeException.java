package com.constellio.data.dao.services.factories;

@SuppressWarnings("serial")
public class BigVaultDaosFactoryRuntimeException extends RuntimeException {

	public BigVaultDaosFactoryRuntimeException() {
	}

	public BigVaultDaosFactoryRuntimeException(String message) {
		super(message);
	}

	public BigVaultDaosFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public BigVaultDaosFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCreateContentsDAO extends BigVaultDaosFactoryRuntimeException {

		public CannotCreateContentsDAO(Throwable t) {
			super("Cannot create contents DAO", t);
		}
	}

}
