package com.constellio.app.modules.rm.services.sip.exceptions;

@SuppressWarnings("serial")
public class SIPMaxReachedException extends Exception {

	private int lastDocumentIndex;

	public SIPMaxReachedException(String message, int lastDocumentIndex) {
		super(message);
		this.lastDocumentIndex = lastDocumentIndex;
	}

	public int getLastDocumentIndex() {
		return lastDocumentIndex;
	}

}
