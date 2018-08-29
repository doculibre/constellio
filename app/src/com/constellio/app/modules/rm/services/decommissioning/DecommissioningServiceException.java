package com.constellio.app.modules.rm.services.decommissioning;

public class DecommissioningServiceException extends Exception {


	public DecommissioningServiceException(String message) {
		super(message);
	}

	public DecommissioningServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecommissioningServiceException(Throwable cause) {
		super(cause);
	}

	public static class DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission
			extends DecommissioningServiceException {

		public DecommissioningServiceException_TooMuchOptimisticLockingWhileAttemptingToDecommission() {
			super("Too much optimistic locking while attempting to decommission");
		}


	}

}
