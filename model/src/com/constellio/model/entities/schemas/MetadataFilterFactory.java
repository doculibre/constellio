package com.constellio.model.entities.schemas;

public class MetadataFilterFactory {

	public static MetadataFilter excludeMetadataWithLocalCode(final String locale) {
		return new MetadataFilter() {
			@Override
			public boolean isFiltered(Metadata metadata) {
				return metadata.getLocalCode().equals(locale);
			}
		};
	}

	public static MetadataFilter excludeMetadataOfSchemaType(final String schemaType, final String metadataLocalCode) {
		return new MetadataFilter() {
			@Override
			public boolean isFiltered(Metadata metadata) {
				return metadata.getSchemaTypeCode().equals(schemaType) && metadata.getLocalCode().equals(metadataLocalCode);
			}
		};
	}

	public static MetadataFilter excludeMetadataWithCode(final String metadataCode) {
		return new MetadataFilter() {
			@Override
			public boolean isFiltered(Metadata metadata) {
				return metadata.getCode().equals(metadataCode);
			}
		};
	}
}