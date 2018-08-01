package com.constellio.app.modules.rm.services.sip.exceptions;

@SuppressWarnings("serial")
public class SIPMaxFileCountReachedException extends SIPMaxReachedException {

	private int sipFilesCount;

	private int sipMaxFilesCount;

	public SIPMaxFileCountReachedException(int sipFilesCount, int sipMaxFilesCount, int lastDocumentIndex) {
		super(getMessage(sipFilesCount, sipMaxFilesCount, lastDocumentIndex), lastDocumentIndex);
		this.sipFilesCount = sipFilesCount;
		this.sipMaxFilesCount = sipMaxFilesCount;
	}

	public static String getMessage(long sipFilesCount, long sipMaxFilesCount, int lastDocumentIndex) {
		String message = "Document non ajouté parce que la limite de fichiers " + sipMaxFilesCount;
		message += " serait dépassée pour le SIP : " + sipFilesCount;
		message += " (index du dernier document ajouté au SIP: " + lastDocumentIndex + ")";
		return message;
	}

	public int getSipFilesCount() {
		return sipFilesCount;
	}

	public int getSipMaxFilesCount() {
		return sipMaxFilesCount;
	}

}
