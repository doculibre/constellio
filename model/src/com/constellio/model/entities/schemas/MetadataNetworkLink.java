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
	/**
	 * Metadata used by 'fromMetadata'
	 */
	Metadata refMetadata;

	int level;

	MetadataNetworkLinkType linkType;

	//	int toNetworkLevel;

	public MetadataNetworkLink(Metadata fromMetadata, Metadata toMetadata, Metadata refMetadata, int level,
			MetadataNetworkLinkType linkType) {
		if (fromMetadata == null) {
			throw new IllegalArgumentException("fromMetadata is null");
		}
		if (toMetadata == null) {
			throw new IllegalArgumentException("toMetadata is null");
		}
		this.fromMetadata = fromMetadata;
		this.refMetadata = refMetadata;
		this.toMetadata = toMetadata;
		this.level = level;
		this.linkType = linkType;
	}

	public Metadata getFromMetadata() {
		return fromMetadata;
	}

	public Metadata getToMetadata() {
		return toMetadata;
	}

	public Metadata getRefMetadata() {
		return refMetadata;
	}

	public int getLevel() {
		return level;
	}

	public boolean isWithingSameSchemaType() {
		String fromSchemaType = fromMetadata.getSchemaTypeCode();
		String toSchemaType = toMetadata.getSchemaTypeCode();
		return fromSchemaType.equals(toSchemaType);
	}

	public MetadataNetworkLinkType getLinkType() {
		return linkType;
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
