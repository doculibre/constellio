package com.constellio.data.dao.managers.config.values;

import java.io.InputStream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.data.io.streamFactories.StreamFactory;

public class BinaryConfiguration {

	private final String version;

	private final StreamFactory<InputStream> inputStreamFactory;

	public BinaryConfiguration(String hash, StreamFactory<InputStream> inputStreamFactory) {
		super();
		this.version = hash;
		this.inputStreamFactory = inputStreamFactory;
	}

	public String getHash() {
		return version;
	}

	public StreamFactory<InputStream> getInputStreamFactory() {
		return inputStreamFactory;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
