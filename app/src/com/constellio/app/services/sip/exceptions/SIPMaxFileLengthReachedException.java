package com.constellio.app.services.sip.exceptions;

@SuppressWarnings("serial")
public class SIPMaxFileLengthReachedException extends SIPMaxReachedException {

	private long sipFilesLength;

	private long sipMaxFilesLength;

	public SIPMaxFileLengthReachedException(long sipFilesLength, long sipMaxFilesLength, int lastDocumentIndex) {
		super(getMessage(sipFilesLength, sipMaxFilesLength, lastDocumentIndex), lastDocumentIndex);
		this.sipFilesLength = sipFilesLength;
		this.sipMaxFilesLength = sipMaxFilesLength;
	}

	public static String getMessage(long sipFilesLength, long sipMaxFilesLength, int lastDocumentIndex) {
		String message = "Document non ajouté parce que la limite de taille " + sipMaxFilesLength;
		message += " serait dépassée pour le SIP : " + sipFilesLength;
		message += " (index du dernier document ajouté au SIP: " + lastDocumentIndex + ")";
		return message;
	}

	public long getSipFilesLength() {
		return sipFilesLength;
	}

	public long getSipMaxFilesLength() {
		return sipMaxFilesLength;
	}

}
