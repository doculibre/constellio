package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;

@SuppressWarnings("serial")
public class DecommissioningEmailServiceException extends Exception {

	protected DecommissioningEmailServiceException(String message) {
		super(message);
	}

	protected DecommissioningEmailServiceException(String message, Exception e) {
		super(message, e);
	}

	public static class CannotFindManangerEmail extends DecommissioningEmailServiceException {

		public CannotFindManangerEmail(DecommissioningList list) {
			super("Cannot find a manager with an email for that list'" + list.getTitle() + "'");
		}

	}
}
