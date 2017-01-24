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

	int level;

	//	int toNetworkLevel;

	public ModifiableMetadataNetworkLink(Metadata fromMetadata, Metadata toMetadata, int level) {
		this.fromMetadata = fromMetadata;
		this.toMetadata = toMetadata;
		this.level = level;
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

	public int getLevel() {
		return level;
	}

	public ModifiableMetadataNetworkLink setLevel(int level) {
		this.level = level;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ModifiableMetadataNetworkLink that = (ModifiableMetadataNetworkLink) o;

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
