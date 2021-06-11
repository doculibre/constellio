package com.constellio.data.extensions.contentDao;

public class ContentDaoReadEvent {

	public enum ContentOperation {STREAM_OPENED, EXIST_CHECK, GET_PROPERTIES}

	String hash;

	ContentOperation contentOperation;

	long duration;

	public String getHash() {
		return hash;
	}

	public ContentDaoReadEvent setHash(String hash) {
		this.hash = hash;
		return this;
	}

	public ContentOperation getContentOperation() {
		return contentOperation;
	}

	public ContentDaoReadEvent setContentOperation(
			ContentOperation contentOperation) {
		this.contentOperation = contentOperation;
		return this;
	}

	public long getDuration() {
		return duration;
	}

	public ContentDaoReadEvent setDuration(long duration) {
		this.duration = duration;
		return this;
	}
}
