package com.constellio.data.utils;

@SuppressWarnings("serial")
public class OfficeDocumentsServicesException extends Exception {

	public OfficeDocumentsServicesException(String message, Exception e) {
		super(message, e);
	}

	public OfficeDocumentsServicesException(String message) {
		super(message);
	}

	public static class CannotReadDocumentsProperties extends OfficeDocumentsServicesException {

		public CannotReadDocumentsProperties(Exception e) {
			super("Cannot read documents properties", e);
		}

	}

	public static class PropertyDoesntExist extends OfficeDocumentsServicesException {

		public PropertyDoesntExist(String propertyName) {
			super("The property doesn't exists : " + propertyName);
		}
	}

	public static class NotCompatibleExtension extends OfficeDocumentsServicesException {
		public NotCompatibleExtension(String ext) {
			super("The extension is not compatible : " + ext);
		}
	}

	public static class RTFFileIsNotCompatible extends OfficeDocumentsServicesException {
		public RTFFileIsNotCompatible() {
			super("The file is a RTF Document");
		}
	}

}
