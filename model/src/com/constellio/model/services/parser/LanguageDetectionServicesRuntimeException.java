package com.constellio.model.services.parser;

public class LanguageDetectionServicesRuntimeException extends RuntimeException {

	public LanguageDetectionServicesRuntimeException(String message) {
		super(message);
	}

	public LanguageDetectionServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LanguageDetectionServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class LanguageDetectionManagerRuntimeException_CannotDetectLanguage
			extends LanguageDetectionServicesRuntimeException {

		public LanguageDetectionManagerRuntimeException_CannotDetectLanguage(String content, Throwable cause) {
			super(newMessageContent(content), cause);
		}

		private static String newMessageContent(String content) {
			String first50CharOfContent = content.substring(0, Math.min(content.length(), 50));
			return "Cannot detect language of '" + first50CharOfContent + "'";
		}
	}

}
