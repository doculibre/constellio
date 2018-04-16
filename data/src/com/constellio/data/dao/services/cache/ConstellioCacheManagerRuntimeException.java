package com.constellio.data.dao.services.cache;

public class ConstellioCacheManagerRuntimeException extends RuntimeException {

	public ConstellioCacheManagerRuntimeException(String message) {
		super(message);
	}

	public ConstellioCacheManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class ConstellioCacheManagerRuntimeException_CacheAlreadyExist extends ConstellioCacheManagerRuntimeException {

		public ConstellioCacheManagerRuntimeException_CacheAlreadyExist(String cacheName) {
			super("Cache with name '" + cacheName + "' already exist");
		}
	}
}
