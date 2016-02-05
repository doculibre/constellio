package com.constellio.model.services.workflows.bpmn;

@SuppressWarnings("serial")
public class BPMNParserRuntimeException extends RuntimeException {

	private BPMNParserRuntimeException(String message) {
		super(message);
	}

	public static class BPMNParserRuntimeException_InvalidCondition extends BPMNParserRuntimeException {
		public BPMNParserRuntimeException_InvalidCondition(String condition) {
			super("Condition in BPMN is invalid or unsupported : " + condition);
		}
	}

}
