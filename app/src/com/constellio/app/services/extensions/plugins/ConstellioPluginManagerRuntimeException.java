package com.constellio.app.services.extensions.plugins;

@SuppressWarnings("serial")
public class ConstellioPluginManagerRuntimeException extends RuntimeException {

	public ConstellioPluginManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioPluginManagerRuntimeException(String message) {
		super(message);
	}

	public ConstellioPluginManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConstellioPluginManagerRuntimeException_NoSuchModule extends ConstellioPluginManagerRuntimeException {

		public ConstellioPluginManagerRuntimeException_NoSuchModule(String moduleId) {
			super("No such module '" + moduleId + "'");
		}
	}

	public static class InvalidId extends ConstellioPluginManagerRuntimeException {
		public InvalidId(String id) {
			super(id);
		}

		public static class InvalidId_ExistingId extends InvalidId {
			public InvalidId_ExistingId(String id) {
				super(id);
			}
		}

		public static class InvalidId_BlankId extends InvalidId {
			public InvalidId_BlankId(String id) {
				super(id);
			}
		}

		public static class InvalidId_NonAlphaNumeric extends InvalidId {
			public InvalidId_NonAlphaNumeric(String id) {
				super(id);
			}
		}
	}

}
