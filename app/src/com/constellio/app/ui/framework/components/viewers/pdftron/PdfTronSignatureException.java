package com.constellio.app.ui.framework.components.viewers.pdftron;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfTronSignatureException extends Exception {
	public PdfTronSignatureException(String message, Exception e) {
		super(message, e);
	}

	public static class PdfTronSignatureException_NotingToSignException extends PdfTronSignatureException {
		public PdfTronSignatureException_NotingToSignException() {
			super($("pdfTronViewer.notingToSignException"), new Exception());
		}
	}

	public static class PdfTronSignatureException_CannotReadSourceFileException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotReadSourceFileException() {
			super($("pdfTronViewer.cannotReadSourceFileException"), new Exception());
		}
	}

	public static class PdfTronSignatureException_CannotReadSignatureFileException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotReadSignatureFileException() {
			super($("pdfTronViewer.cannotReadSignatureFileException"), new Exception());
		}
	}

	public static class PdfTronSignatureException_CannotReadKeystoreFileException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotReadKeystoreFileException() {
			super($("pdfTronViewer.cannotReadKeystoreFileException"), new Exception());
		}

		public PdfTronSignatureException_CannotReadKeystoreFileException(Exception e) {
			super($("pdfTronViewer.cannotReadKeystoreFileException"), e);
		}
	}

	public static class PdfTronSignatureException_CannotReadSignedFileException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotReadSignedFileException(Exception e) {
			super($("pdfTronViewer.cannotReadSignedFileException"), e);
		}
	}

	public static class PdfTronSignatureException_CannotCreateTempFileException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotCreateTempFileException(Exception e) {
			super($("pdfTronViewer.cannotCreateTempFileException"), e);
		}
	}

	public static class PdfTronSignatureException_CannotSaveNewVersionException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotSaveNewVersionException(Exception e) {
			super($("pdfTronViewer.cannotSaveNewVersionException"), e);
		}
	}

	public static class PdfTronSignatureException_CannotSignDocumentException extends PdfTronSignatureException {
		public PdfTronSignatureException_CannotSignDocumentException(Exception e) {
			super($("pdfTronViewer.cannotSignDocumentException"), e);
		}
	}
}
