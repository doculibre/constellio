package com.constellio.model.entities.schemas;

public class ModifiableMetadataNetworkLink {

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

	boolean mustBeOdd;
	boolean mustBeEven;

	public ModifiableMetadataNetworkLink(Metadata fromMetadata, Metadata toMetadata, Metadata refMetadata,
										 MetadataNetworkLinkType linkType, boolean mustBeOdd, boolean mustBeEven) {
		this.fromMetadata = fromMetadata;
		this.toMetadata = toMetadata;
		this.refMetadata = refMetadata;
		this.linkType = linkType;
		this.mustBeOdd = mustBeOdd;
		this.mustBeEven = mustBeEven;
		this.level = 0;
	}

	public boolean isMustBeOdd() {
		return mustBeOdd;
	}

	public boolean isMustBeEven() {
		return mustBeEven;
	}

	public Metadata getFromMetadata() {
		return fromMetadata;
	}

	public ModifiableMetadataNetworkLink setFromMetadata(Metadata fromMetadata) {
		this.fromMetadata = fromMetadata;
		return this;
	}

	public Metadata getToMetadata() {
		return toMetadata;
	}

	public ModifiableMetadataNetworkLink setToMetadata(Metadata toMetadata) {
		this.toMetadata = toMetadata;
		return this;
	}

	public Metadata getRefMetadata() {
		return refMetadata;
	}

	public ModifiableMetadataNetworkLink setRefMetadata(Metadata refMetadata) {
		this.refMetadata = refMetadata;
		return this;
	}

	public int getLevel() {
		return level;
	}

	public ModifiableMetadataNetworkLink setLevel(int level) {
		this.level = level;
		return this;
	}

	public MetadataNetworkLinkType getLinkType() {
		return linkType;
	}

	public ModifiableMetadataNetworkLink setLinkType(MetadataNetworkLinkType linkType) {
		this.linkType = linkType;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ModifiableMetadataNetworkLink that = (ModifiableMetadataNetworkLink) o;

		if (level != that.level) {
			return false;
		}
		if (!fromMetadata.getCode().equals(that.fromMetadata.getCode())) {
			return false;
		}
		if (!toMetadata.getCode().equals(that.toMetadata.getCode())) {
			return false;
		}

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
