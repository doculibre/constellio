package com.constellio.model.services.users;

@SuppressWarnings("serial")
public class GlobalGroupsManagerRuntimeException extends RuntimeException {

	public GlobalGroupsManagerRuntimeException(String message) {
		super(message);
	}

	public GlobalGroupsManagerRuntimeException(Exception e) {
		super(e);
	}

	public static class GlobalGroupsManagerRuntimeException_RecordException extends GlobalGroupsManagerRuntimeException {
		public GlobalGroupsManagerRuntimeException_RecordException(Exception e) {
			super(e);
		}
	}

	public static class GlobalGroupsManagerRuntimeException_ParentNotFound extends GlobalGroupsManagerRuntimeException {
		public GlobalGroupsManagerRuntimeException_ParentNotFound() {
			super("Global group parent not found!");
		}
	}

	public static class GlobalGroupsManagerRuntimeException_InvalidParent extends GlobalGroupsManagerRuntimeException {
		public GlobalGroupsManagerRuntimeException_InvalidParent(String parent) {
			super("Invalid parent: " + parent);
		}
	}
}
