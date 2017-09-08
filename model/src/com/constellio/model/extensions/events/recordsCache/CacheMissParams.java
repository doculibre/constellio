package com.constellio.model.extensions.events.recordsCache;

import com.constellio.model.entities.schemas.Metadata;

public class CacheMissParams {

	Metadata metadata;

	String value;

	long duration;

	public CacheMissParams(Metadata metadata, String value, long duration) {
		this.metadata = metadata;
		this.value = value;
		this.duration = duration;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public String getValue() {
		return value;
	}

	public long getDuration() {
		return duration;
	}
}
