package com.constellio.app.modules.rm.services.decommissioning;

import static com.constellio.app.ui.i18n.i18n.$;

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
			super($("DecommissioningListView.tooMuchOptimisticLocking"));
		}
	}

	public static class DecommissioningServiceException_DecommissioningListAlreadyInProcess
			extends DecommissioningServiceException {

		public DecommissioningServiceException_DecommissioningListAlreadyInProcess() {
			super($("DecommissioningListView.alreadyInProcess"));
		}
	}

	public static class DecommissioningServiceException_CannotDecommission
			extends DecommissioningServiceException {

		public DecommissioningServiceException_CannotDecommission() {
			super($("DecommissioningListView.cannotDecommission"));
		}
	}
}
