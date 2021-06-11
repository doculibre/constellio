package com.constellio.data.extensions.contentDao;

public class ContentDaoWriteEvent {
	String hash;
	long length;
	long duration;
	boolean movedFromLocalFile;

	public String getHash() {
		return hash;
	}

	public ContentDaoWriteEvent setHash(String hash) {
		this.hash = hash;
		return this;
	}

	public long getLength() {
		return length;
	}

	public ContentDaoWriteEvent setLength(long length) {
		this.length = length;
		return this;
	}

	public long getDuration() {
		return duration;
	}

	public ContentDaoWriteEvent setDuration(long duration) {
		this.duration = duration;
		return this;
	}

	public boolean isMovedFromLocalFile() {
		return movedFromLocalFile;
	}

	public ContentDaoWriteEvent setMovedFromLocalFile(boolean movedFromLocalFile) {
		this.movedFromLocalFile = movedFromLocalFile;
		return this;
	}
}
