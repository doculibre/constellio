package com.constellio.app.modules.rm.model.labelTemplate;

public class LabelTemplateManagerRuntimeException extends RuntimeException {

	public LabelTemplateManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate
			extends LabelTemplateManagerRuntimeException {

		public LabelTemplateManagerRuntimeException_CannotCreateLabelTemplate(Exception e) {
			super(e);
		}
	}
}
