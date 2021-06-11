package com.constellio.app.api.pdf.signature.exceptions;


import static com.constellio.app.ui.i18n.i18n.$;

public class PdfSignatureException extends Exception {

	public PdfSignatureException(String message, Exception e) {
		super(message, e);
	}

	public PdfSignatureException(String message) {
		super(message);
	}

	public static class PdfSignatureException_NothingToSignException extends PdfSignatureException {
		public PdfSignatureException_NothingToSignException() {
			super($("pdf.nothingToSignException"), new Exception());
		}
	}

	public static class PdfSignatureException_CannotReadSourceFileException extends PdfSignatureException {
		public PdfSignatureException_CannotReadSourceFileException() {
			this(new Exception());
		}
		
		public PdfSignatureException_CannotReadSourceFileException(Exception e) {
			super($("pdf.cannotReadSourceFileException"), e);
		}
	}

	public static class PdfSignatureException_CannotReadSignatureFileException extends PdfSignatureException {
		public PdfSignatureException_CannotReadSignatureFileException() {
			super($("pdf.cannotReadSignatureFileException"), new Exception());
		}
	}

	public static class PdfSignatureException_CannotReadKeystoreFileException extends PdfSignatureException {
		public PdfSignatureException_CannotReadKeystoreFileException() {
			super($("pdf.cannotReadKeystoreFileException"), new Exception());
		}

		public PdfSignatureException_CannotReadKeystoreFileException(Exception e) {
			super($("pdf.cannotReadKeystoreFileException"), e);
		}
	}

	public static class PdfSignatureException_CannotReadSignedFileException extends PdfSignatureException {
		public PdfSignatureException_CannotReadSignedFileException(Exception e) {
			super($("pdf.cannotReadSignedFileException"), e);
		}
	}

	public static class PdfSignatureException_CannotCreateTempFileException extends PdfSignatureException {
		public PdfSignatureException_CannotCreateTempFileException(Exception e) {
			super($("pdf.cannotCreateTempFileException"), e);
		}
	}

	public static class PdfSignatureException_CannotSaveNewVersionException extends PdfSignatureException {
		public PdfSignatureException_CannotSaveNewVersionException(Exception e) {
			super($("pdf.cannotSaveNewVersionException"), e);
		}
	}

	public static class PdfSignatureException_CannotSignDocumentException extends PdfSignatureException {
		public PdfSignatureException_CannotSignDocumentException(Exception e) {
			super($("pdf.cannotSignDocumentException"), e);
		}

		public PdfSignatureException_CannotSignDocumentException(String message, Exception e) {
			super(message, e);
		}
	}

	public static class PdfSignatureException_ExternalSignaturesAreDisabled extends PdfSignatureException {

		public PdfSignatureException_ExternalSignaturesAreDisabled() {
			super("External signatures are disabled");
		}
	}
}
