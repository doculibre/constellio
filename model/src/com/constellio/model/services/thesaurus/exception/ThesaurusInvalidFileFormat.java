package com.constellio.model.services.thesaurus.exception;

public class ThesaurusInvalidFileFormat extends Exception {

	public ThesaurusInvalidFileFormat(Exception e) {
		super(e);
	}

	public ThesaurusInvalidFileFormat(String message, Exception e) {
		super(message, e);
	}
}
