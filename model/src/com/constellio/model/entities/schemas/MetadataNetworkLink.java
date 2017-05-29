package com.constellio.model.entities.schemas;

import java.io.Serializable;

public class MetadataNetworkLink implements Serializable {

	/**
	 * Metadata using 'toMetadata'
	 */
	Metadata fromMetadata;

	/**
	 * Metadata used by 'fromMetadata'
	 */
	Metadata toMetadata;

	int level;

	//	int toNetworkLevel;

	public MetadataNetworkLink(Metadata fromMetadata, Metadata toMetadata, int level) {
		if (fromMetadata == null) {
			throw new IllegalArgumentException("fromMetadata is null");
		}
		if (toMetadata == null) {
			throw new IllegalArgumentException("toMetadata is null");
		}
		this.fromMetadata = fromMetadata;
		this.toMetadata = toMetadata;
		this.level = level;
	}

	public Metadata getFromMetadata() {
		return fromMetadata;
	}

	public Metadata getToMetadata() {
		return toMetadata;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MetadataNetworkLink that = (MetadataNetworkLink) o;

		if (level != that.level)
			return false;
		if (!fromMetadata.getCode().equals(that.fromMetadata.getCode()))
			return false;
		if (!toMetadata.getCode().equals(that.toMetadata.getCode()))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = fromMetadata.hashCode();
		result = 31 * result + toMetadata.hashCode();
		result = 31 * result + level;
		return result;
	}
}
