package com.constellio.app.modules.rm.exports;

public class RetentionRuleXMLExporterRuntimeException extends RuntimeException {

	public RetentionRuleXMLExporterRuntimeException(String message) {
		super(message);
	}

	public RetentionRuleXMLExporterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RetentionRuleXMLExporterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RetentionRuleXMLExporterRuntimeException_InvalidFile extends RetentionRuleXMLExporterRuntimeException {

		public RetentionRuleXMLExporterRuntimeException_InvalidFile(String fileContent, Exception e) {
			super("Generated file has invalid content : \n" + fileContent, e);
		}
	}
}
