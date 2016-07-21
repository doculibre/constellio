package com.constellio.model.services.contents;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ContentVersionDataSummary {

	private String hash;

	private String mimetype;

	private long length;

	public ContentVersionDataSummary(String hash, String mimetype, long length) {
		this.hash = hash;
		this.mimetype = mimetype;
		this.length = length;
	}

	public String getHash() {
		return hash;
	}

	public String getMimetype() {
		return mimetype;
	}

	public long getLength() {
		return length;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return "ContentVersionDataSummary{hash=" + hash + ", mimetype=" + mimetype + ", length=" + length + "}";
	}
}
