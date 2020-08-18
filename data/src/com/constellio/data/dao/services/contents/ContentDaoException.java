package com.constellio.data.dao.services.contents;

@SuppressWarnings("serial")
public class ContentDaoException extends Exception {

	protected ContentDaoException(String message) {
		super(message);
	}

	protected ContentDaoException(Throwable t) {
		super(t);
	}

	public ContentDaoException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class ContentDaoException_NoSuchContent extends ContentDaoException {
		public ContentDaoException_NoSuchContent(String id) {
			super("Content for id '" + id + "' not found");
		}
	}


}
