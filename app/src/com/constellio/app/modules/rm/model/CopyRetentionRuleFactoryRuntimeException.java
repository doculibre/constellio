package com.constellio.app.modules.rm.model;

public class CopyRetentionRuleFactoryRuntimeException extends RuntimeException {

	public CopyRetentionRuleFactoryRuntimeException(String message) {
		super(message);
	}

	public CopyRetentionRuleFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CopyRetentionRuleFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CopyRetentionRuleFactoryRuntimeException_IdIsRequired extends CopyRetentionRuleFactoryRuntimeException {

		public CopyRetentionRuleFactoryRuntimeException_IdIsRequired() {
			super("Id is required for copies of rules");
		}
	}

	public static class CopyRetentionRuleFactoryRuntimeException_CannotModifyId extends CopyRetentionRuleFactoryRuntimeException {

		public CopyRetentionRuleFactoryRuntimeException_CannotModifyId(String oldId) {
			super("Cannot modify id of copy '" + oldId + "'");
		}
	}
}
